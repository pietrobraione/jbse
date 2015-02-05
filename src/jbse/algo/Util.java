package jbse.algo;

import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_LINKEDLIST;
import static jbse.bc.Signatures.JAVA_LINKEDLIST_ENTRY;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_CASEINSCOMP;
import static jbse.bc.Signatures.VERIFY_ERROR;
import static jbse.mem.Util.isResolvedSymbolicReference;

import jbse.algo.exc.PleaseDoNativeException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NoMethodReceiverException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.exc.DecisionException;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

public class Util {	
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
	
    
	/**
	 * Equivalent to 
	 * {@link #createAndThrow}{@code (s, VERIFY_ERROR)}.
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
    public static void createAndThrowObject(State state, String exceptionClassName) 
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
	 */
	public static boolean ensureKlass(State state, String className, DecisionProcedure dec) 
	throws DecisionException, ClassFileNotFoundException, ThreadStackEmptyException {
		if (state.initialized(className)) {
			return false; //nothing to do
		}    
		if (decideClassInitialized(state, className, dec)) {
			//TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Possibly withdraw it.
			try {
				state.assumeClassInitialized(className);
			} catch (InvalidIndexException e) {
				throwVerifyError(state);
			}
			return false;
		} else {
			state.assumeClassNotInitialized(className);
			return initializeKlass(state, className, dec);
		}	
	}
    
    /**
	 * Determines the satisfiability of a class initialization under
	 * the current assumption. Wraps {@link DecisionProcedure#isSatInitialized(String)} 
	 * to 
	 * 
	 * @param state a {@link State}.
	 * @param className the name of the class.
	 * @return {@code true} if the class has been initialized, 
	 *         {@code false} otherwise.
	 * @throws DecisionException
	 */
	private static boolean decideClassInitialized(State state, String className, DecisionProcedure dec) 
	throws DecisionException {
		//Assume that some classes are not initialized to
		//trigger the execution of their <clinit> methods 
		//TODO move these assumptions into DecisionProcedure or DecisionProcedureAlgorithms.
		if (state.getClassHierarchy().isSubclass(className, JAVA_ENUM) ||
            className.equals(JAVA_CLASS)  ||
            className.equals(JAVA_LINKEDLIST) || className.equals(JAVA_LINKEDLIST_ENTRY) ||
            className.equals(JAVA_STRING)     || className.equals(JAVA_STRING_CASEINSCOMP)
		    ) {
			return false;
		}

		return dec.isSatInitialized(className);
	}
	
	/**
	 * Loads and initializes a (concrete) class by creating 
	 * for the class and all its uninitialized super{@link Klass} objects 
	 * in a {@link State}'s static store and loading on the 
	 * {@link State}'s {@link ThreadStack} frames for their 
	 * {@code <clinit>} methods.  
	 * 
	 * @param state a {@link State}.
	 * @param className the class to be initialized.
	 * @param dec a {@link DecisionProcedure}.
     * @return {@code true} iff it is necessary to run the 
     *         {@code <clinit>} methods for the initialized 
     *         class(es).
	 * @throws DecisionException
	 * @throws ClassFileNotFoundException 
	 * @throws ThreadStackEmptyException 
	 */
	private static boolean initializeKlass(State state, String className, DecisionProcedure dec) 
	throws DecisionException, ClassFileNotFoundException, ThreadStackEmptyException {
		final ClassInitializer ci = new ClassInitializer();
		final boolean failed = ci.initialize(state, className, dec);
		if (failed) {
		    return false;  //upon failure all frames are discarded
		}
		return ci.createdFrames > 0;
	}

	private static class ClassInitializer {
		/**
		 * Counts the number of frames created during class initialization. 
		 * Used in case {@link #initializeClass} fails to restore the stack.
		 * Its value is used only in the context of an {@link #initializeClass} call, 
		 * and is not reused across multiple calls.
		 */
		private int createdFrames = 0;

