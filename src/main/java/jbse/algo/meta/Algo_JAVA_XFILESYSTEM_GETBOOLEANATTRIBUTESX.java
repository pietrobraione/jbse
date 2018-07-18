package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.JAVA_FILE_PATH;
import static jbse.bc.Signatures.JAVA_UNIXFILESYSTEM;
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
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#getBooleanAttributes0(File)} and
 * {@link java.io.WinNTFileSystem#getBooleanAttributes(File)}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME and Algo_JAVA_XFILESYSTEM_GETLENGTH
public final class Algo_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX extends Algo_INVOKEMETA_Nonbranching {
    private Simplex toPush; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
        try {
            //gets the filesystem object and its class
            final Field fileSystemField = File.class.getDeclaredField("fs");
            fileSystemField.setAccessible(true);
            final Object fileSystem = fileSystemField.get(null);
            final Class<?> fileSystemClass = fileSystem.getClass();
            final boolean isUnix = JAVA_UNIXFILESYSTEM.equals(internalClassName(fileSystemClass.getName()));
            final String methodName = "getBooleanAttributes" + (isUnix ? "0" : "");

            //gets the File parameter
            final Reference fileReference = (Reference) this.data.operand(1);
            if (state.isNull(fileReference)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
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
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            if (state.isNull(filePathReference)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final String filePath = valueString(state, filePathReference);
            if (filePath == null) {
                throw new SymbolicValueNotAllowedException("The File f parameter to invocation of method " + fileSystemClass.getName() + "." + methodName + " has a symbolic String in its path field.");
            }
            
            //creates a File object with same path and
            //invokes metacircularly the getBooleanAttributes[0]
            //method to obtain its attributes
            final Method getAttributesMethod = fileSystemClass.getDeclaredMethod(methodName, File.class);
            getAttributesMethod.setAccessible(true);
            final File f = new File(filePath);
            try {
                final int attributes = ((Integer) getAttributesMethod.invoke(fileSystem, f)).intValue();
                this.toPush = state.getCalculator().valInt(attributes);
            } catch (InvocationTargetException e) {
                final String cause = internalClassName(e.getCause().getClass().getName());
                throwNew(state, cause);
                exitFromAlgorithm();
            }            
        } catch (ClassCastException e) {
            throwVerifyError(state);
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
