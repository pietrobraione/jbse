package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;
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
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putInt(Object, long, int)}, 
 * {@link sun.misc.Unsafe#putIntVolatile(Object, long, int)}, 
 * {@link sun.misc.Unsafe#putLong(Object, long, long)}, 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)}, 
 * {@link sun.misc.Unsafe#putObject(Object, long, Object)}, 
 * {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)} and 
 * {@link sun.misc.Unsafe#putOrderedObject(Object, long, Object)}
 * in the case the object to write into is an array.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_SUN_UNSAFE_PUTX_O_Array extends Algo_INVOKEMETA<
DecisionAlternative_XASTORE,
StrategyDecide<DecisionAlternative_XASTORE>, 
StrategyRefine<DecisionAlternative_XASTORE>, 
StrategyUpdate<DecisionAlternative_XASTORE>> {
	private final String methodName;       //set by constructor
    private Reference arrayReference;      //set by cooker
    private Simplex index;                 //set by cooker
    private Value valueToStore;            //set by cooker
    private Primitive inRange, outOfRange; //set by cooker
	
	public Algo_SUN_UNSAFE_PUTX_O_Array(String methodName) {
    	this.methodName = methodName;
	}
	
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    protected abstract void arrayMemberTypeCheck(ClassFile arrayMemberType, State state, Value valueToStore) 
    throws UndefinedResultException, FrozenStateException;

    @Override
    protected final BytecodeCooker bytecodeCooker() {
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
                final ClassFile arrayMemberType = array.getType().getMemberClass();
                arrayMemberTypeCheck(arrayMemberType, state, this.valueToStore);
            } catch (ClassCastException | InvalidTypeException e) {
                //this should never happen now
                failExecution(e);
            }
        };
    } 

    @Override
    protected final Class<DecisionAlternative_XASTORE> classDecisionAlternative() {
        return DecisionAlternative_XASTORE.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_XASTORE> decider() {
        return (state, result) -> { 
            final Outcome o = this.ctx.decisionProcedure.decide_XASTORE(this.inRange, result);
            return o;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_XASTORE> refiner() {
        return (state, alt) -> {
            state.assume(this.ctx.getCalculator().simplify(this.ctx.decisionProcedure.simplify(alt.isInRange() ? this.inRange : this.outOfRange)));
        };
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_XASTORE> updater() {
        return (state, alt) -> {
            if (alt.isInRange()) {
                storeInArray(state, this.ctx, this.arrayReference, this.index, this.valueToStore);
            } else {
                throw new UndefinedResultException("The long offset parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] was out of range w.r.t. the Object o (array) parameter.");
            }
        };
    }

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}
