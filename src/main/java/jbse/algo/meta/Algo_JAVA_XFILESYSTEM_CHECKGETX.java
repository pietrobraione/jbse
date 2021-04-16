package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.JAVA_FILE_PATH;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.internalClassName;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getLastModifiedTime(File)},
 * {@link java.io.WinNTFileSystem#getLastModifiedTime(File)}, {@link java.io.UnixFileSystem#getLength(File)},
 * {@link java.io.WinNTFileSystem#getLength(File)}, {@link java.io.UnixFileSystem#getBooleanAttributes0(File)}, 
 * {@link java.io.WinNTFileSystem#getBooleanAttributes(File)}, {@link java.io.UnixFileSystem#checkAccess(File, int)} and
 * {@link java.io.WinNTFileSystem#checkAccess(File, int)}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_JAVA_XFILESYSTEM_CHECKGETX extends Algo_INVOKEMETA_Nonbranching {
	protected Class<?> fileSystemClass; //set by cookMore
    private Simplex toPush;             //set by cookMore
    
    protected abstract String methodName();
    
    protected abstract Object invokeMethod(Method method, Object fileSystem, File f) 
    throws InvocationTargetException, IllegalAccessException, IllegalArgumentException, SymbolicValueNotAllowedException;

    @Override
    protected final void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the filesystem object and its class
            final Field fileSystemField = File.class.getDeclaredField("fs");
            fileSystemField.setAccessible(true);
            final Object fileSystem = fileSystemField.get(null);
            this.fileSystemClass = fileSystem.getClass();
            final String methodName = methodName();

            //gets the File f parameter
            final Reference fileReference = (Reference) this.data.operand(1);
            if (state.isNull(fileReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance fileObject = (Instance) state.getObject(fileReference);
            if (fileObject == null) {
                //this should never happen
                failExecution("The File f parameter to invocation of method " + this.fileSystemClass.getName() + "." + methodName + " was an unresolved symbolic reference.");
            }
            
            //gets the path field as a String and
            //creates a File object with same path
            final Reference filePathReference = (Reference) fileObject.getFieldValue(JAVA_FILE_PATH);
            if (filePathReference == null) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            }
            if (state.isNull(filePathReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final String filePath = valueString(state, filePathReference);
            if (filePath == null) {
                throw new SymbolicValueNotAllowedException("The File f parameter to invocation of method " + this.fileSystemClass.getName() + "." + methodName + " has a symbolic String in its path field.");
            }
            final File f = new File(filePath);
            
            //invokes metacircularly the method and
            //stores the result
            final Method method = this.fileSystemClass.getDeclaredMethod(methodName, File.class);
            method.setAccessible(true);
            final Object result = invokeMethod(method, fileSystem, f);
            if (result instanceof Boolean) {
            	this.toPush = calc.valInt(((Boolean) result).booleanValue() ? 1 : 0);
            } else {
            	this.toPush = calc.val_(result);
            }
        } catch (InvocationTargetException e) {
        	final String cause = internalClassName(e.getCause().getClass().getName());
        	throwNew(state, calc, cause);
        	exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | 
        NoSuchMethodException | InvalidInputException e) {
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
