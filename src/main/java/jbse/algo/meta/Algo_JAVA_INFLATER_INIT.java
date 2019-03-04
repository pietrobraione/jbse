package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.Inflater;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.Inflater#init(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_INFLATER_INIT extends Algo_INVOKEMETA_Nonbranching {
    private boolean nowrap; //set by cookMore
    private long address; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, InvalidInputException {
        try {
            //gets the (boolean nowrap) parameter
            final Primitive _nowrap = (Primitive) this.data.operand(0);
            if (_nowrap.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The boolean nowrap parameter to invocation of method java.util.zip.Inflater.init cannot be a symbolic value.");
            }
            this.nowrap = (((Integer) ((Simplex) _nowrap).getActualValue()).intValue() > 0);
            //TODO what if jzentry is not open?
            
            //invokes metacircularly the getEntryCrc method
            final Method method = Inflater.class.getDeclaredMethod("init", boolean.class);
            method.setAccessible(true);
            this.address = (long) method.invoke(null, this.nowrap);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, this.ctx.getCalculator(), cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.addInflater(this.address, this.nowrap);
            state.pushOperand(this.ctx.getCalculator().valLong(this.address));
        };
    }
}
