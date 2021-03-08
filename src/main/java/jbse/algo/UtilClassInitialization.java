package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_MAKEKLASSSYMBOLIC;
import static jbse.bc.Signatures.VERIFY_ERROR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

public final class UtilClassInitialization {
	/**
	 * Ensures that a {@link State} has a {@link Klass} in its 
	 * static store for one or more classes, possibly creating the necessary
	 * frames for the {@code <clinit>} methods to initialize them, 
	 * or initializing them symbolically. If necessary it also recursively 
	 * initializes their superclasses. It is equivalent
	 * to {@link UtilClassInitialization#ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
	 * {@code (state, ctx, null, null, classFile)}.
	 * 
	 * @param state a {@link State}. It must have a current frame.
	 * @param ctx an {@link ExecutionContext}.
	 * @param classFile a varargs of {@link ClassFile}s for the classes which must
	 *        be initialized.
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
	 *         class(es).
	 * @throws ContradictionException if some initialization assumption is
	 *         contradicted.
	 */
	public static void ensureClassInitialized(State state, ExecutionContext ctx, ClassFile... classFile)
	throws InvalidInputException, DecisionException, 
	ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
		try {
			ensureClassInitialized(state, ctx, null, null, classFile);
		} catch (ClassFileNotFoundException | IncompatibleClassFileException | 
		ClassFileIllFormedException | BadClassFileVersionException | 
		RenameUnsupportedException | WrongClassNameException | 
		ClassFileNotAccessibleException e) {
			//this should never happen
			failExecution(e);
		}
	}

	/**
	 * Ensures that a {@link State} has a {@link Klass} in its 
	 * static store for one or more classes, possibly creating the necessary
	 * frames for the {@code <clinit>} methods to initialize them, 
	 * or initializing them symbolically. If necessary it also recursively 
	 * initializes their superclasses. It is equivalent
	 * to {@link UtilClassInitialization#ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
	 * {@code (state, ctx, null, boxExceptionMethodSignature, classFile)}.
	 * 
	 * @param state a {@link State}. It must have a current frame.
	 * @param ctx an {@link ExecutionContext}.
	 * @param boxExceptionMethodSignature a {@link Signature} for a method in
	 *        {@link jbse.base.Base} that boxes exceptions thrown by the initializer
	 *        methods, or {@code null} if no boxing must be performed. The class
	 *        name in the signature is not considered.
	 * @param classFile a varargs of {@link ClassFile}s for the classes which must
	 *        be initialized.
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
	 *         class(es).
	 * @throws ContradictionException  if some initialization assumption is
	 *         contradicted.
	 */
	public static void ensureClassInitialized(State state, ExecutionContext ctx, Signature boxExceptionMethodSignature, ClassFile... classFile)
	throws InvalidInputException, DecisionException, 
	ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
		try {
			ensureClassInitialized(state, ctx, null, boxExceptionMethodSignature, classFile);
		} catch (ClassFileNotFoundException | IncompatibleClassFileException | 
		ClassFileIllFormedException | BadClassFileVersionException | 
		RenameUnsupportedException | WrongClassNameException | 
		ClassFileNotAccessibleException e) {
			//this should never happen
			failExecution(e);
		}
	}

	/**
	 * Ensures that a {@link State} has a {@link Klass} in its 
	 * static store for one or more classes, possibly creating the necessary
	 * frames for the {@code <clinit>} methods to initialize them, 
	 * or initializing them symbolically. If necessary it also recursively 
	 * initializes their superclasses. It is equivalent
	 * to {@link UtilClassInitialization#ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
	 * {@code (state, ctx, skip, null, classFile)}.
	 * 
	 * @param state a {@link State}. It must have a current frame.
	 * @param ctx an {@link ExecutionContext}.
	 * @param skip a {@link Set}{@code <}{@link String}{@code >}.
	 *        All the classes (and their superclasses and superinterfaces recursively) 
	 *        whose names are in this set will not be created. A {@code null} value
	 *        is equivalent to the empty set. All the classes must be in the bootstrap
	 *        classpath and will be loaded with the bootstrap classloader.
	 * @param classFile a varargs of {@link ClassFile}s for the classes which must
	 *        be initialized.
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
	 *         class(es).
	 * @throws ClassFileNotFoundException if some class in {@code skip} does not exist
	 *         in the bootstrap classpath.
	 * @throws IncompatibleClassFileException if the superclass for some class in {@code skip} is 
	 *         resolved to an interface type, or any superinterface is resolved to an object type.
	 * @throws ClassFileIllFormedException if some class in {@code skip} is ill-formed.
	 * @throws BadClassFileVersionException if some class in {@code skip} has a version number
	 *         that is unsupported by this version of JBSE.
	 * @throws RenameUnsupportedException if some class in {@code skip} derives from a 
	 *         model class but the classfile does not support renaming.
	 * @throws WrongClassNameException if the bytecode of some class in {@code skip} has a name
	 *         that is different from what expected (the corresponding name in {@code skip}).
	 * @throws ClassFileNotAccessibleException if some class in {@code skip} has
	 *         a superclass/superinterface that it cannot access.
	 * @throws ContradictionException  if some initialization assumption is
	 *         contradicted.
	 */
	public static void ensureClassInitialized(State state, ExecutionContext ctx, Set<String> skip, ClassFile... classFile)
	throws InvalidInputException, DecisionException, ClasspathException, HeapMemoryExhaustedException, 
	InterruptException, ClassFileNotFoundException, IncompatibleClassFileException, ClassFileIllFormedException, 
	BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, ClassFileNotAccessibleException, ContradictionException {
		ensureClassInitialized(state, ctx, skip, null, classFile);
	}

	/**
	 * Ensures that a {@link State} has a {@link Klass} in its 
	 * static store for one or more classes, possibly creating the necessary
	 * frames for the {@code <clinit>} methods to initialize them, 
	 * or initializing them symbolically. If necessary it also recursively 
	 * initializes their superclasses.
	 * 
	 * @param state a {@link State}. It must have a current frame.
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
	 * @param classFile a varargs of {@link ClassFile}s for the classes which must
	 *        be initialized.
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
	 *         class(es).
	 * @throws ClassFileNotFoundException if some class in {@code skip} does not exist
	 *         in the bootstrap classpath.
	 * @throws IncompatibleClassFileException if the superclass for some class in {@code skip} is 
	 *         resolved to an interface type, or any superinterface is resolved to an object type.
	 * @throws ClassFileIllFormedException if some class in {@code skip} is ill-formed.
	 * @throws BadClassFileVersionException if some class in {@code skip} has a version number
	 *         that is unsupported by this version of JBSE.
	 * @throws RenameUnsupportedException if some class in {@code skip} derives from a 
	 *         model class but the classfile does not support renaming.
	 * @throws WrongClassNameException if the bytecode of some class in {@code skip} has a name
	 *         that is different from what expected (the corresponding name in {@code skip}).
	 * @throws ClassFileNotAccessibleException if some class in {@code skip} has
	 *         a superclass/superinterface that it cannot access.
	 * @throws ContradictionException  if some initialization assumption is
	 *         contradicted.
	 */
	public static void ensureClassInitialized(State state, ExecutionContext ctx, Set<String> skip, Signature boxExceptionMethodSignature, ClassFile... classFile) 
	throws InvalidInputException, DecisionException, ClasspathException, HeapMemoryExhaustedException, InterruptException, 
	ClassFileNotFoundException, IncompatibleClassFileException, ClassFileIllFormedException, 
	BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, ClassFileNotAccessibleException, 
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

	private static final class ClassInitializer {
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
		ClassFileIllFormedException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, ClassFileNotAccessibleException {
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
		 * Implements class initialization.
		 * 
		 * @param classFile the {@link ClassFile}s of the classes to be initialized.
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
		private boolean initialize(ClassFile... classFile)
		throws InvalidInputException, DecisionException, 
		ClasspathException, HeapMemoryExhaustedException, ContradictionException {
			phase1(false, classFile);
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
			phase4();
			return false;
		}

		private static boolean hasANonStaticImplementedMethod(ClassFile cf) {
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
		 * @param recurSuperinterfaces if {@code true}, recurs phase 1 over
		 *        {@code classFile}'s superinterfaces even if 
		 *        {@code classFile.}{@link ClassFile#isInterface() isInterface}{@code () == true}.
		 * @param classFile the {@link ClassFile}s of the classes to be initialized.
		 * 
		 * @throws InvalidInputException if {@code classFile} is null.
		 * @throws DecisionException if the decision procedure fails.
		 * @throws ContradictionException  if some initialization assumption is
		 *         contradicted.
		 */
		private void phase1(boolean recurSuperinterfaces, ClassFile... classFiles)
		throws InvalidInputException, DecisionException, ContradictionException {
			for (ClassFile classFile : classFiles) {
				//if classFile is already in this.classesForPhase3
				//we must reschedule it to respect the visiting order
				//of JVMS v8 section 5.5, point 7
				if (this.classesForPhase3.contains(classFile)) {
					this.classesForPhase3.remove(classFile);
					this.classesForPhase3.add(classFile);
					continue;
				}

				//if classFile is in the skip set, skip it 
				if (this.skip.contains(classFile.getClassName())) {
					continue;
				}

				//if there is a Klass object for classFile in the state, 
				//and is in the "initialization started" status (means 
				//initialization in progress or already initialized),
				//skip it
				final boolean klassAlreadyExists = this.s.existsKlass(classFile);
				if (klassAlreadyExists && this.s.getKlass(classFile).initializationStarted()) {
					continue;
				}

				//saves classFile in the list of the newly
				//created Klasses
				this.classesForPhase2.add(classFile);

				//decides whether the class is assumed pre-initialized and whether
				//a symbolic or concrete Klass object should be created
				//TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Withdraw this assumption and branch.
				final Initialization isSymbolicInitialized = isSymbolicInitialized(classFile);

				//creates the Klass object
				if (isSymbolicInitialized == Initialization.SYMBOLIC_INITIALIZED) {
					//creates a symbolic Klass
					this.s.ensureKlassSymbolic(this.ctx.getCalculator(), classFile);
				} else {
					//creates a concrete Klass and schedules it for phase 3
					this.s.ensureKlass(this.ctx.getCalculator(), classFile);
					if (JAVA_OBJECT.equals(classFile.getClassName())) {
						this.pushClinitFor_JAVA_OBJECT = true;
					} else {
						this.classesForPhase3.add(classFile);
					}
				}

				//pushes the assumption
				if (!klassAlreadyExists) { //if klassAlreadyExists, the clause is already present
					if (isSymbolicInitialized == Initialization.SYMBOLIC_INITIALIZED || 
					isSymbolicInitialized == Initialization.CONCRETE_INITIALIZED) { 
						final Klass k = this.s.getKlass(classFile);
						this.s.assumeClassInitialized(classFile, k);
					} else {
						this.s.assumeClassNotInitialized(classFile);
					}
				}

				//if the created Klass is concrete and pre-initialized, 
				//schedules the Klass to become symbolic (when
				//the corresponding flag is active)
				if (isSymbolicInitialized == Initialization.CONCRETE_INITIALIZED && this.makePreInitClassesSymbolic
				&& !JBSE_BASE.equals(classFile.getClassName()) /* HACK */) {
					this.preInitializedClasses.add(classFile);
				}

				//if classFile denotes a class rather than an interface, 
				//then recursively performs phase1 
				//on its superclass and superinterfaces, according to
				//JVMS v8 section 5.5, point 7
				if (!classFile.isInterface() || recurSuperinterfaces) {
					for (ClassFile superinterface : Util.reverse(classFile.getSuperInterfaces())) {
						if (hasANonStaticImplementedMethod(classFile)) {
							phase1(true, superinterface);
						}
					}
					final ClassFile superclass = classFile.getSuperclass();
					if (superclass != null) {
						phase1(false, superclass);
					}
				}

				//if classFile denotes an array class, then it shall
				//not be initialized (it has no static initializer), 
				//but its creation triggers the creation of its member
				//class, see JVMS v8 section 5.3.3
				if (classFile.isArray()) {
					phase1(false, classFile.getMemberClass());
				}
			}
		}
		
		private enum Initialization { SYMBOLIC_INITIALIZED, CONCRETE_INITIALIZED, CONCRETE_NOTINITIALIZED };
		
		private Initialization isSymbolicInitialized(ClassFile classFile) 
		throws InvalidInputException, DecisionException {
			final ClassHierarchy hier = this.s.getClassHierarchy();
			final boolean symbolicKlass;
			boolean assumeInitialized = false; //bogus initialization to keep the compiler happy
			
			final boolean klassAlreadyExists = this.s.existsKlass(classFile);
			if (klassAlreadyExists) {
				symbolicKlass = this.s.getKlass(classFile).isSymbolic();
				//search assumeInitialized in the path condition - if there is a 
				//Klass in the state there must also be a path condition clause
				boolean found = false;
				for (Clause c : this.s.getPathCondition()) {
					if (c instanceof ClauseAssumeClassInitialized) {
						if (((ClauseAssumeClassInitialized) c).getClassFile().equals(classFile)) {
							found = true;
							assumeInitialized = true;
						}
					} else if (c instanceof ClauseAssumeClassNotInitialized) {
						if (((ClauseAssumeClassNotInitialized) c).getClassFile().equals(classFile)) {
							found = true;
							assumeInitialized = false;
						}
					}
				}
				if (!found) {
					throw new UnexpectedInternalException("Ill-formed state: Klass present in the static store but ClassFile not present in the path condition.");
				}
			} else {
				if (this.s.phase() == Phase.PRE_INITIAL) {
					symbolicKlass = false; //...and they are also assumed to be pure (or unmodified since their initialization)
					assumeInitialized = true; //all pre-initial class are assumed to be pre-initialized...
				} else if (this.ctx.decisionProcedure.isSatInitialized(classFile)) { 
					final boolean shallRunStaticInitializer = classFile.isPure() || this.ctx.classHasAPureInitializer(hier, classFile) || this.ctx.classInvariantAfterInitialization(classFile);
					symbolicKlass = !shallRunStaticInitializer;
					assumeInitialized = true;
				} else {
					symbolicKlass = false;
					assumeInitialized = false;
				}
			}
			
			//invariant: symbolicKlass implies assumeInitialized
			final Initialization retVal;
			if (symbolicKlass && assumeInitialized) {
				retVal = Initialization.SYMBOLIC_INITIALIZED;
			} else if (!symbolicKlass && assumeInitialized) {
				retVal = Initialization.CONCRETE_INITIALIZED;
			} else if (!symbolicKlass && !assumeInitialized) {
				retVal = Initialization.CONCRETE_NOTINITIALIZED;
			} else {
				throw new UnexpectedInternalException("Found class that is symbolic and is not assumed to be pre-initialized");
			}
			
			return retVal;
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
					//sig is directly extracted from the classfile, 
					//so no resolution is necessary
					setConstantField(k, sig, classFile);
				}
			}
		}
		
		private void setConstantField(Klass k, Signature sig, ClassFile classFile) 
		throws HeapMemoryExhaustedException {
			try {
				if (classFile.isFieldConstant(sig)) {
					Value v = null; //to keep the compiler happy
					final ConstantPoolValue cpv = classFile.fieldConstantValue(sig);
					if (cpv instanceof ConstantPoolPrimitive) {
						v = this.ctx.getCalculator().val_(cpv.getValue());
					} else if (cpv instanceof ConstantPoolString) {
						final String stringLit = ((ConstantPoolString) cpv).getValue();
						s.ensureStringLiteral(this.ctx.getCalculator(), stringLit);
						v = s.referenceToStringLiteral(stringLit);
					} else { //should never happen
						/* 
						 * TODO is it true that it should never happen? Especially, 
						 * what about ConstantPoolClass/MethodType/MethodHandle values? 
						 * Give another look at the JVMS and determine whether other kind 
						 * of constant static fields may be present.
						 */
						failExecution("Unexpected constant from constant pool (neither primitive nor String)."); 
						//TODO put string in constant or throw better exception
					}
					k.setFieldValue(sig, v);
				}
			} catch (FieldNotFoundException | AttributeNotFoundException | 
			InvalidIndexException | InvalidInputException | ClassFileIllFormedException e) {
				//this should never happen
				failExecution(e);
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
					exceptionBoxFrameYetToPush = invokeClinit(classFile, exceptionBoxFrameYetToPush);
				}
				if (this.pushClinitFor_JAVA_OBJECT) {
					invokeClinitJavaObject(exceptionBoxFrameYetToPush);
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
		
		private boolean invokeClinit(ClassFile classFile, boolean exceptionBoxFrameYetToPush) 
		throws HeapMemoryExhaustedException, NullMethodReceiverException, MethodNotFoundException, 
		MethodCodeNotFoundException, InvalidSlotException, InvalidTypeException, InvalidProgramCounterException, 
		ThreadStackEmptyException {
			final Signature sigClinit = new Signature(classFile.getClassName(), "()" + Type.VOID, "<clinit>");
			if (classFile.hasMethodImplementation(sigClinit)) {
				try {
					if (this.preInitializedClasses.contains(classFile)) {
						this.s.ensureStringLiteral(this.ctx.getCalculator(), classFile.getClassName());
						this.s.pushFrame(this.ctx.getCalculator(), this.cf_JBSE_BASE, JBSE_BASE_MAKEKLASSSYMBOLIC, root(), 0, this.ctx.getCalculator().valInt(classFile.getDefiningClassLoader()), this.s.referenceToStringLiteral(classFile.getClassName()));
						++this.createdFrames;
					}
					if (this.boxExceptionMethodSignature != null && exceptionBoxFrameYetToPush) {
						this.s.pushFrame(this.ctx.getCalculator(), this.cf_JBSE_BASE, this.boxExceptionMethodSignature, root(), 0);
						exceptionBoxFrameYetToPush = false;
						++this.createdFrames;
					}
					this.s.pushFrame(this.ctx.getCalculator(), classFile, sigClinit, root(), 0);
					++this.createdFrames;
				} catch (InvalidInputException e) {
					//this should never happen
					failExecution("Could not find the classfile for " + classFile.getClassName() + " or for jbse/base/Base.");
				}
			}
			return exceptionBoxFrameYetToPush;
		}
		
		private void invokeClinitJavaObject(boolean exceptionBoxFrameYetToPush) 
		throws NullMethodReceiverException, MethodNotFoundException, MethodCodeNotFoundException, 
		InvalidSlotException, InvalidTypeException, InvalidProgramCounterException, ThreadStackEmptyException {
			try {
				if (this.boxExceptionMethodSignature != null && exceptionBoxFrameYetToPush) {
					this.s.pushFrame(this.ctx.getCalculator(), this.cf_JBSE_BASE, this.boxExceptionMethodSignature, root(), 0);
					++this.createdFrames;
				}
				final Signature sigClinit_JAVA_OBJECT = new Signature(JAVA_OBJECT, "()" + Type.VOID, "<clinit>");
				final ClassFile cf_JAVA_OBJECT = this.s.getClassHierarchy().loadCreateClass(JAVA_OBJECT);
				this.s.pushFrame(this.ctx.getCalculator(), cf_JAVA_OBJECT, sigClinit_JAVA_OBJECT, root(), 0);
				++this.createdFrames;
			} catch (ClassFileNotFoundException | IncompatibleClassFileException | 
			ClassFileIllFormedException | BadClassFileVersionException | 
			RenameUnsupportedException | WrongClassNameException | 
			InvalidInputException | ClassFileNotAccessibleException e) {
				//this should never happen
				failExecution("Could not find the classfile for java.lang.Object.");
			}
		}

		/**
		 * Phase 4 sets all the created {@link Klass}es with a pushed
		 * <clinit> frame to the "initialization started" status and 
		 * all the {@link Klass}es without a pushed <clinit> frame to
		 * the "intialization completed" status.
		 * @throws FrozenStateException if {@code this.s} is frozen.
		 */
		private void phase4() throws FrozenStateException {
			for (ClassFile classFile : this.classesForPhase3) {
				final Signature sigClinit = new Signature(classFile.getClassName(), "()" + Type.VOID, "<clinit>");
				if (classFile.hasMethodImplementation(sigClinit)) {
					this.s.getKlass(classFile).setInitializationStarted();
				} else {
					this.s.getKlass(classFile).setInitializationCompleted(); //nothing else to do
				}
			}
			if (this.pushClinitFor_JAVA_OBJECT) {
				try {
					final ClassFile cf_JAVA_OBJECT = this.s.getClassHierarchy().loadCreateClass(JAVA_OBJECT);
					this.s.getKlass(cf_JAVA_OBJECT).setInitializationStarted();
				} catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException
				| ClassFileNotAccessibleException | IncompatibleClassFileException
				| BadClassFileVersionException | RenameUnsupportedException | WrongClassNameException e) {
					//this should never happen
					failExecution(e);
				}
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
			throwNew(this.s, this.ctx.getCalculator(), this.failure);
		}
	}

	/** Do not instantiate! */
	private UtilClassInitialization() {
		//nothing to do
	}
}
