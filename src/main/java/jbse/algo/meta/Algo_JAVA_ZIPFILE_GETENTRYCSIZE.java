package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.ZipFile;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getEntryCSize(long)}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_JAVA_ZIPFILE_STARTSWITHLOC and Algo_JAVA_ZIPFILE_GETENTRYFLAG and Algo_JAVA_ZIPFILE_GETTOTAL and Algo_JAVA_ZIPFILE_GETENTRYTIME and Algo_JAVA_ZIPFILE_GETENTRYCRC and Algo_JAVA_ZIPFILE_GETENTRYSIZE and Algo_JAVA_ZIPFILE_GETENTRYMETHOD
public final class Algo_JAVA_ZIPFILE_GETENTRYCSIZE extends Algo_INVOKEMETA_Nonbranching {
    private Simplex toPush; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the (long jzentry) parameter
            final Primitive _jzentry = (Primitive) this.data.operand(0);
            if (_jzentry.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzentry parameter to invocation of method java.util.zip.ZipFile.getEntryCSize cannot be a symbolic value.");
            }
            final long jzentry = ((Long) ((Simplex) _jzentry).getActualValue()).longValue();
            //TODO what if jzentry is not open?
            
            //invokes metacircularly the getEntryCSize method
            final Method method = ZipFile.class.getDeclaredMethod("getEntryCSize", long.class);
            method.setAccessible(true);
            final long retVal = (long) method.invoke(null, state.getZipFileEntryJz(jzentry));
            this.toPush = calc.valLong(retVal);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.toPush);
        };
    }
}
