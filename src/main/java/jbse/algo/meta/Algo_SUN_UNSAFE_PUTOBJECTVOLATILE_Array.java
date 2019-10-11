package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.common.Type.INT;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)}
 * in the case the object to write to is an array.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTOBJECTVOLATILE_Array extends Algo_INVOKEMETA<
DecisionAlternative_XASTORE,
StrategyDecide<DecisionAlternative_XASTORE>, 
StrategyRefine<DecisionAlternative_XASTORE>, 
StrategyUpdate<DecisionAlternative_XASTORE>> {

    private Reference arrayReference; //produced by the cooker
    private Simplex index; //produced by the cooker
    private Primitive inRange, outOfRange; //produced by the cooker
    private Value valueToStore; //produced by the cooker
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        //copied from Algo_XASTORE
        return (state) -> { 
        	final Calculator calc = this.ctx.getCalculator();
            try {
                this.arrayReference = (Reference) this.data.operand(1);
            } catch (ClassCastException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            }
            try {
                this.index = (Simplex) calc.push((Simplex) this.data.operand(2)).narrow(INT).pop();
            } catch (ClassCastException | InvalidTypeException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            }
            
            final Value value = this.data.operand(3);
            
            //no null check

            //creates the Values that check whether the index
            //is in range or out of range w.r.t. the array
            //moreover, converts the value in case of [b/c/s]astore
            try {
                final Array array = (Array) state.getObject(this.arrayReference);
                this.inRange = array.inRange(calc, this.index);
                this.outOfRange = array.outOfRange(calc, this.index);
                final ClassFile arrayMemberType = array.getType().getMemberClass();
                if (arrayMemberType.isReference() || arrayMemberType.isArray()) {
                    if (!(value instanceof Reference)) {
                        throwVerifyError(state, calc);
                        exitFromAlgorithm();
                    }
                    final Reference valueToStoreRef = (Reference) value;
                    final Objekt o = state.getObject(valueToStoreRef);
                    final ClassHierarchy hier = state.getClassHierarchy();
                    if (state.isNull(valueToStoreRef) ||
                        hier.isAssignmentCompatible(o.getType(), arrayMemberType)) {
                        this.valueToStore = value;
                    } else {
                        throw new UndefinedResultException("The Object x parameter to sun.misc.Unsafe.putObjectVolatile was not assignment-compatible with the Object o (array) parameter.");
                    }
                } else {
                    throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.putObjectVolatile was an array whose member type is not a reference.");
                }
            } catch (InvalidInputException | InvalidTypeException | 
                     ClassCastException e) {
                //index is bad, or the reference does not point to an array,
                //or the class/superclasses of the array component, or of 
                //the value to store, are not in the classpath or are incompatible
                //with JBSE
                throwVerifyError(state, calc); //TODO is it ok?
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
                storeInArray(state, this.ctx, this.arrayReference, this.index, this.valueToStore);
            } else {
                throw new UndefinedResultException("The long offset parameter to sun.misc.Unsafe.putObjectVolatile was out of range w.r.t. the Object o (array) parameter.");
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}
