package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
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
 * Meta-level implementation of the methods {@link java.util.zip.ZipFile#getEntryCrc(long)}, 
 * {@link java.util.zip.ZipFile#getEntryCSize(long)}, {@link java.util.zip.ZipFile#getEntryFlag(long)}, 
 * {@link java.util.zip.ZipFile#getEntryMethod(long)}, {@link java.util.zip.ZipFile#getEntrySize(long)}, 
 * {@link java.util.zip.ZipFile#getEntryTime(long)}, {@link java.util.zip.ZipFile#getTotal(long)} and 
 * {@link java.util.zip.ZipFile#startsWithLOC(long)}.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_JAVA_ZIPFILE_GETENTRY_STARTS extends Algo_INVOKEMETA_Nonbranching {
	private final String methodName; //set by constructor
	private final boolean isEntry;   //set by constructor
	protected Simplex toPush;        //set by cookMore (via subclass method setToPush)
	
	public Algo_JAVA_ZIPFILE_GETENTRY_STARTS(String methodName, boolean isEntry) {
		this.methodName = methodName;
		this.isEntry = isEntry;
	}
	
	protected abstract void setToPush(Object retVal);
    
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected final void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, InvalidInputException {
        final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the long parameter
            final Primitive _jzValue = (Primitive) this.data.operand(0);
            if (_jzValue.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzentry parameter to invocation of method java.util.zip.ZipFile." + this.methodName + " cannot be a symbolic value.");
            }
            final long jzValue = ((Long) ((Simplex) _jzValue).getActualValue()).longValue();
            //TODO what if jzentry is not open?
            
            //invokes metacircularly the method
            final Method method = ZipFile.class.getDeclaredMethod(this.methodName, long.class);
            method.setAccessible(true);
            final long param = this.isEntry ? state.getZipFileEntryJz(jzValue) : state.getZipFileJz(jzValue);
            final Object retVal = method.invoke(null, param);
            setToPush(retVal);
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
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.toPush);
        };
    }
}
