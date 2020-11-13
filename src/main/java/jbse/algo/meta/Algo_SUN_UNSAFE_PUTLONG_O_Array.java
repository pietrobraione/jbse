package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.storeInArray;
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
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putLong(Object, long, long)} and 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)} 
 * in the case the object to write into is an array.
 */
//TODO heavily copied from Algo_XASTORE: Refactor and merge 
public final class Algo_SUN_UNSAFE_PUTLONG_O_Array extends Algo_INVOKEMETA<
DecisionAlternative_XASTORE,
StrategyDecide<DecisionAlternative_XASTORE>, 
StrategyRefine<DecisionAlternative_XASTORE>, 
StrategyUpdate<DecisionAlternative_XASTORE>> {

    private Reference arrayReference; //set by cooker
    private Simplex index; //set by cooker
    private Value valueToStore; //set by cooker
    private Primitive inRange, outOfRange; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
            	final Calculator calc = this.ctx.getCalculator();
                this.arrayReference = (Reference) this.data.operand(1);
                this.index = (Simplex) calc.push((Simplex) this.data.operand(2)).narrow(INT).pop();
                this.valueToStore = this.data.operand(3);
                final Array array = (Array) state.getObject(this.arrayReference);
                this.inRange = array.inRange(calc, this.index);
                this.outOfRange = array.outOfRange(calc, this.index);

                //checks
                final ClassFile arrayType = array.getType();
                if (!arrayType.getMemberClass().getClassName().equals("long")) {
                    throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.putLongXxxx was an array whose member type is not long.");
                }
            } catch (ClassCastException | InvalidTypeException e) {
                //this should never happen now
                failExecution(e);
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
                throw new UndefinedResultException("The long offset parameter to sun.misc.Unsafe.putLongXxxx was out of range w.r.t. the Object o (array) parameter.");
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
