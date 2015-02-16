package jbse.algo;

import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_NAME;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE;
import static jbse.bc.Signatures.JAVA_LINKEDLIST;
import static jbse.bc.Signatures.JAVA_LINKEDLIST_ENTRY;
import static jbse.bc.Signatures.JAVA_NUMBER;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_CASEINSCOMP;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.bc.Signatures.VERIFY_ERROR;
import static jbse.common.Type.binaryClassName;
import static jbse.mem.Util.isResolvedSymbolicReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jbse.algo.exc.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileException;
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
import jbse.dec.DecisionProcedure;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

public class Util {	
    /**
     * Determines whether two {@link Reference}s are alias.
     * 
     * @param s a {@link State}.
     * @param r1 a {@link Reference}.
     * @param r2 a {@link Reference}.
     * @return {@code true} iff {@code r1} and {@code r2} are 
     *         concrete or resolved, and {@code r1} refers to the same 
     *         object as {@code r2}.
     */
	public static boolean aliases(State s, Reference r1, Reference r2) {
		final long pos1;
		if (r1 instanceof ReferenceConcrete) {
			pos1 = ((ReferenceConcrete) r1).getHeapPosition();
		} else if (isResolvedSymbolicReference(s, r1)) {
			pos1 = s.getResolution((ReferenceSymbolic) r1);
		} else {
			return false;
		}
		final long pos2;
		if (r2 instanceof ReferenceConcrete) {
			pos2 = ((ReferenceConcrete) r2).getHeapPosition();
		} else if (isResolvedSymbolicReference(s, r2)) {
			pos2 = s.getResolution((ReferenceSymbolic) r2);
		} else {
			return false;
		}
		return (pos1 == pos2);
	}
	
