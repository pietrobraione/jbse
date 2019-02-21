package jbse.algo;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_LOADCLASS;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_DECLARINGCLASS;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_FILENAME;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_LINENUMBER;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT_METHODNAME;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_MAKEKLASSSYMBOLIC;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.VERIFY_ERROR;
import static jbse.bc.Signatures.noclass_REGISTERLOADEDCLASS;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Frame;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.SnippetFrameNoWrap;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Util {
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
     * Cleanly interrupts the execution of an {@link Algorithm}, 
     * and schedules another one as the next to be executed.
     * 
     * @param algo the next {@link Algorithm} to be executed.
     */
    public static void continueWith(Action... act)
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
     * Performs lookup of a method implementation (bytecode or native).
     * See JVMS v8, invokeinterface, invokespecial, invokestatic and invokevirtual
     * bytecodes specification.
     * 
     * @param state a {@link State}
     * @param resolutionClass the {@link ClassFile} of the resolved method.
     * @param methodSignature the {@link Signature} of the method
     *        whose implementation must be looked up.
     * @param isInterface {@code true} iff the method is declared interface.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param isStatic {@code true} iff the method is declared static.
     * @param receiverClass a {@link ClassFile}, the class of the receiver
     *        of the method invocation. It can be {@code null} when 
     *        {@code isStatic == true}.
     * @return the {@link ClassFile} of the class which contains the method implementation.
     * @throws InvalidInputException if {@code state == null || resolutionClass == null || 
     *         methodSignature == null || (!isStatic && !isSpecial && receiverClass == null)}.
     * @throws FrozenStateException if {@code state} is frozen.
     * @throws MethodNotFoundException if lookup fails and {@link java.lang.NoSuchMethodError} should be thrown.
     * @throws MethodNotAccessibleException  if lookup fails and {@link java.lang.IllegalAccessError} should be thrown.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     * @throws ThreadStackEmptyException if {@code state} has an empty stack (i.e., no
     *         current method).
     */
    public static ClassFile lookupMethodImpl(State state, ClassFile resolutionClass, Signature methodSignature, boolean isInterface, boolean isSpecial, boolean isStatic, ClassFile receiverClass) 
    throws InvalidInputException, FrozenStateException, MethodNotFoundException, MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException, ThreadStackEmptyException {
    	if (state == null || resolutionClass == null || methodSignature == null || (!isStatic && !isSpecial && receiverClass == null)) {
    		throw new InvalidInputException("Invoked " + Util.class.getName() + ".lookupMethodImpl with a null parameter.");
    	}
        final ClassFile retVal;
        final ClassHierarchy hier = state.getClassHierarchy();
        if (isInterface) { 
            retVal = hier.lookupMethodImplInterface(receiverClass, resolutionClass, methodSignature);
        } else if (isSpecial) {
            final ClassFile currentClass = state.getCurrentClass();
            retVal = hier.lookupMethodImplSpecial(currentClass, resolutionClass, methodSignature);
        } else if (isStatic) {
            retVal = hier.lookupMethodImplStatic(resolutionClass, methodSignature);
        } else { //invokevirtual
            retVal = hier.lookupMethodImplVirtual(receiverClass, resolutionClass, methodSignature);
        }
        //TODO invokedynamic
        return retVal;
    }

    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param ref a {@link Reference}.
     * @return a {@link String} corresponding to the value of 
     *         the string {@link Instance} referred by {@code ref}, 
     *         or {@code null} if {@code ref} does not refer an {@link Instance} 
     *         in {@code s}, or if it refers an {@link Instance} but its 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or its value
     *         is not a concrete array of {@code char}s.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public static String valueString(State s, Reference ref) throws FrozenStateException {
        final Instance i;
        try {
            i = (Instance) s.getObject(ref);
        } catch (ClassCastException e) {
            return null;
        }
        return valueString(s, i);
    }
    
    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param i an {@link Instance}.
     * @return a {@link String} corresponding to the {@code value} of 
     *         the {@code i}, 
     *         or {@code null} if such {@link Instance}'s 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or its value
     *         is not a simple array of {@code char}s.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public static String valueString(State s, Instance i) throws FrozenStateException {
        final ClassFile cf_JAVA_STRING = s.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING);
        if (cf_JAVA_STRING == null) {
            failExecution("Could not find class java.lang.String.");
        }
        if (cf_JAVA_STRING.equals(i.getType())) {
            final Reference valueRef = (Reference) i.getFieldValue(JAVA_STRING_VALUE);
            final Array value = (Array) s.getObject(valueRef);
            if (value == null) {
                //this happens when valueRef is symbolic and unresolved
                return null;
            }
            return value.valueString();
        } else {
            return null;
        }
    }

    /**
     * Equivalent to 
     * {@link #throwNew}{@code (state, "java/lang/VerifyError")}.
     * 
     * @param state the {@link State} whose {@link Heap} will receive 
     *              the new object.
     * @throws ClasspathException if the class file for {@code java.lang.VerifyError}
     *         is not in the classpath, or is ill-formed, or cannot access one of its
     *         superclasses/superinterfaces.
     */
    public static void throwVerifyError(State state) throws ClasspathException {
        try {
            final ClassFile cf_VERIFY_ERROR = state.getClassHierarchy().loadCreateClass(VERIFY_ERROR);
            if (cf_VERIFY_ERROR == null) {
                failExecution("Could not find class java.lang.VerifyError.");
            }
            final ReferenceConcrete excReference = state.createInstanceSurely(cf_VERIFY_ERROR);
            fillExceptionBacktrace(state, excReference);
            state.unwindStack(excReference);
        } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | ClassFileNotAccessibleException e) {
            throw new ClasspathException(e);
        } catch (InvalidInputException | InvalidIndexException | InvalidProgramCounterException e) {
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
     * @param toThrowClassName the name of the class of the new instance
     *        to throw. It must be a {@link Throwable} defined in the standard
     *        library and available in the bootstrap classpath.
     * @throws ClasspathException  if the classfile for {@code toThrowClassName}
     *         is missing or is ill-formed.
     */
    public static void throwNew(State state, String toThrowClassName) throws ClasspathException {
        if (toThrowClassName.equals(VERIFY_ERROR)) {
            throwVerifyError(state);
            return;
        }
        try {
            final ClassFile exceptionClass = state.getClassHierarchy().loadCreateClass(toThrowClassName);
            final ReferenceConcrete excReference = state.createInstanceSurely(exceptionClass);
            fillExceptionBacktrace(state, excReference);
            throwObject(state, excReference);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                 BadClassFileVersionException | WrongClassNameException e) {
            throw new ClasspathException(e);
        } catch (IncompatibleClassFileException | ClassFileNotAccessibleException | InvalidInputException e) {
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
     * @param toThrow see {@link State#unwindStack(Reference)}.
     * @throws InvalidInputException if {@code toThrow} is an unresolved symbolic reference, 
     *         or is a null reference, or is a reference to an object that does not extend {@code java.lang.Throwable}.
     * @throws ClasspathException if the class file for {@code java.lang.VerifyError}
     *         is not in the classpath, or is ill-formed, or cannot access one of its
     *         superclasses/superinterfaces.
     */
    public static void throwObject(State state, Reference toThrow) 
    throws InvalidInputException, ClasspathException {
        try {
            state.unwindStack(toThrow);
        } catch (InvalidIndexException | InvalidProgramCounterException e) {
            throwVerifyError(state); //TODO that's desperate
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
     * @param exc a {@link Reference} to the exception {@link Instance} 
     *        whose {@code backtrace} and {@code stackTrace}
     *        fields must be set.
     */
    public static void fillExceptionBacktrace(State state, Reference excReference) {
        try {
            final Instance exc = (Instance) state.getObject(excReference);
            exc.setFieldValue(JAVA_THROWABLE_STACKTRACE, Null.getInstance());
            final ClassFile excClass = exc.getType();
            int stackDepth = 0;
            for (Frame f : state.getStack()) {
                if (f instanceof SnippetFrameNoWrap) {
                    continue; //skips
                }
                final ClassFile fClass = f.getCurrentClass();
                final String methodName = f.getCurrentMethodSignature().getName();
                if (excClass.equals(fClass) && "<init>".equals(methodName)) {
                    break;
                }
                ++stackDepth;
            }
            final ClassFile cf_JAVA_STACKTRACEELEMENT = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STACKTRACEELEMENT);
            if (cf_JAVA_STACKTRACEELEMENT == null) {
                failExecution("Could not find classfile for java.lang.StackTraceElement.");
            }
            final ClassFile cf_arrayJAVA_STACKTRACEELEMENT = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, "" + ARRAYOF + REFERENCE + JAVA_STACKTRACEELEMENT + TYPEEND);
            if (cf_arrayJAVA_STACKTRACEELEMENT == null) {
                failExecution("Could not find classfile for java.lang.StackTraceElement[].");
            }
            final ReferenceConcrete refToArray = 
                state.createArray(null, state.getCalculator().valInt(stackDepth), cf_arrayJAVA_STACKTRACEELEMENT);
            final Array theArray = (Array) state.getObject(refToArray);
            exc.setFieldValue(JAVA_THROWABLE_BACKTRACE, refToArray);
            int i = 0;
            final Calculator calc = state.getCalculator();
            for (Frame f : state.getStack()) {
                if (f instanceof SnippetFrameNoWrap) {
                    continue; //skips
                }
                
                final ClassFile currentClass = f.getCurrentClass();

                //gets the data
                final String declaringClass = currentClass.getClassName().replace('/', '.').replace('$', '.'); //TODO is it ok?
                final String fileName       = currentClass.getSourceFile();
                final int    lineNumber     = f.getSourceRow(); 
                final String methodName     = f.getCurrentMethodSignature().getName();

                //break if we reach the first frame for the exception <init>
                if (excClass.equals(currentClass) && "<init>".equals(methodName)) {
                    break;
                }

                //creates the string literals
                state.ensureStringLiteral(declaringClass);
                state.ensureStringLiteral(fileName);
                state.ensureStringLiteral(methodName);

                //creates the java.lang.StackTraceElement object and fills it
                final ReferenceConcrete steReference = state.createInstance(cf_JAVA_STACKTRACEELEMENT);
                final Instance stackTraceElement = (Instance) state.getObject(steReference);
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_DECLARINGCLASS, state.referenceToStringLiteral(declaringClass));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_FILENAME,       state.referenceToStringLiteral(fileName));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_LINENUMBER,     calc.valInt(lineNumber));
                stackTraceElement.setFieldValue(JAVA_STACKTRACEELEMENT_METHODNAME,     state.referenceToStringLiteral(methodName));

                //sets the array
                theArray.setFast(calc.valInt(i++), steReference);
            }
        } catch (HeapMemoryExhaustedException e) {
            //just gives up
            return;
        } catch (ClassCastException | InvalidInputException |
                 InvalidTypeException | InvalidOperandException | 
                 FastArrayAccessNotAllowedException e) {
            //this should not happen (and if happens there is not much we can do)
            failExecution(e);
        }
    }

    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store for a class, possibly creating the necessary
     * frames for the {@code <clinit>} methods to initialize it, 
     * or initializing it symbolically. If necessary it also recursively 
     * initializes its superclasses. It is equivalent
     * to {@link #ensureClassInitialized(State, ClassFile, ExecutionContext, Set, Signature) ensureClassInitialized}
     * {@code (state, classFile, ctx, null, null)}.
     * 
     * @param state a {@link State}. It must have a current frame.
     * @param classFile a {@link ClassFile} for the class which must
     *        be initialized.
     * @param ctx an {@link ExecutionContext}.
     * @throws InvalidInputException if {@code classFile} or {@code state} 
     *         is null.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code classFile} is or is not initialized.
     * @throws ClasspathException if some standard JRE class is missing
     *         from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE. 
     * @throws HeapMemoryExhaustedException if during class creation
     *         and initialization the heap memory ends.
     * @throws InterruptException iff it is necessary to interrupt the
     *         execution of the bytecode, to run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es) or because of heap memory exhaustion.
     * @throws ContradictionException if some initialization assumption is
     *         contradicted.
     */
    public static void ensureClassInitialized(State state, ClassFile classFile, ExecutionContext ctx)
    throws InvalidInputException, DecisionException, 
    ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        try {
            ensureClassInitialized(state, classFile, ctx, null, null);
        } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store for a class, possibly creating the necessary
     * frames for the {@code <clinit>} methods to initialize it, 
     * or initializing it symbolically. If necessary it also recursively 
     * initializes its superclasses. It is equivalent
     * to {@link #ensureClassInitialized(State, ClassFile, ExecutionContext, Set, Signature) ensureClassInitialized}
     * {@code (state, classFile, ctx, null, boxExceptionMethodSignature)}.
     * 
     * @param state a {@link State}. It must have a current frame.
     * @param classFile a {@link ClassFile} for the class which must
     *        be initialized.
     * @param ctx an {@link ExecutionContext}.
     * @param boxExceptionMethodSignature a {@link Signature} for a method in
     *        {@link jbse.base.Base} that boxes exceptions thrown by the initializer
     *        methods, or {@code null} if no boxing must be performed. The class
     *        name in the signature is not considered.
     * @throws InvalidInputException if {@code classFile} or {@code state} 
     *         is null.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code classFile} is or is not initialized.
     * @throws ClasspathException if some standard JRE class is missing
     *         from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE. 
     * @throws HeapMemoryExhaustedException if during class creation
     *         and initialization the heap memory ends.
     * @throws InterruptException iff it is necessary to interrupt the
     *         execution of the bytecode, to run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es) or because of heap memory exhaustion.
     * @throws ContradictionException  if some initialization assumption is
     *         contradicted.
     */
    public static void ensureClassInitialized(State state, ClassFile classFile, ExecutionContext ctx, Signature boxExceptionMethodSignature)
    throws InvalidInputException, DecisionException, 
    ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        try {
            ensureClassInitialized(state, classFile, ctx, null, boxExceptionMethodSignature);
        } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store for a class, possibly creating the necessary
     * frames for the {@code <clinit>} methods to initialize it, 
     * or initializing it symbolically. If necessary it also recursively 
     * initializes its superclasses. It is equivalent
     * to {@link #ensureClassInitialized(State, String, ExecutionContext, Set, Signature) ensureClassInitialized}
     * {@code (state, classFile, ctx, skip, null)}.
     * 
     * @param state a {@link State}. It must have a current frame.
     * @param classFile a {@link ClassFile} for the class which must
     *        be initialized.
     * @param ctx an {@link ExecutionContext}.
     * @param skip a {@link Set}{@code <}{@link String}{@code >}.
     *        All the classes (and their superclasses and superinterfaces recursively) 
     *        whose names are in this set will not be created. A {@code null} value
     *        is equivalent to the empty set. All the classes must be in the bootstrap
     *        classpath and will be loaded with the bootstrap classloader.
     * @throws InvalidInputException if {@code classFile} or {@code state} 
     *         is null.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code classFile} is or is not initialized.
     * @throws ClasspathException if some standard JRE class is missing
     *         from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE. 
     * @throws HeapMemoryExhaustedException if during class creation
     *         and initialization the heap memory ends.
     * @throws InterruptException iff it is necessary to interrupt the
     *         execution of the bytecode, to run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es) or because of heap memory exhaustion.
     * @throws ClassFileNotFoundException if some class in {@code skip} does not exist
     *         in the bootstrap classpath.
     * @throws IncompatibleClassFileException if the superclass for some class in {@code skip} is 
     *         resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileIllFormedException if some class in {@code skip} is ill-formed.
     * @throws BadClassFileVersionException if some class in {@code skip} has a version number
     *         that is unsupported by this version of JBSE.
     * @throws WrongClassNameException if the bytecode of some class in {@code skip} has a name
     *         that is different from what expected (the corresponding name in {@code skip}).
     * @throws ClassFileNotAccessibleException if some class in {@code skip} has
     *         a superclass/superinterface that it cannot access.
     * @throws ContradictionException  if some initialization assumption is
     *         contradicted.
     */
    public static void ensureClassInitialized(State state, ClassFile classFile, ExecutionContext ctx, Set<String> skip)
    throws InvalidInputException, DecisionException, ClasspathException, HeapMemoryExhaustedException, 
    InterruptException, ClassFileNotFoundException, IncompatibleClassFileException, ClassFileIllFormedException, 
    BadClassFileVersionException, WrongClassNameException, ClassFileNotAccessibleException, ContradictionException {
        ensureClassInitialized(state, classFile, ctx, skip, null);
    }
    
    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store for a class, possibly creating the necessary
     * frames for the {@code <clinit>} methods to initialize it, 
     * or initializing it symbolically. If necessary it also recursively 
     * initializes its superclasses.
     * 
     * @param state a {@link State}. It must have a current frame.
     * @param classFile a {@link ClassFile} for the class which must
     *        be initialized.
     * @param ctx an {@link ExecutionContext}.
     * @param skip a {@link Set}{@code <}{@link String}{@code >}.
     *        All the classes (and their superclasses and superinterfaces recursively) 
     *        whose names are in this set will not be created. A {@code null} value
     *        is equivalent to the empty set. All the classes must be in the bootstrap
     *        classpath and will be loaded with the bootstrap classloader.
     * @param boxExceptionMethodSignature a {@link Signature} for a method in
     *        {@link jbse.base.Base} that boxes exceptions thrown by the initializer
     *        methods, or {@code null} if no boxing must be performed. The class
     *        name in the signature is not considered.
     * @throws InvalidInputException if {@code classFile} or {@code state} 
     *         is null.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code classFile} is or is not initialized.
     * @throws ClasspathException if some standard JRE class is missing
     *         from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE. 
     * @throws HeapMemoryExhaustedException if during class creation
     *         and initialization the heap memory ends.
     * @throws InterruptException iff it is necessary to interrupt the
     *         execution of the bytecode, to run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es) or because of heap memory exhaustion.
     * @throws ClassFileNotFoundException if some class in {@code skip} does not exist
     *         in the bootstrap classpath.
     * @throws IncompatibleClassFileException if the superclass for some class in {@code skip} is 
     *         resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileIllFormedException if some class in {@code skip} is ill-formed.
     * @throws BadClassFileVersionException if some class in {@code skip} has a version number
     *         that is unsupported by this version of JBSE.
     * @throws WrongClassNameException if the bytecode of some class in {@code skip} has a name
     *         that is different from what expected (the corresponding name in {@code skip}).
     * @throws ClassFileNotAccessibleException if some class in {@code skip} has
     *         a superclass/superinterface that it cannot access.
     * @throws ContradictionException  if some initialization assumption is
     *         contradicted.
     */
    public static void ensureClassInitialized(State state, ClassFile classFile, ExecutionContext ctx, Set<String> skip, Signature boxExceptionMethodSignature) 
    throws InvalidInputException, DecisionException, ClasspathException, HeapMemoryExhaustedException, InterruptException, 
    ClassFileNotFoundException, IncompatibleClassFileException, ClassFileIllFormedException, 
    BadClassFileVersionException, WrongClassNameException, ClassFileNotAccessibleException, 
    ContradictionException {
        final Set<String> _skip = (skip == null) ? new HashSet<>() : skip; //null safety
        final ClassInitializer ci = new ClassInitializer(state, ctx, _skip, boxExceptionMethodSignature, ctx.getMakePreInitClassesSymbolic());
        final boolean failed = ci.initialize(classFile);
        if (failed) {
            return;
        }
        if (ci.createdFrames > 0) {
            exitFromAlgorithm(); //time to execute <clinit>s
        }
    }

    private static class ClassInitializer {
        /**
         * The current state.
         */
        private final State s;

        /**
         * The decision procedure.
         */
        private final ExecutionContext ctx;
        
        /**
         * The classes whose creation must be skipped.
         */
        private final Set<String> skip;

        /** 
         * The signature of the method that boxes exception, or null if exceptions
         * shall not be boxed.
         */
        private final Signature boxExceptionMethodSignature;
        
        /**
         * Whether all the classes created during
         * the pre-inizialization phase shall be made 
         * symbolic. 
         */
        private final boolean makePreInitClassesSymbolic;
        
        /**
         * Counts the number of frames created during class initialization. 
         * Used in case {@link #initializeClass} fails to restore the stack.
         * Its value is used only in the context of an {@link #initializeClass} call, 
         * and is not reused across multiple calls.
         */
        private int createdFrames = 0;

        /**
         * Stores the classes for which this initializer has created a {@link Klass},
         * and that therefore must be processed during phase 2 (creation of 
         * constant values).
         */
        private final ArrayList<ClassFile> classesForPhase2 = new ArrayList<>();

        /**
         * Stores the classes that are assumed to be initialized
         * before the start of symbolic execution (if their static
         * initialized is run, then the created Klass object must
         * be made symbolic).
         */
        private final HashSet<ClassFile> preInitializedClasses = new HashSet<>();

        /**
         * Stores the classes for which the {@code <clinit>} 
         * method must be run, and that therefore must be processed
         * during phase 3 (creation of {@code <clinit>} frames).
         */
        private final ArrayList<ClassFile> classesForPhase3 = new ArrayList<>();

        /**
         * Set to {@code true} iff must load a frame for {@code java.lang.Object}'s 
         * {@code <clinit>}.
         */
        private boolean pushClinitFor_JAVA_OBJECT = false;

        /** Is the initialization process failed? */
        private boolean failed = false;

        /** What is the cause of the failure? (meaningless if failed == false) */
        private String failure = null;
        
        /** ClassFile for jbse.base.Base. */
        private ClassFile cf_JBSE_BASE;

        private ClassInitializer(State s, ExecutionContext ctx, Set<String> skip, Signature boxExceptionMethodSignature, boolean makePreInitClassesSymbolic) 
        throws InvalidInputException, ClassFileNotFoundException, IncompatibleClassFileException, 
        ClassFileIllFormedException, BadClassFileVersionException, WrongClassNameException, ClassFileNotAccessibleException {
            this.s = s;
            this.ctx = ctx;
            this.boxExceptionMethodSignature = boxExceptionMethodSignature;
            this.makePreInitClassesSymbolic = makePreInitClassesSymbolic;
            
            //closes skip w.r.t. superclasses
            this.skip = new HashSet<>();
            final ClassHierarchy hier = this.s.getClassHierarchy();
            for (String className : skip) {
                this.skip.add(className);
                final ClassFile  classFile = hier.loadCreateClass(className);
                for (ClassFile superClass : classFile.superclasses()) {
                    this.skip.add(superClass.getClassName());
                }
                for (ClassFile superInterface : classFile.superinterfaces()) {
                    this.skip.add(superInterface.getClassName());
                }
            }
            
            //gets classfile for jbse.base.Base and checks the method
            try {
                this.cf_JBSE_BASE = s.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, JBSE_BASE, true);
            } catch (ClassFileNotFoundException | IncompatibleClassFileException | ClassFileIllFormedException | 
                     ClassFileNotAccessibleException | PleaseLoadClassException e) {
                //this should never happen
                failExecution("Could not find classfile for loaded class jbse.base.Base, or the classfile is ill-formed.");
            }
            if (this.boxExceptionMethodSignature != null && !this.cf_JBSE_BASE.hasMethodImplementation(this.boxExceptionMethodSignature)) {
                throw new InvalidInputException("Could not find implementation of exception boxing method " + this.boxExceptionMethodSignature.toString() + ".");
            }
        }

        /**
         * Implements {@link Util#ensureClassInitialized(State, ClassFile, ExecutionContext, Set)}.
         * 
         * @param classFile the {@link ClassFile} of the class to be initialized.
         * @return {@code true} iff the initialization of 
         *         the class or of one of its superclasses 
         *         fails for some reason.
         * @throws InvalidInputException if {@code classFile} is null.
         * @throws DecisionException if the decision procedure fails.
         * @throws ClasspathException if the classfile for some JRE class
         *         is not in the classpath or is incompatible with the
         *         current version of JBSE.
         * @throws HeapMemoryExhaustedException if heap memory ends while
         *         performing class initialization
         * @throws ContradictionException  if some initialization assumption is
         *         contradicted.
         */
        private boolean initialize(ClassFile classFile)
        throws InvalidInputException, DecisionException, 
        ClasspathException, HeapMemoryExhaustedException, ContradictionException {
            phase1(classFile, false);
            if (this.failed) {
                revert();
                return true;
            }
            phase2();
            if (this.failed) {
                revert();
                return true;
            }
            phase3();
            if (this.failed) {
                revert();
                return true;
            }
            return false;
        }
        
        /**
         * Returns an {@link Iterable} that scans a {@link List} in 
         * reverse order, from tail to head.
         * 
         * @param list a {@link List}{@code <T>}. It must not be {@code null}.
         * @return an {@link Iterable}{@code <T>}.
         */
        private static <T> Iterable<T> reverse(final List<T> list) {
            return new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return new Iterator<T>() {
                        private ListIterator<T> delegate = list.listIterator(list.size());

                        @Override
                        public boolean hasNext() {
                            return this.delegate.hasPrevious();
                        }

                        @Override
                        public T next() {
                            return this.delegate.previous();
                        }

                        @Override
                        public void remove() {
                            this.delegate.remove();
                        }
                    };
                }
            };
        }

        private boolean hasANonStaticImplementedMethod(ClassFile cf) {
            final Signature[] methods = cf.getDeclaredMethods();
            for (Signature method : methods) {
                try {
                    if (!cf.isMethodAbstract(method) && !cf.isMethodStatic(method)) {
                        return true;
                    }
                } catch (MethodNotFoundException e) {
                    //this should never happen
                    failExecution(e);
                }
            }
            return false;
        }

        /**
         * Phase 1 creates all the {@link Klass} objects for a class and its
         * superclasses that can be assumed to be not initialized. It also 
         * refines the path condition by adding all the initialization assumptions.
         * 
         * @param classFile the {@link ClassFile} of the class to be initialized.
         * @param recurSuperinterfaces if {@code true}, recurs phase 1 over
         *        {@code classFile}'s superinterfaces even if 
         *        {@code classFile.}{@link ClassFile#isInterface() isInterface}{@code () == true}.
         * @throws InvalidInputException if {@code classFile} is null.
         * @throws DecisionException if the decision procedure fails.
         * @throws ContradictionException  if some initialization assumption is
         *         contradicted.
         */
        private void phase1(ClassFile classFile, boolean recurSuperinterfaces)
        throws InvalidInputException, DecisionException, ContradictionException {
            //if there is a Klass object for className (means 
            //initialization in progress or already initialized), 
            //or if className is in the skip set, there is 
            //nothing to do
            if (this.s.existsKlass(classFile) || this.skip.contains(classFile.getClassName())) {
                //if classFile was already in this.classesToPushClinit
                //we must reschedule it to respect the visiting order
                //of JVMS v8 section 5.5, point 7
                if (this.classesForPhase3.contains(classFile)) {
                    this.classesForPhase3.remove(classFile);
                    this.classesForPhase3.add(classFile);
                }
                return;
            }

            //saves classFile in the list of the newly
            //created Klasses
            this.classesForPhase2.add(classFile);
            
            try {
                //decides whether the class is pre-initialized and whether
                //a symbolic or concrete Klass object should be created
                //TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Withdraw this assumption and branch.
                final ClassHierarchy hier = this.s.getClassHierarchy();
                final boolean pure = classFile.isPure() || this.ctx.hasClassAPureInitializer(hier, classFile);
                final boolean createSymbolicKlass;
                final boolean assumeInitialized;
                if (this.s.phase() == Phase.PRE_INITIAL) {
                    //all pre-initial class are assumed to be pre-initialized
                    createSymbolicKlass = false;
                    assumeInitialized = true;
                } else if (this.ctx.decisionProcedure.isSatInitialized(classFile)) { 
                    createSymbolicKlass = !pure; //if pure, the static initializer will be executed
                    assumeInitialized = true;
                } else {
                    createSymbolicKlass = false;
                    assumeInitialized = false;
                }
                
                //creates the Klass object
                if (createSymbolicKlass) {
                    //creates a symbolic Klass
                    this.s.ensureKlassSymbolic(classFile);
                } else {
                    //creates a concrete Klass and schedules it for phase 3
                    this.s.ensureKlass(classFile);
                    if (JAVA_OBJECT.equals(classFile.getClassName())) {
                        this.pushClinitFor_JAVA_OBJECT = true;
                    } else {
                        this.classesForPhase3.add(classFile);
                    }
                }
                
                //pushes the assumption
                if (assumeInitialized) {
                    final Klass k = this.s.getKlass(classFile);
                    this.s.assumeClassInitialized(classFile, k);
                } else {
                    this.s.assumeClassNotInitialized(classFile);
                }

                //if the created Klass is concrete but 
                //the class is assumed to be pre-initialized, 
                //schedules the Klass to become symbolic (if
                //the corresponding flag is active)
                if (!createSymbolicKlass && assumeInitialized && this.makePreInitClassesSymbolic
                    && !JBSE_BASE.equals(classFile.getClassName()) /* HACK */) {
                    this.preInitializedClasses.add(classFile);
                }
            } catch (InvalidIndexException e) {
                this.failed = true;
                this.failure = VERIFY_ERROR;
                return;
            }

            //if classFile denotes a class rather than an interface
            //and has a superclass, then recursively performs phase1 
            //on its superclass and superinterfaces, according to
            //JVMS v8 section 5.5, point 7
            if (!classFile.isInterface() || recurSuperinterfaces) {
                for (ClassFile superinterface : reverse(classFile.getSuperInterfaces())) {
                    if (hasANonStaticImplementedMethod(classFile)) {
                        phase1(superinterface, true);
                    }
                }
                final ClassFile superclass = classFile.getSuperclass();
                if (superclass != null) {
                    phase1(superclass, false);
                }
            }
        }

        /**
         * Phase 2 inits the constant fields for all the {@link Klass} objects
         * created during phase 1; Note that we do not care about the initialization  
         * of the {@code java.lang.String} class if we meet some {@code String} constant, 
         * since the class is explicitly initialized by the init algorithm.
         * 
         * @throws DecisionException if the decision procedure fails.
         * @throws HeapMemoryExhaustedException if during phase 2 heap memory ends.
         * @throws FrozenStateException if {@code this.s} is frozen.
         */
        private void phase2() 
        throws DecisionException, HeapMemoryExhaustedException, FrozenStateException {
            final ListIterator<ClassFile> it = this.classesForPhase2.listIterator();
            while (it.hasNext()) {
                final ClassFile classFile = it.next();
                final Klass k = this.s.getKlass(classFile);
                final Signature[] flds = classFile.getDeclaredFieldsStatic();
                for (final Signature sig : flds) {
                    try {
                        if (classFile.isFieldConstant(sig)) {
                            //sig is directly extracted from the classfile, 
                            //so no resolution is necessary
                            Value v = null; //to keep the compiler happy
                            final ConstantPoolValue cpv = classFile.fieldConstantValue(sig);
                            if (cpv instanceof ConstantPoolPrimitive) {
                                v = s.getCalculator().val_(cpv.getValue());
                            } else if (cpv instanceof ConstantPoolString) {
                                final String stringLit = ((ConstantPoolString) cpv).getValue();
                                s.ensureStringLiteral(stringLit);
                                v = s.referenceToStringLiteral(stringLit);
                            } else { //should never happen
                                /* 
                                 * TODO is it true that it should never happen? Especially, 
                                 * what about ConstantPoolClass values? Give another look at the 
                                 * JVMS and determine whether other kind of constant static fields
                                 * may be present.
                                 */
                                failExecution("Unexpected constant from constant pool (neither primitive nor String)."); 
                                //TODO put string in constant or throw better exception
                            }
                            k.setFieldValue(sig, v);
                        }
                    } catch (FieldNotFoundException | AttributeNotFoundException | InvalidIndexException e) {
                        //this should never happen
                        failExecution(e);
                    }
                }
            }
        }

        private boolean root() throws FrozenStateException {
        	return (this.s.getStackSize() == 0);
        }
        
        /**
         * Phase 3 pushes the {@code <clinit>} frames for all the initialized 
         * classes that have it.
         * 
         * @throws FrozenStateException if {@code this.s} is frozen. 
         * @throws HeapMemoryExhaustedException if the memory is exhausted.
         */
        private void phase3() throws FrozenStateException, HeapMemoryExhaustedException {
            try {
                boolean exceptionBoxFrameYetToPush = true; 
                for (ClassFile classFile : this.classesForPhase3) {
                    final Signature sigClinit = new Signature(classFile.getClassName(), "()" + Type.VOID, "<clinit>");
                    if (classFile.hasMethodImplementation(sigClinit)) {
                    	if (this.preInitializedClasses.contains(classFile)) {
                    		this.s.ensureStringLiteral(classFile.getClassName());
                    		this.s.pushFrame(this.cf_JBSE_BASE, JBSE_BASE_MAKEKLASSSYMBOLIC, root(), 0, this.s.getCalculator().valInt(classFile.getDefiningClassLoader()), this.s.referenceToStringLiteral(classFile.getClassName()));
                                ++this.createdFrames;
                    	}
                        if (this.boxExceptionMethodSignature != null && exceptionBoxFrameYetToPush) {
                            this.s.pushFrame(this.cf_JBSE_BASE, this.boxExceptionMethodSignature, root(), 0);                    
                            ++this.createdFrames;
                        }
                        this.s.pushFrame(classFile, sigClinit, root(), 0);
                        ++this.createdFrames;
                    }
                }
                if (this.pushClinitFor_JAVA_OBJECT) {
                    try {
                        if (this.boxExceptionMethodSignature != null && exceptionBoxFrameYetToPush) {
                            this.s.pushFrame(this.cf_JBSE_BASE, this.boxExceptionMethodSignature, root(), 0);                    
                            ++this.createdFrames;
                        }
                        final Signature sigClinit_JAVA_OBJECT = new Signature(JAVA_OBJECT, "()" + Type.VOID, "<clinit>");
                        final ClassFile cf_JAVA_OBJECT = this.s.getClassHierarchy().loadCreateClass(JAVA_OBJECT);
                        this.s.pushFrame(cf_JAVA_OBJECT, sigClinit_JAVA_OBJECT, root(), 0);
                        ++this.createdFrames;
                    } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                             ClassFileIllFormedException | BadClassFileVersionException | 
                             WrongClassNameException | InvalidInputException | 
                             ClassFileNotAccessibleException e) {
                        //this should never happen
                        failExecution("Could not find the classfile for java.lang.Object.");
                    }
                }
            } catch (MethodNotFoundException | MethodCodeNotFoundException e) {
                /* TODO Here I am in doubt about how I should manage exceptional
                 * situations. The JVMS v8 (4.6, access_flags field discussion)
                 * states that the access flags of <clinit> should be ignored except for 
                 * ACC_STRICT. But it also says that if a method is either native 
                 * or abstract (from its access_flags field) it must have no code.
                 * What if a <clinit> is marked to be abstract or native? In such 
                 * case it should have no code. However, this shall not happen for 
                 * <clinit> methods - all <clinit>s I have seen are not 
                 * native, rather they invoke a static native method. I will assume 
                 * that in this case a verification error should be raised.
                 */
                this.failed = true;
                this.failure = VERIFY_ERROR;
            } catch (InvalidProgramCounterException | NullMethodReceiverException | 
                     ThreadStackEmptyException | InvalidSlotException | InvalidTypeException e) {
                //this should never happen
                failExecution(e);
            } 
        }

        private void revert() throws ClasspathException, FrozenStateException {
            //pops all the frames created by the recursive calls
            try {
                for (int i = 1; i <= this.createdFrames; ++i) {
                    this.s.popCurrentFrame();
                }
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
            
            //it is not necessary to delete the Klass objects
            //because they are not initialized and this fact
            //is registered in their state
            
            //throws and exits
            throwNew(this.s, this.failure);
        }
    }

    /**
     * Utility function that writes a value to an array,
     * invoked by *aload and *astore algorithms. If the parameters
     * are incorrect fails symbolic execution.
     * 
     * @param state a {@link State}.
     * @param ctx an {@link ExecutionContext}.
     * @param arrayReference a {@link Reference} to an {@link Array} in the heap 
     *        of {@code State}.
     * @param index the index in the array where the value should be put.
     *        It must be a {@link Primitive} with type {@link Type#INT INT}.
     * @param valueToStore the {@link Value} to be stored in the array.
     * @throws DecisionException upon failure of the decision procedure.
     */
    public static void storeInArray(State state, ExecutionContext ctx, Reference arrayReference, Primitive index, Value valueToStore) 
    throws DecisionException {
        try {
            final Array array = (Array) state.getObject(arrayReference);
            if (array.hasSimpleRep() && index instanceof Simplex) {
                array.setFast((Simplex) index, valueToStore);
            } else {
                final Iterator<? extends Array.AccessOutcomeIn> entries = array.entriesPossiblyAffectedByAccess(index, valueToStore);
                ctx.decisionProcedure.constrainArrayForSet(state.getClassHierarchy(), entries, index);
                array.set(index, valueToStore);
            }
        } catch (InvalidInputException | InvalidOperandException | 
                 InvalidTypeException | ClassCastException | 
                 FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    public static void invokeClassLoaderLoadClass(State state, PleaseLoadClassException e) 
    throws ClasspathException, ThreadStackEmptyException, InvalidInputException {
        try {
            //gets the initiating loader
            final int initiatingLoader = e.getInitiatingLoader();
            if (!state.hasInstance_JAVA_CLASSLOADER(initiatingLoader)) {
                //this should never happen
                failExecution("Unknown classloader identifier " + initiatingLoader + ".");
            }
            final ReferenceConcrete classLoaderReference = state.referenceToInstance_JAVA_CLASSLOADER(initiatingLoader);

            //makes the string for the class name
            final String className = binaryClassName(e.className());
            state.ensureStringLiteral(className);
            final ReferenceConcrete classNameReference = state.referenceToStringLiteral(className);

            //upcalls ClassLoader.loadClass
            //first, creates the snippet
            final Snippet snippet = state.snippetFactoryNoWrap()
                .op_invokevirtual(JAVA_CLASSLOADER_LOADCLASS) //loads the class...
                .op_invokestatic(noclass_REGISTERLOADEDCLASS) //...and registers it with the initiating loader
                .op_return()
                .mk();
            state.pushSnippetFrameNoWrap(snippet, 0, CLASSLOADER_BOOT, "java/lang");
            //TODO if ClassLoader.loadClass finds no class we should either propagate the thrown ClassNotFoundException or wrap it inside a NoClassDefFoundError.
            //then, pushes the parameters for noclass_REGISTERLOADEDCLASS
            state.pushOperand(state.getCalculator().valInt(initiatingLoader));
            //finally, pushes the parameters for JAVA_CLASSLOADER_LOADCLASS
            state.pushOperand(classLoaderReference);
            state.pushOperand(classNameReference);
        } catch (HeapMemoryExhaustedException exc) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
        } catch (InvalidProgramCounterException exc) {
            //this should never happen
            failExecution(exc);
        }
    }

    /**
     * Finds a classfile corresponding to a class name from the loaded
     * classfiles with an initiating loader suitable to reference resolution.
     * To be used to find the classfile of a resolved reference from its
     * class name.
     * 
     * @param state a {@link State}.
     * @param className a {@link String}.
     * @return the {@link ClassFile} with name {@code className}, if one 
     *         was loaded in {@code state} with either the boot, or the 
     *         extension, or the app classloader as intiating loader. 
     */
    public static ClassFile findClassFile(State state, String className) {
        ClassFile retVal = null;
        for (int classLoader = CLASSLOADER_APP; classLoader >= CLASSLOADER_BOOT; --classLoader) {
            retVal = state.getClassHierarchy().getClassFileClassArray(classLoader, className);
            if (retVal != null) {
                return retVal;
            }
        }
        throw new UnexpectedInternalException("Unable to find the classfile for a reference resolution.");
    }
    
    /** 
     * Do not instantiate it!
     */
    private Util() { }
}
