package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
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
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.io.UnixFileSystem#canonicalize0(String)} and
 * {@link java.io.WinNTFileSystem#canonicalize0(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_XFILESYSTEM_CANONICALIZE0 extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete toPush; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the filesystem object and its class
            final Field fileSystemField = File.class.getDeclaredField("fs");
            fileSystemField.setAccessible(true);
            final Object fileSystem = fileSystemField.get(null);
            final Class<?> fileSystemClass = fileSystem.getClass();
            final String methodName = "canonicalize0";

            //gets the (String path) parameter
            final Reference pathReference = (Reference) this.data.operand(1);
            if (state.isNull(pathReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final String path = valueString(state, pathReference);
            if (path == null) {
                throw new SymbolicValueNotAllowedException("The String parameter to invocation of method " + fileSystemClass.getName() + "." + methodName + "  is not a simple String.");
            }
            
            //invokes metacircularly the canonicalize0
            //method to obtain the canonical path
            final Method method = fileSystemClass.getDeclaredMethod(methodName, String.class);
            method.setAccessible(true);
            String pathCanonical = null; //to keep the compiler happy
            try {
                pathCanonical = (String) method.invoke(fileSystem, path);
            } catch (InvocationTargetException e) {
                final String cause = internalClassName(e.getCause().getClass().getName());
                throwNew(state, calc, cause);
                exitFromAlgorithm();
            }
            
            //adds a String to the state for the canonical path
            if (pathCanonical == null) {
                this.toPush = Null.getInstance();
            } else {
                state.ensureStringLiteral(calc, pathCanonical);
                this.toPush = state.referenceToStringLiteral(pathCanonical);
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
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