		/**
		 * Implements {@link Util#initializeKlass(State, String, DecisionProcedure)} 
		 * by recursion.
		 * 
		 * @param s a {@link State}.
		 * @param className the class to be initialized.
		 * @return {@code true} iff the initialization of 
		 *         {@code className} or of one of its superclasses 
		 *         fails for some reason.
		 * @param dec a {@link DecisionProcedure}.
		 * @throws DecisionException 
		 * @throws ClassFileNotFoundException 
		 * @throws ThreadStackEmptyException 
		 */
		private boolean initialize(State s, String className, DecisionProcedure dec) 
		throws DecisionException, ClassFileNotFoundException, ThreadStackEmptyException {
			//if className is already initialized, then does nothing:
			//the corresponding Klass object will be lazily created
			//upon first access, if it does not exist yet
			if (decideClassInitialized(s, className, dec)) { 
				return false;
			}

			//creates the Klass and puts it in the method area
			try {
				s.createKlass(className);
			} catch (InvalidIndexException e) {
			    throwVerifyError(s);
				return true; //failure
			}
			
			//if className denotes a class rather than an interface, then 
			//recursively initializes its superclass if necessary
			final ClassFile classFile = s.getClassHierarchy().getClassFile(className);
			boolean failed = false;
			if (!classFile.isInterface()) {
				final String superName = classFile.getSuperClassName();
				failed = initialize(s, superName, dec);
			}

			//if the initialization of the superclass(es) didn't succeed, 
			//aborts
			if (failed) {
				return true;
			}

			//in the case a <clinit> method exists, loads a Frame for it
			String myExce = null;
			try {
				final Signature sigClinit = new Signature(className, "()V", "<clinit>");
				if (classFile.hasMethodDeclaration(sigClinit)) {
					s.pushFrame(sigClinit, false, true, false, 0);
					++createdFrames;
				}
			/* TODO Here I am in doubt about how I should manage exceptional
			 * situations. The JVM spec v2 (4.6, access_flags field discussion)
			 * states that the access flags of <clinit> should be ignored except for 
			 * strictfp. But it also says that if a method is either native 
			 * or abstract (from its access_flags field) it must have no code.
			 * What if a <clinit> is marked to be abstract or native? In such 
			 * case it should have no code. However, this shall not happen for 
			 * <clinit> methods. I will assume that in this case a verification 
			 * error should be raised.
			 */
			} catch (IncompatibleClassFileException e) {
				failed = true;
				myExce = INCOMPATIBLE_CLASS_CHANGE_ERROR;
			} catch (MethodNotFoundException | PleaseDoNativeException e) {
				failed = true;
				myExce = VERIFY_ERROR;
			} catch (InvalidProgramCounterException | NoMethodReceiverException | 
					ThreadStackEmptyException | InvalidSlotException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			} 
			
			if (failed) {
				//pops all the frames created by the recursive calls
				for (int i = 1; i <= createdFrames; ++i) {
					s.popCurrentFrame();
				}
				
                //TODO delete all the Klass objects from the static store?
                
				//throws and exits
				createAndThrowObject(s, myExce);
				return true; //returns failure
			} else {
				//returns success
				return false;
			}
		}
	}
	
	public static boolean ensureStringLiteral(State state, String stringLit, DecisionProcedure dec) 
	throws DecisionException, ClasspathException, ThreadStackEmptyException {
	    final boolean mustExit;
	    try {
	        mustExit = ensureKlass(state, JAVA_STRING, dec);
	    } catch (ClassFileNotFoundException e) {
	        throw new ClasspathException(e);
	    }
	    state.ensureStringLiteral(stringLit);
	    return mustExit;
	}
    
    public static boolean ensureClass(State state, String className, DecisionProcedure dec) 
    throws DecisionException, ClasspathException, ClassFileNotFoundException, 
    ClassFileNotAccessibleException, ThreadStackEmptyException {
        final boolean mustExit;
        try {
            mustExit = ensureKlass(state, JAVA_STRING, dec) || ensureKlass(state, JAVA_CLASS, dec);
        } catch (ClassFileNotFoundException e) {
            throw new ClasspathException(e);
        }
        state.ensureClass(className);
        return mustExit;
    }

	/** 
	 * Do not instantiate it!
	 */
	private Util() { }

}
