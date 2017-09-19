package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.className;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.isReference;

import java.util.function.Supplier;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} managing all the *astore (store into array) bytecodes 
 * ([a/c/b/d/f/i/l/s]astore). It decides over access index 
 * membership (inbound vs. outbound), which is a sheer numeric 
 * decision.
 * 
 * @author Pietro Braione
 */
final class Algo_XASTORE extends Algorithm<
BytecodeData_0,
DecisionAlternative_XASTORE, 
StrategyDecide<DecisionAlternative_XASTORE>, 
StrategyRefine<DecisionAlternative_XASTORE>, 
StrategyUpdate<DecisionAlternative_XASTORE>> {

    Primitive inRange, outOfRange; //produced by the cooker
    Value valueToStore; //produced by the cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            Reference myObjectRef = null;
            Primitive index = null;
            Value value = null;
            try {
                myObjectRef = (Reference) this.data.operand(0);
                index = (Primitive) this.data.operand(1);
                value = this.data.operand(2);
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

            //null check
            if (state.isNull(myObjectRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            //creates the Values that check whether the index
            //is in range or out of range w.r.t. the array;
            //moreover, converts the value in case of [b/c/s]astore
            //and checks assignment compatibility in case of aastore
            try {
                final Array array = (Array) state.getObject(myObjectRef);
                this.inRange = array.inRange(index);
                this.outOfRange = array.outOfRange(index);
                final String arrayMemberType = getArrayMemberType(array.getType());
                if (isReference(arrayMemberType) || isArray(arrayMemberType)) {
                    if (!(value instanceof Reference)) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                    final Reference valueToStoreRef = (Reference) value;
                    final Objekt o = state.getObject(valueToStoreRef);
                    final ClassHierarchy hier = state.getClassHierarchy();
                    if (state.isNull(valueToStoreRef) ||
                    hier.isAssignmentCompatible(o.getType(), className(arrayMemberType))) {
                        this.valueToStore = value;
                    } else {
                        throwNew(state, ARRAY_STORE_EXCEPTION);
                        exitFromAlgorithm();
                    }
                } else {
                    if (!(value instanceof Primitive)) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                    try {
                        this.valueToStore = (isPrimitiveOpStack(arrayMemberType.charAt(0)) ? value : 
                                             ((Primitive) value).to(arrayMemberType.charAt(0)));
                    } catch (InvalidTypeException e) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                }
            } catch (InvalidOperandException | InvalidTypeException | 
            ClassCastException | BadClassFileException e) {
                //index is bad or the reference does not point to an array
                //or the class/superclasses of the array component, or of 
                //the value to store, is not in the classpath or are incompatible
                //with JBSE
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_XASTORE> classDecisionAlternative() {
        return DecisionAlternative_XASTORE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XASTORE> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_XASTORE(state.getClassHierarchy(), this.inRange, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XASTORE> refiner() {
        return (state, alt) -> {
            state.assume(this.ctx.decisionProcedure.simplify(alt.isInRange() ? this.inRange : this.outOfRange));
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XASTORE> updater() {
        return (state, alt) -> {
            if (alt.isInRange()) {
                final Reference arrayReference = (Reference) this.data.operand(0);
                final Primitive index = (Primitive) this.data.operand(1);
                storeInArray(state, this.ctx, arrayReference, index, this.valueToStore);
            } else {
                throwNew(state, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XALOADSTORE_OFFSET;
    }
}
