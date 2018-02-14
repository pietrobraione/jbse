package jbse.algo;

import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_NAME;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STACK_TRACE_ELEMENT;
import static jbse.bc.Signatures.JAVA_STACK_TRACE_ELEMENT_DECLARINGCLASS;
import static jbse.bc.Signatures.JAVA_STACK_TRACE_ELEMENT_FILENAME;
import static jbse.bc.Signatures.JAVA_STACK_TRACE_ELEMENT_LINENUMBER;
import static jbse.bc.Signatures.JAVA_STACK_TRACE_ELEMENT_METHODNAME;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;
import static jbse.bc.Signatures.VERIFY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Frame;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
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

public class Util {
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
        assert false;
        throw InterruptException.mk();
    }

    /**
     * Cleanly interrupts the execution of an {@link Algorithm}, 
     * and schedules another one as the next to be executed.
     * 
     * @param algo the next {@link Algorithm} to be executed.
     */
    public static void continueWith(Algorithm<?, ?, ?, ?, ?> algo)
    throws InterruptException {
        throw InterruptException.mk(algo);
    }

    /**
     * Finds the {@link ClassFile} where the implementation of a method
     * resides (or where the method is declared native).
     * 
     * @param state a {@link State}
     * @param methodSignatureResolved the {@link Signature} of the resolved method
     *        to lookup.
     * @param isStatic {@code true} iff the method is declared static.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param receiverClassName a {@link String}, the class name of the receiver
     *        of the method invocation.
     * @return the {@link ClassFile} of the class which contains the method implementation.
     * @throws BadClassFileException  when the class file 
     *         with name {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         does not exist or is ill-formed.
     * @throws MethodNotFoundException if lookup fails in finding the method implementation.
     * @throws IncompatibleClassFileException when the resolved method is not compatible
     *         with {@code isStatic} or {@code isSpecial}.
     * @throws ThreadStackEmptyException if {@code state} has an empty stack (i.e., no
     *         current method).
     */
    public static ClassFile lookupClassfileMethodImpl(State state, Signature methodSignatureResolved, boolean isStatic, boolean isSpecial, String receiverClassName) 
    throws BadClassFileException, MethodNotFoundException, IncompatibleClassFileException, ThreadStackEmptyException {
        final ClassFile retVal;
        final ClassHierarchy hier = state.getClassHierarchy();
        if (isStatic) {
            retVal = hier.lookupMethodImplStatic(methodSignatureResolved);
        } else if (isSpecial) {
            final String currentClassName = state.getCurrentMethodSignature().getClassName();
            retVal = hier.lookupMethodImplSpecial(currentClassName, methodSignatureResolved);
        } else { //invokevirtual and invokeinterface 
            retVal = hier.lookupMethodImplVirtualInterface(receiverClassName, methodSignatureResolved);
        }
        //TODO invokedynamic
        return retVal;
    }

    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param ref {@code a Reference}.
     * @return a {@link String} corresponding to the {@code value} of 
     *         the {@link Instance} referred by {@code ref}, 
     *         or {@code null} if such {@link Instance}'s 
     *         {@link Instance#getType() type} is not 
     *         {@code "java/lang/String"}, or its {@code value}
     *         is not a concrete array of {@code char}s.
     *         
     */
    public static String valueString(State s, Reference ref) {
        final Instance i;
        try {
            i = (Instance) s.getObject(ref);
        } catch (ClassCastException e) {
            return null;
        }
        if (i.getType().equals(JAVA_STRING)) {
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
     */
    public static void throwVerifyError(State state) {
        try {
            final ReferenceConcrete excReference = state.createInstance(VERIFY_ERROR);
            fillExceptionBacktrace(state, excReference);
            state.unwindStack(excReference);
        } catch (InvalidIndexException | InvalidProgramCounterException e) {
            //there is not much we can do if this happens
            failExecution(e);
        }
    }

    /**
     * Creates a new instance of a given class in the 
     * heap of a state. The fields of the object are initialized 
     * with the default values for each field's type. Then, unwinds 
     * the stack of the state in search for an exception handler for
     * the object. The procedure aims to be fail-safe w.r.t 
     * errors in the classfile.
     * 
     * @param state the {@link State} where the new object will be 
     *        created and whose stack will be unwound.
     * @param exceptionClassName the name of the class of the new instance.
     */
    public static void throwNew(State state, String exceptionClassName) {
        if (exceptionClassName.equals(VERIFY_ERROR)) {
            throwVerifyError(state);
            return;
        }
        final ReferenceConcrete excReference = state.createInstance(exceptionClassName);
        fillExceptionBacktrace(state, excReference);
        throwObject(state, excReference);
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
     */
    public static void throwObject(State state, Reference toThrow) {
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
            final String excClass = exc.getType();
            int stackDepth = 0;
            for (Frame f : state.getStack()) {
                final String fClass = f.getCurrentMethodSignature().getClassName();
                final String methodName = f.getCurrentMethodSignature().getName();
                if (excClass.equals(fClass) && "<init>".equals(methodName)) {
                    break;
                }
                ++stackDepth;
            }
            final ReferenceConcrete refToArray = 
            state.createArray(null, state.getCalculator().valInt(stackDepth), "" + ARRAYOF + REFERENCE + JAVA_STACK_TRACE_ELEMENT + TYPEEND);
            final Array theArray = (Array) state.getObject(refToArray);
            exc.setFieldValue(JAVA_THROWABLE_BACKTRACE, refToArray);
            int i = 0;
            for (Frame f : state.getStack()) {
                final Calculator calc = state.getCalculator();
                final String fClass = f.getCurrentMethodSignature().getClassName();

                //gets the data
                final String declaringClass = fClass.replace('/', '.').replace('$', '.'); //TODO is it ok?
                final String fileName       = state.getClassHierarchy().getClassFile(fClass).getSourceFile();
                final int    lineNumber     = f.getSourceRow(); 
                final String methodName     = f.getCurrentMethodSignature().getName();

                //break if we reach the first frame for the exception <init>
                if (excClass.equals(fClass) && "<init>".equals(methodName)) {
                    break;
                }

                //creates the string literals
                state.ensureStringLiteral(declaringClass);
                state.ensureStringLiteral(fileName);
                state.ensureStringLiteral(methodName);

                //creates the java.lang.StackTraceElement object and fills it
                final ReferenceConcrete steReference = state.createInstance(JAVA_STACK_TRACE_ELEMENT);
                final Instance stackTraceElement = (Instance) state.getObject(steReference);
                stackTraceElement.setFieldValue(JAVA_STACK_TRACE_ELEMENT_DECLARINGCLASS, state.referenceToStringLiteral(declaringClass));
                stackTraceElement.setFieldValue(JAVA_STACK_TRACE_ELEMENT_FILENAME,       state.referenceToStringLiteral(fileName));
                stackTraceElement.setFieldValue(JAVA_STACK_TRACE_ELEMENT_LINENUMBER,     calc.valInt(lineNumber));
                stackTraceElement.setFieldValue(JAVA_STACK_TRACE_ELEMENT_METHODNAME,     state.referenceToStringLiteral(methodName));

                //sets the array
                theArray.setFast(calc.valInt(i++), steReference);
            }
        } catch (BadClassFileException | ClassCastException | 
        InvalidTypeException | InvalidOperandException | 
        FastArrayAccessNotAllowedException e) {
            //this should not happen (and if happens there is not much we can do)
            failExecution(e);
        }
    }

    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store, possibly by creating it together with all 
     * the necessary super{@link Klass}es and all the necessary
     * frames for the {@code <clinit>} methods.
     * 
     * @param state a {@link State}. It must have a current frame.
     * @param className a {@link String}, the name of a class.
     * @param ctx an {@link ExecutionContext}.
     * @return {@code true} iff it is necessary to run the 
     *         {@code <clinit>} methods for the initialized 
     *         class(es).
     * @throws InvalidInputException if {@code className} or {@code state} 
     *         is null.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code className} is or is not initialized.
     * @throws BadClassFileException if {@code className} or
     *         one of its superclasses is not in the classpath or
     *         is ill-formed.
     * @throws ClasspathException if some standard JRE class is missing
     *         from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE. 
     * @throws InterruptException iff it is necessary to interrupt the
     *         execution of the bytecode and run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es).
     */
    public static void ensureClassCreatedAndInitialized(State state, String className, ExecutionContext ctx) 
    throws InvalidInputException, DecisionException, BadClassFileException, 
    ClasspathException, InterruptException {
        final ClassInitializer ci = new ClassInitializer(state, ctx);
        final boolean failed = ci.initialize(className);
        if (failed) {
            return;
        }
        if (ci.createdFrames > 0) {
            exitFromAlgorithm();
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
         * Counts the number of frames created during class initialization. 
         * Used in case {@link #initializeClass} fails to restore the stack.
         * Its value is used only in the context of an {@link #initializeClass} call, 
         * and is not reused across multiple calls.
         */
        private int createdFrames = 0;

        /**
         * Stores the names of the {@link Klass}es that are created by this initializer.
         */
        private final ArrayList<String> classesCreated = new ArrayList<>();

        /**
         * Stores the names of the {@link Klass}es for which the {@code <clinit>} 
         * method must be run.
         */
        private final ArrayList<String> classesToInitialize = new ArrayList<>();

        /**
         * Set to {@code true} iff must load a frame for {@code java.lang.Object}'s 
         * {@code <clinit>}.
         */
        private boolean pushFrameForJavaLangObject = false;

        /**
         * Is the initialization process failed?
         */
        private boolean failed = false;

        /**
         * What is the cause of the failure? (meaningless if failed == false)
         */
        private String failure = null;

        /**
         * Constructor.
         */
        private ClassInitializer(State s, ExecutionContext ctx) {
            this.s = s;
            this.ctx = ctx;
        }

        /**
         * Implements {@link Util#ensureClassCreatedAndInitialized}.
         * 
         * @param className the class to be initialized.
         * @return {@code true} iff the initialization of 
         *         {@code className} or of one of its superclasses 
         *         fails for some reason.
         * @throws InvalidInputException if {@code className} is null.
         * @throws DecisionException if the decision procedure fails.
         * @throws BadClassFileException if the classfile for {@code className} or
         *         for one of its superclasses is not in the classpath or
         *         is ill-formed.
         * @throws ClasspathException if the classfile for some JRE class
         *         is not in the classpath or is incompatible with the
         *         current version of JBSE.
         */
        private boolean initialize(String className)
        throws InvalidInputException, DecisionException, BadClassFileException, ClasspathException {
            phase1(className);
            if (this.failed) {
                handleFailure();
                return true;
            }
            phase2();
            if (this.failed) {
                handleFailure();
                return true;
            }
            phase3();
            if (this.failed) {
                handleFailure();
                return true;
            }
            return false;
        }

        /**
         * Equivalent to {@link #phase1(String, ListIterator) phase1}{@code (className, null)}.
         * 
         * @param className a {@code String}, the name of the class.
         * @param it a {@code ListIterator}, the name of the class.
         * @throws InvalidInputException if {@code className} is null.
         * @throws DecisionException if the decision procedure fails.
         * @throws BadClassFileException if the classfile for {@code className} or
         *         for one of its superclasses is not in the classpath or
         *         is ill-formed.
         */
        private void phase1(String className)
        throws InvalidInputException, DecisionException, BadClassFileException {
            phase1(className, null);
        }

        /**
         * Phase 1 creates all the {@link Klass} objects for a class and its
         * superclasses that can be assumed to be not initialized. It also 
         * refines the path condition by adding all the initialization assumption.
         * 
         * @param className a {@code String}, the name of the class.
         * @param it a {@code ListIterator} to {@code this.classesCreated}.
         * @throws InvalidInputException if {@code className} is null.
         * @throws DecisionException if the decision procedure fails.
         * @throws BadClassFileException if the classfile for {@code className} or
         *         for one of its superclasses is not in the classpath or
         *         is ill-formed.
         */
        private void phase1(String className, ListIterator<String> it)
        throws InvalidInputException, DecisionException, BadClassFileException {
            //if there is a Klass object for className, then 
            //there is nothing to do
            if (this.s.existsKlass(className)) {
                return;
            }    

            if (it == null) {
                this.classesCreated.add(className);
            } else {
                it.add(className);
            }
            //TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Withdraw this assumption and branch.
            try {
                //invokes the decision procedure, adds the returned 
                //assumption to the state's path condition and creates 
                //a Klass
            		final ClassHierarchy hier = this.s.getClassHierarchy();
            		final boolean pure = this.ctx.hasClassAPureInitializer(hier, className);
            		final boolean createKlass;
            		if (pure) {
            			createKlass = true;
            		} else if (this.ctx.decisionProcedure.isSatInitialized(hier, className)) { 
                    this.s.assumeClassInitialized(className);
                    createKlass = false;
                } else {
                    this.s.assumeClassNotInitialized(className);
                    createKlass = true;
                }
            		if (createKlass) {
                    //creates the Klass and schedules it for phase 3
                    this.s.ensureKlass(className);
                    if (className.equals(JAVA_OBJECT)) {
                        this.pushFrameForJavaLangObject = true;
                    } else {
                        this.classesToInitialize.add(className);
                    }
                }
            } catch (InvalidIndexException e) {
                this.failed = true;
                this.failure = VERIFY_ERROR;
                return;
            }

            //if className denotes a class rather than an interface
            //and has a superclass, then recursively performs phase1 
            //on its superclass(es)
            final ClassFile classFile = this.s.getClassHierarchy().getClassFile(className);
            if (!classFile.isInterface()) {
                final String superName = classFile.getSuperClassName();
                if (superName != null) {
                    phase1(superName, it);
                }
            }
        }

        /**
         * Phase 2 inits the constant fields for all the {@link Klass} objects
         * created during phase 1; in the case one of these fields is a 
         * {@code String} constant launches phase 1 on {@code java.lang.String}.
         * 
         * @throws DecisionException if the decision procedure fails.
         * @throws BadClassFileException if the classfile for any of the 
         *         classes to initialize is not in the classpath or
         *         is ill-formed.
         * @throws ClasspathException if the classfile for some JRE class
         *         is not in the classpath or is incompatible with the
         *         current version of JBSE.
         */
        private void phase2() 
        throws DecisionException, BadClassFileException, ClasspathException {
            final ListIterator<String> it = this.classesCreated.listIterator();
            while (it.hasNext()) {
                final String className = it.next();
                final Klass k = this.s.getKlass(className);
                final ClassFile classFile = this.s.getClassHierarchy().getClassFile(className);
                final Signature[] flds = classFile.getFieldsStatic();
                for (final Signature sig : flds) {
                    try {
                        if (classFile.isFieldConstant(sig)) {
                            //sig is directly extracted from the classfile, 
                            //so no resolution is necessary
                            final Value v;
                            final ConstantPoolValue cpv = classFile.fieldConstantValue(sig);
                            if (cpv instanceof ConstantPoolPrimitive) {
                                v = s.getCalculator().val_(cpv.getValue());
                            } else if (cpv instanceof ConstantPoolString) {
                                final String stringLit = ((ConstantPoolString) cpv).getValue();
                                try {
                                    phase1(JAVA_STRING, it);
                                } catch (InvalidInputException e) {
                                    //this should never happen
                                    throw new UnexpectedInternalException(e);
                                } catch (ClassFileNotFoundException e) {
                                    throw new ClasspathException(e);
                                }
                                s.ensureStringLiteral(stringLit);
                                v = s.referenceToStringLiteral(stringLit);
                            } else { //cpv instanceof ConstantPoolClass - should never happen
                                throw new UnexpectedInternalException("Unexpected constant from constant pool (neither primitive nor java.lang.String)"); 
                                //TODO put string in constant or throw better exception
                            }
                            k.setFieldValue(sig, v);
                        }
                    } catch (FieldNotFoundException | AttributeNotFoundException | InvalidIndexException e) {
                        //this should never happen
                        throw new UnexpectedInternalException(e);
                    }
                }
            }
        }

        /**
         * Phase 3 pushes the {@code <clinit>} frames for all the initialized 
         * classes that have it.
         * 
         * @throws ClasspathException whenever the classfile for
         *         {@code java.lang.Object} is not in the classpath
         *         or is incompatible with the current JBSE.
         * @throws BadClassFileException  whenever the classfile for
         *         one of the classes to initialize is not in the classpath
         *         or is ill-formed.
         */
        private void phase3() throws ClasspathException, BadClassFileException {
            try {
                final ClassHierarchy classHierarchy = this.s.getClassHierarchy();
                for (String className : reverse(this.classesToInitialize)) {
                    final Signature sigClinit = new Signature(className, "()" + Type.VOID, "<clinit>");
                    final ClassFile classFile = classHierarchy.getClassFile(className);
                    if (classFile.hasMethodImplementation(sigClinit)) {
                        s.pushFrame(sigClinit, false, 0);
                        ++createdFrames;
                    }
                }
                if (this.pushFrameForJavaLangObject) {
                    final Signature sigClinit = new Signature(JAVA_OBJECT, "()" + Type.VOID, "<clinit>");
                    try {
                        s.pushFrame(sigClinit, false, 0);
                    } catch (ClassFileNotFoundException e) {
                        throw new ClasspathException(e);
                    }
                    ++createdFrames;
                }
            } catch (MethodNotFoundException | MethodCodeNotFoundException e) {
                /* TODO Here I am in doubt about how I should manage exceptional
                 * situations. The JVM spec v2 (4.6, access_flags field discussion)
                 * states that the access flags of <clinit> should be ignored except for 
                 * strictfp. But it also says that if a method is either native 
                 * or abstract (from its access_flags field) it must have no code.
                 * What if a <clinit> is marked to be abstract or native? In such 
                 * case it should have no code. However, this shall not happen for 
                 * <clinit> methods - all <clinit>s I have seen are not by themselves
                 * native, rather they invoke a static native method. I will assume 
                 * that in this case a verification error should be raised.
                 */
                this.failed = true;
                this.failure = VERIFY_ERROR;
            } catch (InvalidProgramCounterException | NullMethodReceiverException | 
            ThreadStackEmptyException | InvalidSlotException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            } 
        }

        private void handleFailure() {
            //pops all the frames created by the recursive calls
            for (int i = 1; i <= this.createdFrames; ++i) {
                try {
                    this.s.popCurrentFrame();
                } catch (ThreadStackEmptyException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            }

            //TODO delete all the Klass objects from the static store?
            //TODO delete all the created String object from static field initialization?

            //throws and exits
            throwNew(this.s, this.failure);
        }
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

    /**
     * Creates an {@link Instance} of class {@code java.lang.String} 
     * in a {@link State}'s heap corresponding to a string literal sidestepping 
     * the constructors of {@code java.lang.String} to avoid incredible 
     * circularity issues with string constant fields. Also 
     * manages the creation and initialization of the {@link Klass} for 
     * {@code java.lang.String} and its members. If the literal already 
     * exists in the {@link State}'s heap, does nothing.
     * 
     * @param state the {@link State} on which this method will operate.
     * @param stringLit a {@link String} representing a string literal.
     * @param ctx an {@link ExecutionContext}.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code java.lang.String} is or is not initialized.
     * @throws ClassFileIllFormedException if the {@code java.lang.String} classfile 
     *         is ill-formed.
     * @throws ClasspathException if the {@code java.lang.String} class is 
     *         missing from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE.
     * @throws InterruptException  iff it is necessary to interrupt the
     *         execution of the current bytecode and run the 
     *         {@code <clinit>} method for {@code java.lang.String}.
     */
    public static void ensureStringLiteral(State state, String stringLit, ExecutionContext ctx) 
    throws DecisionException, ClassFileIllFormedException, ClasspathException, InterruptException {
        state.ensureStringLiteral(stringLit);
        try {
            ensureClassCreatedAndInitialized(state, JAVA_STRING, ctx);
        } catch (ClassFileNotFoundException e) {
            throw new ClasspathException(e);
        } catch (ClassFileIllFormedException e) {
            throw e;
        } catch (InvalidInputException | BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    /**
     * Ensures an {@link Instance} of class {@code java.lang.Class} 
     * corresponding to a class name exists in the {@link Heap}. If
     * the instance does not exist, it resolves the class and creates 
     * it, otherwise it does nothing. Also manages the creation 
     * of the {@link Klass}es for {@code java.lang.Class} and for 
     * the classes of the members of the created object.
     * 
     * @param state the {@link State} on which this method will operate.
     * @param accessor a {@link String}, the name of the class of the accessor 
     *        that wants to obtain the {@link Instance} of {@code java.lang.Class}. 
     * @param className a {@link String}, the name of the class reified
     *        by the {@link Instance} of {@code java.lang.Class}.
     * @param ctx an {@link ExecutionContext}.
     * @throws DecisionException if {@code dec} fails in determining
     *         whether {@code java.lang.String} is or is not initialized.
     * @throws ClassFileIllFormedException if the {@code java.lang.String} classfile 
     *         is ill-formed.
     * @throws ClassFileNotAccessibleException if {@code className} is not
     *         accessible from {@code accessor}.
     * @throws ClasspathException if the {@code java.lang.String} class is 
     *         missing from {@code state}'s classpath or is incompatible with the
     *         current version of JBSE.
     * @throws InterruptException  iff it is necessary to interrupt the
     *         execution of the current bytecode and run the 
     *         {@code <clinit>} method for {@code java.lang.String}.
     */
    public static void ensureInstance_JAVA_CLASS(State state, String accessor, String className, ExecutionContext ctx) 
    throws DecisionException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    ClasspathException, InterruptException {
        //we store locally the interrupt and throw it at the end
        //to ensure the invariant that, at the end of the invocation, 
        //everything is created so the second time this method is 
        //invoked because of interruption nothing remains to do 
        InterruptException exc = null;  

        //possibly creates and initializes java.lang.Class
        try {
            ensureClassCreatedAndInitialized(state, JAVA_CLASS, ctx);
        } catch (InterruptException e) {
            exc = e;
        } catch (ClassFileNotFoundException e) {
            throw new ClasspathException(e);
        } catch (ClassFileIllFormedException e) {
            throw e;
        } catch (InvalidInputException | BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }

        //creates a String object for the binary class name
        final String classNameBinary = binaryClassName(className);
        //TODO is it ok to treat the class name String as a string literal?
        try {
            ensureStringLiteral(state, classNameBinary, ctx);
        } catch (InterruptException e) {
            exc = e;
        }

        //possibly creates and initializes the java.lang.Class Instance
        final boolean mustInit = (!state.hasInstance_JAVA_CLASS(className));
        try {
            state.ensureInstance_JAVA_CLASS(accessor, className);
        } catch (ClassFileIllFormedException e) {
            throw e;
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
        if (mustInit) {
            final Reference r = state.referenceToInstance_JAVA_CLASS(className);
            final Instance i = (Instance) state.getObject(r);
            final ReferenceConcrete classNameString = state.referenceToStringLiteral(classNameBinary);
            i.setFieldValue(JAVA_CLASS_NAME, classNameString);
        }

        //throws the interrupt, if any
        if (exc != null) {
            throw exc;
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
                final Iterator<Array.AccessOutcomeIn> entries = array.entriesPossiblyAffectedByAccess(index, valueToStore);
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

    /** 
     * Do not instantiate it!
     */
    private Util() { }

}
