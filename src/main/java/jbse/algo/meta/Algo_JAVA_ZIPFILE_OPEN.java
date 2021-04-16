package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
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
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#open(String, int, long, boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_OPEN extends Algo_INVOKEMETA_Nonbranching {
    private long jzfile; //set by cookMore
    private String name; //set by cookMore
    private int mode; //set by cookMore
    private long lastModified; //set by cookMore
    private boolean usemmap; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first (String name) parameter
            final Reference nameReference = (Reference) this.data.operand(0);
            if (state.isNull(nameReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            this.name = valueString(state, nameReference);
            if (this.name == null) {
                throw new SymbolicValueNotAllowedException("The String name parameter to invocation of method java.util.zip.ZipFile.open cannot be a symbolic String.");
            }
            
            //gets the second (int mode) parameter
            final Primitive _mode = (Primitive) this.data.operand(1);
            if (_mode.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int mode parameter to invocation of method java.util.zip.ZipFile.open cannot be a symbolic value.");
            }
            this.mode = ((Integer) ((Simplex) _mode).getActualValue()).intValue();
            
            //gets the third (long lastModified) parameter
            final Primitive _lastModified = (Primitive) this.data.operand(2);
            if (_lastModified.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long lastModified parameter to invocation of method java.util.zip.ZipFile.open cannot be a symbolic value.");
            }
            this.lastModified = ((Long) ((Simplex) _lastModified).getActualValue()).longValue();
            
            //gets the fourth (boolean usemmap) parameter
            final Primitive _usemmap = (Primitive) this.data.operand(3);
            if (_usemmap.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The boolean usemmap parameter to invocation of method java.util.zip.ZipFile.open method cannot be a symbolic value.");
            }
            this.usemmap = (((Integer) ((Simplex) _usemmap).getActualValue()).intValue() > 0);
            
            //invokes metacircularly the open method
            final Method method = ZipFile.class.getDeclaredMethod("open", String.class, int.class, long.class, boolean.class);
            method.setAccessible(true);
            this.jzfile = (long) method.invoke(null, this.name, this.mode, this.lastModified, this.usemmap);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (SecurityException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.addZipFile(this.jzfile, this.name, this.mode, this.lastModified, this.usemmap);
            final Simplex toPush = this.ctx.getCalculator().valLong(this.jzfile);
            state.pushOperand(toPush);
        };
    }
}
