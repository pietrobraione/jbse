package jbse.algo;

import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_DECLARINGCLASS;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_FILENAME;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_LINENUMBER;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_METHODNAME;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;
import static jbse.bc.Signatures.VERIFY_ERROR;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Frame;
import jbse.mem.Instance;
import jbse.mem.SnippetFrameNoWrap;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

/**
 * Utility class containing methods that deal with control flow
 * of {@link Algorithm} execution, either at meta level (interruption
 * of execution of an algorithm, continuation...) or at base level
 * (unwinding of stack for exceptions). 
 * 
 * @author Pietro Braione
 *
 */
public final class UtilControlFlow {
    /**
     * Abruptly interrupts the execution of JBSE
     * in the case of an unexpected internal error.
     * 
     * @param e an {@code Exception}, the cause of
     *        the internal error. 
     */
    public static void failExecution(Exception e) {
        throw new UnexpectedInternalException(e);
    }

    /**
     * Abruptly interrupts the execution of JBSE
     * in the case of an unexpected internal error.
     * 
     * @param m a {@code String}, the cause of
     *        the internal error. 
     */
    public static void failExecution(String s) {
        throw new UnexpectedInternalException(s);
    }

    /**
     * Cleanly interrupts the execution of an {@link Algorithm}.
     */
    public static void exitFromAlgorithm() throws InterruptException {
        throw InterruptException.mk();
    }

    /**
     * Cleanly interrupts the execution of an {@link Action}, 
     * and schedules another one as the next to be executed.
     * 
     * @param algo the next {@link Action} to be executed.
     */
    public static void continueWith(Action act)
    throws InterruptException {
        throw InterruptException.mk(act);
    }

    /**
     * Cleanly interrupts the execution of an invoke* 
     * bytecode and schedules the base-level 
     * implementation of the method for execution. 
     */
    public static void continueWithBaseLevelImpl(State state, boolean isInterface, boolean isSpecial, boolean isStatic) 
    throws InterruptException {
        final Algo_INVOKEX_Completion continuation = 
            new Algo_INVOKEX_Completion(isInterface, isSpecial, isStatic);
        continuation.setProgramCounterOffset(offsetInvoke(isInterface));
        continuation.shouldFindImplementation();
        continueWith(continuation);
    }
    

