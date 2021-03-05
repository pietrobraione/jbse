package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.Inflater;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.Inflater#getAdler(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_INFLATER_GETADLER extends Algo_INVOKEMETA_Nonbranching {
    private Simplex retVal; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the (long addr) parameter
            final Primitive _addr = (Primitive) this.data.operand(0);
            if (_addr.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long addr parameter to invocation of method java.util.zip.Inflater.getAdler cannot be a symbolic value.");
            }
            final long addr = ((Long) ((Simplex) _addr).getActualValue()).longValue();
            //TODO check that addr is valid, and react appropriately in the negative case
            
            //invokes metacircularly the getAdler method
            final Method method = Inflater.class.getDeclaredMethod("getAdler", long.class);
            method.setAccessible(true);
            final int _retVal = (int) method.invoke(null, state.getInflater(addr));
            this.retVal = calc.valInt(_retVal);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.retVal);
        };
    }
}
