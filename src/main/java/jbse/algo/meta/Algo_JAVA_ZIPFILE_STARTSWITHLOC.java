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
 * Meta-level implementation of {@link java.util.zip.ZipFile#startsWithLOC(long)}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_JAVA_ZIPFILE_GETTOTAL and Algo_JAVA_ZIPFILE_GETENTRYFLAG and Algo_JAVA_ZIPFILE_GETENTRYTIME and Algo_JAVA_ZIPFILE_GETENTRYCRC and Algo_JAVA_ZIPFILE_GETENTRYSIZE and Algo_JAVA_ZIPFILE_GETENTRYCSIZE and Algo_JAVA_ZIPFILE_GETENTRYMETHOD
public final class Algo_JAVA_ZIPFILE_STARTSWITHLOC extends Algo_INVOKEMETA_Nonbranching {
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
            //gets the (long jzfile) parameter
            final Primitive _jzfile = (Primitive) this.data.operand(0);
            if (_jzfile.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzfile parameter to invocation of method java.util.zip.ZipFile.startsWithLOC cannot be a symbolic value.");
            }
            final long jzfile = ((Long) ((Simplex) _jzfile).getActualValue()).longValue();
            //TODO what if jzfile is not open?
            
            //invokes metacircularly the startsWithLOC method
            final Method method = ZipFile.class.getDeclaredMethod("startsWithLOC", long.class);
            method.setAccessible(true);
            final boolean retVal = (boolean) method.invoke(null, state.getZipFileJz(jzfile));
            this.toPush = calc.valInt(retVal ? 1 : 0);
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
