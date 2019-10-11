package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.JAVA_FILE_PATH;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.internalClassName;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getLastModifiedTime(File)} and
 * {@link java.io.WinNTFileSystem#getLastModifiedTime(File)}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_JAVA_XFILESYSTEM_CHECKACCESS, Algo_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX and JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME
public final class Algo_JAVA_XFILESYSTEM_GETLENGTH extends Algo_INVOKEMETA_Nonbranching {
    private Simplex toPush; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the filesystem object and its class
            final Field fileSystemField = File.class.getDeclaredField("fs");
            fileSystemField.setAccessible(true);
            final Object fileSystem = fileSystemField.get(null);
            final Class<?> fileSystemClass = fileSystem.getClass();
            final String methodName = "getLength";

            //gets the File parameter
            final Reference fileReference = (Reference) this.data.operand(1);
            if (state.isNull(fileReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance fileObject = (Instance) state.getObject(fileReference);
            if (fileObject == null) {
                //this should never happen
                failExecution("The File f parameter to invocation of method " + fileSystemClass.getName() + "." + methodName + " was an unresolved symbolic reference.");
            }
            
            //gets the path field as a String
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
                throw new SymbolicValueNotAllowedException("The File f parameter to invocation of method " + fileSystemClass.getName() + "." + methodName + " has a symbolic String in its path field.");
            }
            
            //creates a File object with same path and
            //invokes metacircularly the getLength
            //method to obtain its length
            final Method getLengthMethod = fileSystemClass.getDeclaredMethod(methodName, File.class);
            getLengthMethod.setAccessible(true);
            final File f = new File(filePath);
            try {
                final long length = ((Long) getLengthMethod.invoke(fileSystem, f)).longValue();
                this.toPush = calc.valLong(length);
            } catch (InvocationTargetException e) {
                final String cause = internalClassName(e.getCause().getClass().getName());
                throwNew(state, calc, cause);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | NoSuchMethodException e) {
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