	public static ClassFile lookupMethodImpl(State state, Signature methodSignatureResolved, boolean isStatic, boolean isSpecial, String receiverClassName) 
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
            return value.valueString();
        } else {
            return null;
        }
    }

    /**
	 * Equivalent to 
	 * {@link #createAndThrowObject}{@code (s, VERIFY_ERROR)}.
	 * 
	 * @param state the {@link State} whose {@link Heap} will receive 
	 *              the new object.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public static void throwVerifyError(State state) {
	    try {
	        final ReferenceConcrete excReference = state.createInstance(VERIFY_ERROR);
	        state.push(excReference);
            state.throwObject(excReference);
        } catch (ThreadStackEmptyException | InvalidIndexException |
                 InvalidProgramCounterException e) {
            //there is not much we can do if this happens
            throw new UnexpectedInternalException(e);
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
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public static void throwNew(State state, String exceptionClassName) 
    throws ThreadStackEmptyException {
        if (exceptionClassName.equals(VERIFY_ERROR)) {
            throwVerifyError(state);
            return;
        }
        final ReferenceConcrete excReference = state.createInstance(exceptionClassName);
        state.push(excReference);
        throwObject(state, excReference);
    }

    /**
     * Unwinds the stack of a state until it finds an exception 
     * handler for an object. This procedure aims to wrap 
     * {@link State#throwObject(Reference)} with a fail-safe  
     * interface to errors in the classfile.
     * 
     * @param state the {@link State} where the new object will be 
     *        created and whose stack will be unwound.
     * @param toThrow see {@link State#throwObject(Reference)}.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public static void throwObject(State state, Reference toThrow) 
    throws ThreadStackEmptyException {
        try {
            state.throwObject(toThrow);
        } catch (InvalidIndexException | InvalidProgramCounterException e) {
            throwVerifyError(state);
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
	 * @param dec a {@link DecisionProcedure}.
	 * @return {@code true} iff it is necessary to run the 
	 *         {@code <clinit>} methods for the initialized 
	 *         class(es).
	 * @throws DecisionException if {@code dec} fails in determining
	 *         whether {@code className} is or is not initialized.
	 * @throws ClassFileNotFoundException if {@code className} does 
	 *         not have a suitable classfile in the classpath.
	 * @throws ThreadStackEmptyException if {@code state} has not a 
	 *         current frame (is stuck).
	 * @throws ClasspathException if some standard JRE class is missing
	 *         from {@code state}'s classpath or is incompatible with the
	 *         current version of JBSE. 
	 * @throws InterruptException iff it is necessary to interrupt the
	 *         execution of the bytecode and run the 
     *         {@code <clinit>} method(s) for the initialized 
     *         class(es).
	 */
	public static void ensureClassCreatedAndInitialized(State state, String className, DecisionProcedure dec) 
	throws DecisionException, BadClassFileException, 
	ThreadStackEmptyException, ClasspathException, InterruptException {
        final ClassInitializer ci = new ClassInitializer(state, dec);
        final boolean failed = ci.initialize(className);
        if (failed) {
            return;
        }
        if (ci.createdFrames > 0) {
            throw new InterruptException();
        }
	}
    
    /**
	 * Determines the satisfiability of a class initialization under
	 * the current assumption. Wraps {@link DecisionProcedure#isSatInitialized(String)} 
	 * to inject some assumptions on the initialization over some JRE standard classes.
	 * 
	 * @param state a {@link State}.
	 * @param className the name of the class.
	 * @param dec a {@link DecisionProcedure}.
	 * @return {@code true} if the class has been initialized, 
	 *         {@code false} otherwise.
	 * @throws DecisionException if the decision procedure fails.
	 */
	private static boolean decideClassInitialized(State state, String className, DecisionProcedure dec) 
	throws DecisionException {
		//Assume that some classes are not initialized to
		//trigger the execution of their <clinit> methods 
		//TODO move these assumptions into DecisionProcedure or DecisionProcedureAlgorithms.
		if (className.equals(JAVA_CLASS)      ||
            className.equals(JAVA_INTEGER)    || className.equals(JAVA_INTEGER_INTEGERCACHE) || 
            className.equals(JAVA_LINKEDLIST) || className.equals(JAVA_LINKEDLIST_ENTRY)     ||
            className.equals(JAVA_NUMBER)     ||
            className.equals(JAVA_OBJECT)     ||
            className.equals(JAVA_STRING)     || className.equals(JAVA_STRING_CASEINSCOMP)   ||
            state.getClassHierarchy().isSubclass(className, JAVA_ENUM)
           ) {
			return false;
		}

		return dec.isSatInitialized(className);
	}
	
	private static class ClassInitializer {
	    /**
	     * The current state.
	     */
	    private final State s;
	    
	    /**
	     * The decision procedure.
	     */
	    private final DecisionProcedure dec;
	    
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
		 * Set to true iff must load a frame for {@code java.lang.Object}'s 
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
		private ClassInitializer(State s, DecisionProcedure dec) {
		    this.s = s;
		    this.dec = dec;
		}
		
        /**
         * Implements {@link Util#initializeKlass(State, String, DecisionProcedure)}.
         * 
         * @param className the class to be initialized.
         * @return {@code true} iff the initialization of 
         *         {@code className} or of one of its superclasses 
         *         fails for some reason.
         * @throws DecisionException if the decision procedure fails.
         * @throws BadClassFileException if the classfile for {@code className} or
         *         for one of its superclasses is not in the classpath or
         *         is ill-formed.
         * @throws ClasspathException if the classfile for some JRE class
         *         is not in the classpath or is incompatible with the
         *         current version of JBSE.
         * @throws ThreadStackEmptyException if {@code this.s} has an empty
         *         thread stack.
         */
		private boolean initialize(String className)
		throws DecisionException, BadClassFileException, 
		ThreadStackEmptyException, ClasspathException {
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
		 * Phase 1 creates all the {@link Klass} objects for a class and its
		 * superclasses that can be assumed to be not initialized. It also 
		 * refines the path condition by adding all the initialization assumption
		 * and loads all the <clinit> frames for these classes, with the exclusion
		 * of the <clinit> of java.lang.Object.
		 * 
		 * @param className a {@code String}, the name of the class.
		 * @throws DecisionException if the decision procedure fails.
		 * @throws BadClassFileException if the classfile for {@code className} or
		 *         for one of its superclasses is not in the classpath or
		 *         is ill-formed.
		 */
        private void phase1(String className)
        throws DecisionException, BadClassFileException {
            //if there is a Klass object for className, then 
            //there is nothing to do
            if (this.s.initialized(className)) {
                return;
            }    

            this.classesCreated.add(className);
            //TODO here we assume mutual exclusion of the initialized/not initialized assumptions. We could withdraw this assumption at the expense of a considerable blow in the number of cases.
            try {
                //invokes the decision procedure, adds the returned 
                //assumption to the state's path condition and creates 
                //a Klass
                if (decideClassInitialized(this.s, className, this.dec)) { 
                    this.s.assumeClassInitialized(className); 
                } else {
                    this.s.assumeClassNotInitialized(className);
                    //schedules the Klass object for phase 2
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
	                phase1(superName);
				}
			}
		}
        
        /**
         * Phase 2 inits the constant fields for all the {@code Klass} objects
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
            for (String className : this.classesCreated) {
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
                                    phase1(JAVA_STRING);
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
         * Pushes the {@code <clinit>} frames for all the initialized classes
         * that have it.
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
        
        private void handleFailure() throws ThreadStackEmptyException {
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
	   
	public static void ensureStringLiteral(State state, String stringLit, DecisionProcedure dec) 
	throws DecisionException, ClasspathException, ThreadStackEmptyException, InterruptException {
	    InterruptException exc = null;
	    try {
	        ensureClassCreatedAndInitialized(state, JAVA_STRING, dec);
	    } catch (BadClassFileException e) {
	        throw new ClasspathException(e);
	    } catch (InterruptException e) {
	        exc = e;
	    }
	    state.ensureStringLiteral(stringLit);
	    if (exc != null) {
	        throw exc;
	    }
	}
    
    public static void ensureClassInstance(State state, String className, DecisionProcedure dec) 
    throws DecisionException, ClasspathException, BadClassFileException, 
    ClassFileNotAccessibleException, ThreadStackEmptyException, InterruptException {
        //possibly creates and initializes java.lang.Class
        InterruptException exc = null;
        try {
            ensureClassCreatedAndInitialized(state, JAVA_CLASS, dec);
        } catch (BadClassFileException e) {
            throw new ClasspathException(e);
        } catch (InterruptException e) {
            exc = e;
        }
        
        //possibly creates and initializes the java.lang.Class Instance
        final boolean mustInitClass = (!state.hasClassInstance(className));
        state.ensureClassInstance(className);
        if (mustInitClass) {
            final Reference r = state.referenceToClassInstance(className);
            final Instance i = (Instance) state.getObject(r);
            final String classNameBinary = binaryClassName(className);
            try {
                //TODO is it ok to treat the class name String as a string literal?
                ensureStringLiteral(state, classNameBinary, dec);
            } catch (InterruptException e) {
                exc = e;
            }
            final ReferenceConcrete classNameString = state.referenceToStringLiteral(classNameBinary);
            i.setFieldValue(JAVA_CLASS_NAME, classNameString);
        }
        
        //throws InterruptException if there is some <clinit>
        //frame on the stack
        if (exc != null) {
            throw exc;
        }
    }

	/** 
	 * Do not instantiate it!
	 */
	private Util() { }

}
