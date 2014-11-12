package jbse.algo;

import static jbse.Util.JAVA_LANG_ENUM;
import static jbse.Util.VERIFY_ERROR;
import static jbse.Util.byteCat;
import static jbse.mem.Util.isResolvedSymbolicReference;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.exc.algo.JavaReifyException;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.ClassFileNotAccessibleException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.bc.NoMethodReceiverException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.Reference;
import jbse.mem.ReferenceConcrete;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.State;

public class Util {	
	//exceptions
	public static final String ARITHMETIC_EXCEPTION 				= "java/lang/ArithmeticException";
	public static final String ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION 	= "java/lang/ArrayIndexOutOfBoundsException";
	public static final String CLASS_CAST_EXCEPTION 				= "java/lang/ClassCastException";
	public static final String INDEX_OUT_OF_BOUNDS_EXCEPTION 		= "java/lang/IndexOutOfBoundsException";
	public static final String NEGATIVE_ARRAY_SIZE_EXCEPTION 		= "java/lang/NegativeArraySizeException";
	public static final String NULL_POINTER_EXCEPTION				= "java/lang/NullPointerException";
	
	//errors
	public static final String ABSTRACT_METHOD_ERROR                = "java/lang/AbstractMethodError";
	public static final String ILLEGAL_ACCESS_ERROR                 = "java/lang/IllegalAccessError";
	public static final String INCOMPATIBLE_CLASS_CHANGE_ERROR		= "java/lang/IncompatibleClassChangeError";
	public static final String NO_CLASS_DEFINITION_FOUND_ERROR      = "java/lang/NoClassDefFoundError";
	public static final String NO_SUCH_FIELD_ERROR                  = "java/lang/NoSuchFieldError";
	public static final String NO_SUCH_METHOD_ERROR                 = "java/lang/NoSuchMethodError";

	static boolean aliases(State s, Reference r1, Reference r2) {
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
	static boolean ensureKlass(State s, String className, DecisionProcedure dec) 
	throws DecisionException, ClassFileNotFoundException, ThreadStackEmptyException {
		if (s.initialized(className)) {
			return false; //nothing to do
		} else if (decideClassInitialized(s, className, dec)) {
			//TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Possibly withdraw it.
			try {
				s.assumeClassInitialized(className);
			} catch (InvalidIndexException e) {
				s.createThrowableAndThrowIt(VERIFY_ERROR);
			}
			return false;
		} else {
			s.assumeClassNotInitialized(className);
			initializeKlass(s, className, dec);
			return true;
		}	
	}
    
    /**
	 * Determines the satisfiability of a class initialization under
	 * the current assumption. Wraps {@link DecisionProcedure#isSatInitialized(String)} 
	 * to 
	 * 
	 * @param s a {@link State}.
	 * @param className the name of the class.
	 * @return {@code true} if the class has been initialized, 
	 *         {@code false} otherwise.
	 * @throws DecisionException
	 */
	private static boolean decideClassInitialized(State s, String className, DecisionProcedure dec) 
	throws DecisionException {
		//We force the assumption that the enums are not initialized to
		//trigger the execution of their <clinit> methods 
		//TODO this is just for convenience, possibly consider to remove this assumption, or to move into DecisionProcedure or DecisionProcedureAlgorithms.
		if (s.getClassHierarchy().isSubclass(className, JAVA_LANG_ENUM)) {
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
	 * @param s a {@link State}.
	 * @param className the class to be initialized.
	 * @param dec a {@link DecisionProcedure}.
	 * @throws DecisionException
	 * @throws ClassFileNotFoundException 
	 * @throws ThreadStackEmptyException 
	 */
	private static void initializeKlass(State s, String className, DecisionProcedure dec) 
	throws DecisionException, ClassFileNotFoundException, ThreadStackEmptyException {
		final ClassInitializer ci = new ClassInitializer();
		ci.initialize(s, className, dec);
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
		 * Implements {@link Util#initializeKlass(State, String, DecisionProcedure)} by recursion.
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
				s.createThrowableAndThrowIt(VERIFY_ERROR);
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
				//TODO delete all the Klass objects from the static store?
				
				//pops all the frames created by the recursive calls
				for (int i = 1; i <= createdFrames; ++i) {
					s.popCurrentFrame();
				}
				s.createThrowableAndThrowIt(myExce);
				return true; //returns failure
			} else {
				//returns success
				return false;
			}
		}
	}
	
	static boolean checkCastInstanceof(State runningState) 
	throws JavaReifyException, ThreadStackEmptyException {
		final int index;
		try {
			final byte tmp1 = runningState.getInstruction(1);
	        final byte tmp2 = runningState.getInstruction(2);
	        index = byteCat(tmp1,tmp2);
		} catch (InvalidProgramCounterException e) {
			throw new JavaReifyException(VERIFY_ERROR);
		}

        //gets in the current class constant pool the name 
        //of the class
        final ClassHierarchy hier = runningState.getClassHierarchy();
        final String currentClassName = runningState.getCurrentMethodSignature().getClassName();	
        final String classSignature;
		try {
			classSignature = hier.getClassFile(currentClassName).getClassSignature(index);
		} catch (ClassFileNotFoundException | InvalidIndexException e) {
			throw new JavaReifyException(VERIFY_ERROR);
		}

        //performs resolution
        final String classSignatureResolved;
		try {
			classSignatureResolved = hier.resolveClass(currentClassName, classSignature);
		} catch (ClassFileNotFoundException e) {
			throw new JavaReifyException(NO_CLASS_DEFINITION_FOUND_ERROR);
		} catch (ClassFileNotAccessibleException e) {
			throw new JavaReifyException(ILLEGAL_ACCESS_ERROR);
		}

        //gets from the operand stack the reference to the 
        //object to be checked
        final Reference tmpValue;
		try {
			tmpValue = (Reference) runningState.top();
		} catch (OperandStackEmptyException e) {
			throw new JavaReifyException(VERIFY_ERROR);
		}
        
        //checks whether the object's class is a subclass 
        //of the class name from the constant pool
        final boolean isSubclass;
        if (runningState.isNull(tmpValue)) {
        	isSubclass = true;  //the null value belongs to all classes
        } else {
	        final Objekt objS = runningState.getObject(tmpValue);
	        String classS = objS.getType();
	        isSubclass = hier.isSubclass(classS, classSignatureResolved);
        }
        
        return isSubclass;
	}

	/** 
	 * Do not instantiate it!
	 */
	private Util() { }

}
