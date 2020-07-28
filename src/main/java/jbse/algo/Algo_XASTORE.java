package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Calculator;
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

    private Primitive inRange, outOfRange; //set by cooker
    private Value valueToStore; //set by cooker

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
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //null check
            if (state.isNull(myObjectRef)) {
                throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            //creates the Values that check whether the index
            //is in range or out of range w.r.t. the array;
            //moreover, converts the value in case of [b/c/s]astore
            //and checks assignment compatibility in case of aastore
            try {
                final Array array = (Array) state.getObject(myObjectRef);
                final Calculator calc = this.ctx.getCalculator();
                this.inRange = array.inRange(calc, index);
                this.outOfRange = array.outOfRange(calc, index);
                final ClassFile arrayMemberType = array.getType().getMemberClass();
                if (arrayMemberType.isReference() || arrayMemberType.isArray()) {
                    if (!(value instanceof Reference)) {
                        throwVerifyError(state, this.ctx.getCalculator());
                        exitFromAlgorithm();
                    }
                    final Reference valueToStoreRef = (Reference) value;
                    final Objekt o = state.getObject(valueToStoreRef);
                    final ClassHierarchy hier = state.getClassHierarchy();
                    if (state.isNull(valueToStoreRef) ||
                        hier.isAssignmentCompatible(o.getType(), arrayMemberType)) {
                        this.valueToStore = value;
                    } else {
                        throwNew(state, this.ctx.getCalculator(), ARRAY_STORE_EXCEPTION);
                        exitFromAlgorithm();
                    }
                } else { //arrayMemberType.isPrimitive()
                    if (!(value instanceof Primitive)) {
                        throwVerifyError(state, this.ctx.getCalculator());
                        exitFromAlgorithm();
                    }
                    try {
                        final char arrayMemberTypeInternal = toPrimitiveOrVoidInternalName(arrayMemberType.getClassName());
                        this.valueToStore = (isPrimitiveOpStack(arrayMemberTypeInternal) ? value : 
                            this.ctx.getCalculator().push((Primitive) value).to(arrayMemberTypeInternal).pop());
                    } catch (InvalidTypeException e) {
                        throwVerifyError(state, this.ctx.getCalculator());
                        exitFromAlgorithm();
                    }
                }
            } catch (InvalidOperandException | InvalidTypeException | 
                     ClassCastException e) {
                //index is bad or the reference does not point to an array
                //or the class/superclasses of the array component, or of 
                //the value to store, is not in the classpath or are incompatible
                //with JBSE
                throwVerifyError(state, this.ctx.getCalculator());
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
            final Outcome o = this.ctx.decisionProcedure.decide_XASTORE(this.inRange, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XASTORE> refiner() {
        return (state, alt) -> {
            state.assume(this.ctx.getCalculator().simplify(this.ctx.decisionProcedure.simplify(alt.isInRange() ? this.inRange : this.outOfRange)));
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
                throwNew(state, this.ctx.getCalculator(), ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
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