    /**
     * Equivalent to 
     * {@link #throwNew}{@code (state, "java/lang/VerifyError")}.
     * 
     * @param state the {@link State} whose {@link Heap} will receive 
     *              the new object.
     * @param calc a {@link Calculator}.
     * @throws ClasspathException if the class file for {@code java.lang.VerifyError}
     *         is not in the classpath, or is ill-formed, or cannot access one of its
     *         superclasses/superinterfaces.
     */
    public static void throwVerifyError(State state, Calculator calc) throws ClasspathException {
        try {
            final ClassFile cf_VERIFY_ERROR = state.getClassHierarchy().loadCreateClass(VERIFY_ERROR);
            if (cf_VERIFY_ERROR == null) {
                failExecution("Could not find class java.lang.VerifyError.");
            }
            final ReferenceConcrete excReference = state.createInstanceSurely(calc, cf_VERIFY_ERROR);
            fillExceptionBacktrace(state, calc, excReference);
            state.unwindStack(excReference);
        } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | ClassFileNotAccessibleException e) {
            throw new ClasspathException(e);
        } catch (RenameUnsupportedException | InvalidInputException | InvalidIndexException | 
        		 InvalidProgramCounterException e) {
            //there is not much we can do if this happens
            failExecution(e);
        }
    }

    /**
     * Creates a new instance of a given object in the 
     * heap of a state. The fields of the object are initialized 
     * with the default values for each field's type. Then, unwinds 
     * the stack of the state in search for an exception handler for
     * the object. The procedure aims to be fail-safe w.r.t 
     * errors in the classfile.
     * 
     * @param state the {@link State} where the new object will be 
     *        created and whose stack will be unwound.
     * @param calc a {@link Calculator}.
     * @param toThrowClassName the name of the class of the new instance
     *        to throw. It must be a {@link Throwable} defined in the standard
     *        library and available in the bootstrap classpath.
     * @throws ClasspathException  if the classfile for {@code toThrowClassName}
     *         is missing or is ill-formed.
     */
    public static void throwNew(State state, Calculator calc, String toThrowClassName) throws ClasspathException {
        if (toThrowClassName.equals(VERIFY_ERROR)) {
            throwVerifyError(state, calc);
            return;
        }
        try {
            final ClassFile exceptionClass = state.getClassHierarchy().loadCreateClass(toThrowClassName);
            final ReferenceConcrete excReference = state.createInstanceSurely(calc, exceptionClass);
            fillExceptionBacktrace(state, calc, excReference);
            throwObject(state, calc, excReference);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                 BadClassFileVersionException | WrongClassNameException e) {
            throw new ClasspathException(e);
        } catch (RenameUnsupportedException | IncompatibleClassFileException | ClassFileNotAccessibleException | 
        		 InvalidInputException e) {
            //there is not much we can do if this happens
            failExecution(e);
        }
    }

    /**
     * Unwinds the stack of a state until it finds an exception 
     * handler for an object. This procedure aims to wrap 
     * {@link State#unwindStack(Reference)} with a fail-safe  
     * interface to errors in the classfile.
     * 
     * @param state the {@link State} where the new object will be 
     *        created and whose stack will be unwound.
     * @param calc a {@link Calculator}.
     * @param toThrow see {@link State#unwindStack(Reference)}.
     * @throws InvalidInputException if {@code toThrow} is an unresolved symbolic reference, 
     *         or is a null reference, or is a reference to an object that does not extend {@code java.lang.Throwable}.
     * @throws ClasspathException if the class file for {@code java.lang.VerifyError}
     *         is not in the classpath, or is ill-formed, or cannot access one of its
     *         superclasses/superinterfaces.
     */
    public static void throwObject(State state, Calculator calc, Reference toThrow) 
    throws InvalidInputException, ClasspathException {
        try {
            state.unwindStack(toThrow);
        } catch (InvalidIndexException | InvalidProgramCounterException e) {
            throwVerifyError(state, calc); //TODO that's desperate
        }
    }

    /**
     * Sets the {@code backtrace} and {@code stackTrace} fields 
     * of an exception {@link Instance} to their initial values.
     * This method is low-level, in that it does <em>not</em> 
     * initialize statically (i.e., create the {@code <clinit>} frames)
     * the classes involved in the backtrace creation. This way it
     * can be used in hostile contexts where it is impractical or
     * impossible to initialize statically the classes without 
     * creating races.
     * 
     * @param state a {@link State}. The backtrace will be created 
     *        in the heap of {@code state}.
     * @param calc a {@link Calculator}.
     * @param exc a {@link Reference} to the exception {@link Instance} 
     *        whose {@code backtrace} and {@code stackTrace}
     *        fields must be set.
     */
    public static void fillExceptionBacktrace(State state, Calculator calc, Reference excReference) {
        try {
            final Instance exc = (Instance) state.getObject(excReference);
            exc.setFieldValue(JAVA_THROWABLE_STACKTRACE, Null.getInstance());
            final ClassFile excClass = exc.getType();
            int stackDepth = 0;
            for (Frame f : state.getStack()) {
                if (f instanceof SnippetFrameNoWrap) {
                    continue; //skips
                }
                final ClassFile fClass = f.getMethodClass();
                final String methodName = f.getMethodSignature().getName();
                if (excClass.equals(fClass) && "<init>".equals(methodName)) {
                    break;
                }
                ++stackDepth;
            }
            final ClassFile cf_JAVA_STACKTRACEELEMENT = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STACKTRACEELEMENT); //surely loaded
            if (cf_JAVA_STACKTRACEELEMENT == null) {
                failExecution("Could not find classfile for java.lang.StackTraceElement.");
            }
            final ClassFile cf_arrayJAVA_STACKTRACEELEMENT = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, "" + ARRAYOF + REFERENCE + JAVA_STACKTRACEELEMENT + TYPEEND); //surely loaded
            if (cf_arrayJAVA_STACKTRACEELEMENT == null) {
                failExecution("Could not find classfile for java.lang.StackTraceElement[].");
            }
            final ReferenceConcrete refToArray = 
                state.createArray(calc, null, calc.valInt(stackDepth), cf_arrayJAVA_STACKTRACEELEMENT);
            final Array theArray = (Array) state.getObject(refToArray);
            exc.setFieldValue(JAVA_THROWABLE_BACKTRACE, refToArray);
            int i = stackDepth - 1;
            for (Frame f : state.getStack()) {
                if (f instanceof SnippetFrameNoWrap) {
                    continue; //skips
                }
                
                final ClassFile currentClass = f.getMethodClass();

                //gets the data
                final String declaringClass = currentClass.getClassName().replace('/', '.').replace('$', '.'); //TODO is it ok?
                final String fileName       = currentClass.getSourceFile();
                final int    lineNumber     = f.getSourceRow(); 
                final String methodName     = f.getMethodSignature().getName();

                //break if we reach the first frame for the exception <init>
                if (excClass.equals(currentClass) && "<init>".equals(methodName)) {
                    break;
                }

                //creates the string literals
                state.ensureStringLiteral(calc, declaringClass);
                state.ensureStringLiteral(calc, fileName);
                state.ensureStringLiteral(calc, methodName);

                //creates the java.lang.StackTraceElement object and fills it
                final ReferenceConcrete steReference = state.createInstance(calc, cf_JAVA_STACKTRACEELEMENT);
                final Instance stackTraceElement = (Instance) state.getObject(steReference);
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_DECLARINGCLASS, state.referenceToStringLiteral(declaringClass));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_FILENAME,       state.referenceToStringLiteral(fileName));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_LINENUMBER,     calc.valInt(lineNumber));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_METHODNAME,     state.referenceToStringLiteral(methodName));

                //sets the array
                theArray.setFast(calc.valInt(i--), steReference);
            }
        } catch (HeapMemoryExhaustedException e) {
            //just gives up
            return;
        } catch (ClassCastException | InvalidInputException |
                 InvalidTypeException |  FastArrayAccessNotAllowedException e) {
            //this should not happen (and if happens there is not much we can do)
            failExecution(e);
        }
    }
    
	/** Do not instantiate! */
	private UtilControlFlow() {
		//nothing to do
	}
}
