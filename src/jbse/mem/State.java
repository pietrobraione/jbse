package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_COUNT;
import static jbse.bc.Signatures.JAVA_STRING_HASH;
import static jbse.bc.Signatures.JAVA_STRING_OFFSET;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.common.Type.isPrimitiveBinaryClassName;
import static jbse.common.Util.ROOT_FRAME_MONIKER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jbse.bc.ClassFile;
import jbse.bc.ClassFileFactory;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.ExceptionTable;
import jbse.bc.ExceptionTableEntry;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Objekt.Epoch;
import jbse.mem.exc.CannotRefineException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.SymbolFactory;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represents the state of execution.
 */
public final class State implements Cloneable {
	/** The slot number of the "this" (method receiver) object. */
	private static final int ROOT_THIS_SLOT = 0;

	/** 
	 * The identifier of the state in the execution tree.
	 */
	private String identifier = "";

	/** The sequence number of the state along an execution tree branch. */
	private int sequenceNumber = 0;

	/** 
	 * Flag indicating whether the current state was produced by a
	 * branching decision.
	 */
	private boolean branchingDecision = false;
	
	/** The depth of the state, i.e., the number of branch points over it. */
	private int depth = 0;

	/** The count of the state, i.e., the number of states from the previous branch point. */
	private int count = 0;

	/** The string literals. */
	private HashMap<String, ReferenceConcrete> stringLiterals = new HashMap<>();

    /** The class objects for nonprimitive types. */
    private HashMap<String, ReferenceConcrete> classes = new HashMap<>();

    /** The class objects for primitive types. */
    private HashMap<String, ReferenceConcrete> classesPrimitive = new HashMap<>();

	/** The JVM stack of the current execution thread. */
	private ThreadStack stack = new ThreadStack();

	/** The JVM heap. */
	private Heap heap = new Heap();

	/** The JVM static method area. */
	private StaticMethodArea staticMethodArea = new StaticMethodArea();

	/** The path condition of the state in the execution tree. */
	private PathCondition pathCondition = new PathCondition();

	/** The number of pushed path condition clauses from the last reset. */ 
	private int nPushedClauses;

	/** {@code true} iff the state is stuck. */
	private boolean stuck = false;

	/** 
	 * The exception raised to the root method's caller, 
	 * meaningful only when {@code this.stuck == true}. 
	 */ 
	private Reference exc = null;

	/** 
	 * The return value for the root method's caller, 
	 * meaningful only when {@code this.stuck == true}. 
	 */ 
	private Value val = null;

	/** May symbolic execution from this state violate an assumption? */
	private boolean mayViolateAssumption = true;

	/** {@code true} iff the next bytecode must be executed in its WIDE variant. */
	private boolean wide = false;
	
	/** The {@link Calculator}. */
	private final Calculator calc;
    
    /** 
     * The object that fetches classfiles from the classpath, stores them, 
     * and allows visiting the whole class/interface hierarchy. 
     */
    private final ClassHierarchy classHierarchy;

    /** 
     * The generator for unambiguous symbol identifiers; mutable
     * because different states at different branch may have different
     * generators, possibly starting from the same numbers. 
     */
    private SymbolFactory symbolFactory;
    
	/**
	 * Constructor of an empty State.
	 * 
	 * @param cp a {@link Classpath}.
	 * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
	 *        The class must have an accessible constructor with two parameters, the first a 
	 *        {@link ClassFileStore}, the second a {@link Classpath}.
	 * @param expansionBackdoor a 
	 *        {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>}
	 *        associating class names to sets of names of their subclasses. It 
	 *        is used in place of the class hierarchy to perform expansion.
	 * @param calc a {@link Calculator}. It will be used to do all kinds of calculations
	 *        on concrete and symbolic values.
	 * @throws InvalidClassFileFactoryClassException in the case {@link fClass}
	 *         has not the expected features (missing constructor, unaccessible 
	 *         constructor...).
	 */
	public State(Classpath cp, 
	             Class<? extends ClassFileFactory> fClass, 
	             Map<String, Set<String>> expansionBackdoor, 
	             Calculator calc) 
	throws InvalidClassFileFactoryClassException {
        this.calc = calc;
		this.classHierarchy = new ClassHierarchy(cp, fClass, expansionBackdoor);
		this.symbolFactory = new SymbolFactory(this.calc);
	}

	
	/**
	 * Getter for this state's classpath.
	 * 
	 * @return a {@link Classpath}.
	 */
	public Classpath getClasspath() {
		return this.classHierarchy.getClasspath();
	}
	
	/**
	 * Getter for this state's calculator.
	 * 
	 * @return a {@link Calculator}.
	 */
	public Calculator getCalculator() {
		return this.calc;
	}

	/**
	 * Returns and deletes the value from the top of the current 
	 * operand stack.
	 * 
	 * @return the {@link Value} on the top of the current 
	 * operand stack.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws InvalidNumberOfOperandsException if the current operand 
	 * stack is empty
	 */
	public Value popOperand() throws ThreadStackEmptyException, InvalidNumberOfOperandsException {
		return this.stack.currentFrame().pop();
	}
	
	public void popOperands(int n) throws ThreadStackEmptyException, InvalidNumberOfOperandsException {
	    this.stack.currentFrame().pop(n);
	}

	/**
	 * Returns the topmost element in the current operand stack, 
	 * without removing it.
	 * 
	 * @return a {@link Value}.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws InvalidNumberOfOperandsException if the current operand
	 *         stack is empty. 
	 */
	public Value topOperand() 
	throws ThreadStackEmptyException, InvalidNumberOfOperandsException {
		return this.stack.currentFrame().top();
	}

	/**
	 * Puts a {@link Value} on the top of the current operand stack.
	 * If tracking is active and the value is a {@link Reference}, it 
	 * informs the tracker about this.
	 * 
	 * @param val {@link Value} to put on the top of the current 
	 * operand stack.
	 * @throws ThreadStackEmptyException if the thread stack is empty. 
	 */
	public void pushOperand(Value val) throws ThreadStackEmptyException {
		this.stack.currentFrame().push(val);		
	}

    /**
     * Clears the current operand stack.
     * 
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public void clearOperands() throws ThreadStackEmptyException {
        this.stack.currentFrame().clear();
    }

	/**
	 * Checks whether this state may violate some
	 * assumption not yet assumed.
	 * 
	 * @return {@code true} iff it can.
	 */
	public boolean mayViolateAssumption() {
		return this.mayViolateAssumption;
	}

	/**
	 * Disables the possibility of having 
	 * other assumptions being issued later
	 * during symbolic execution.
	 */
	public void disableAssumptionViolation() {
		this.mayViolateAssumption = false;
	}
	
	/**
	 * Returns the {@link Signature} of the  
	 * current method.
	 * 
	 * @return a {@link Signature}.
	 * @throws ThreadStackEmptyException if the stack is empty.
	 */
	public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
		return this.stack.currentFrame().getCurrentMethodSignature();
	}
	
	/**
	 * Returns the {@link Signature} of the  
	 * root method.
	 * 
	 * @return a {@link Signature}.
	 * @throws ThreadStackEmptyException if the stack is empty.
	 */
	public Signature getRootMethodSignature() throws ThreadStackEmptyException {
		return this.stack.rootFrame().getCurrentMethodSignature();
	}

	/**
	 * Returns a {@link Reference} to the root object, i.e., the 
	 * receiver of the entry method of the execution.
	 * 
	 * @return A {@link Reference} to the root object in the heap 
	 * of the current state, or {@code null} if the root method is static.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public Reference getRootObjectReference() throws ThreadStackEmptyException {
		final Signature s = getRootMethodSignature();
		try {
			if (this.classHierarchy.getClassFile(s.getClassName()).isMethodStatic(s)) {
				return null;
			} else {
				try {
					return (Reference) this.stack.rootFrame().getLocalVariableValue(ROOT_THIS_SLOT);
				} catch (InvalidSlotException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			}
		} catch (MethodNotFoundException | BadClassFileException e) {
			throw new UnexpectedInternalException(e);
		}
	}

    /**
     * Returns the name of a local variable in the current frame 
     * as declared in the debug information of the class.
     *  
     * @param slot the number of the slot of a local variable.
     * @return a {@link String} containing the name of the local
     *         variable at {@code slot} as from the available debug 
     *         information, depending on the current program counter
     *         {@code curPC}, or {@code null} if no debug information is 
     *         available for the {@code (slot, curPC)} combination.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public String getLocalVariableDeclaredName(int slot) throws ThreadStackEmptyException {
        return this.stack.currentFrame().getLocalVariableDeclaredName(slot);
    }

	/**
	 * Returns the value of a local variable in the current frame.
	 * 
	 * @param slot an {@code int}, the slot of the local variable.
	 * @return a {@link Value}.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws InvalidSlotException if {@code slot} is not a valid slot number.
	 */
	public Value getLocalVariableValue(int slot) throws ThreadStackEmptyException, InvalidSlotException {
		return this.stack.currentFrame().getLocalVariableValue(slot);
	}

	/**
	 * Stores a value into a specific local variable of the current 
	 * frame.
	 * 
	 * @param an {@code int}, the slot of the local variable.
	 * @param item the {@link Value} to be stored.  
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws InvalidSlotException if {@code slot} is not a valid slot number.
	 */
	public void setLocalVariable(int slot, Value val) throws ThreadStackEmptyException, InvalidSlotException {
		this.stack.currentFrame().setLocalVariableValue(slot, this.stack.currentFrame().getProgramCounter(), val);
	}


	/**
	 * Tests whether a class is initialized.
	 * 
	 * @param className the name of the class.
	 * @return {@code true} iff the class {@code className} 
	 *         has been initialized. Note that in the positive
	 *         case the {@link State}'s static store contains
	 *         a {@link Klass} object for {@link className}.
	 */
	public boolean initialized(String className) {
		return this.staticMethodArea.contains(className);
	}


	/**
	 * Tests whether a symbolic reference is resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff {@code ref} is resolved.
	 *         Note that in the positive case either the 
	 *         {@link State}'s heap contains an {@link Objekt}
	 *         at the position indicated by {@code ref}, 
	 *         or {@code ref} is resolved by null.
     * @throws NullPointerException if {@code ref == null}.
	 */
	public boolean resolved(ReferenceSymbolic ref) {
		return this.pathCondition.resolved(ref);
	}

	/**
	 * Returns the heap position associated to a resolved 
	 * symbolic reference.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return a {@code long}, the heap position to which
	 * {@code ref} has been resolved, or {@code null} if
     * {@link #resolved}{@code (ref) == false}.
     * @throws NullPointerException if {@code ref == null}.
	 */
    public long getResolution(ReferenceSymbolic ref) {
    	return this.pathCondition.getResolution(ref);
    }
    
    /**
     * Tests whether a reference is null.
     * 
     * @param ref a {@link Reference}.
     * @return {@code true} iff {@code ref} is {@link Null}, 
     * or if is a symbolic reference resolved to null.
     * @throws NullPointerException if {@code ref == null}.
     */
    public boolean isNull(Reference ref) {
    	if (ref instanceof ReferenceSymbolic) {
    		final ReferenceSymbolic refS = (ReferenceSymbolic) ref;
    		return (resolved(refS) && getResolution(refS) == jbse.mem.Util.POS_NULL);
    	} else {
    		return (ref == Null.getInstance());
    	}
    }
        
    /**
     * Gets an object from the heap.
     * 
     * @param ref a {@link Reference}.
     * @return the {@link Objekt} referred to by {@code ref}, or 
     *         {@code null} if {@code ref} does not refer to 
     *         an object in the heap, i.e.
     *         <ul>
     *         <li>{@code ref} is {@link Null}, or</li> 
     *         <li>{@code ref} is symbolic and resolved to null, or</li> 
     *         <li>{@code ref} is symbolic and unresolved.</li>
     *         </ul>
     * @throws NullPointerException if {@code ref == null}.
     */
    public Objekt getObject(Reference ref) {
		final Objekt retVal;
    	if (ref.isSymbolic()) {
    		final ReferenceSymbolic refSymbolic = (ReferenceSymbolic) ref;
    		if (resolved(refSymbolic)) {
    			final long pos = getResolution(refSymbolic);
    			retVal = this.heap.getObject(pos);
    		} else {
    			retVal = null;
    		}
    	} else {
    		final ReferenceConcrete refConcrete = (ReferenceConcrete) ref;
    		final long pos = refConcrete.getHeapPosition();
			retVal = this.heap.getObject(pos);
    	}
    	return retVal;
    }
    
    /**
     * Gets a symbolic object as it was initially.
     * 
     * @param ref a {@link Reference}.
     * @return the symbolic {@link Objekt} referred to by {@code ref} 
     *         in the state it was at its epoch (equivalently, at the
     *         moment of its assumption), or 
     *         {@code null} if {@code ref} does not refer to 
     *         anything (e.g., is {@link Null}, or is an unresolved 
     *         symbolic reference, or is resolved to null), or the 
     *         reference is concrete and refers to a concrete object.
     */
    //TODO eliminate this method!!!
    public Objekt getObjectInitial(Reference ref) {
		final long pos;
    	if (ref.isSymbolic()) {
    		final ReferenceSymbolic refSymbolic = (ReferenceSymbolic) ref;
    		if (resolved(refSymbolic)) {
    			pos = getResolution(refSymbolic);
    		} else {
    			return null;
    		}
    	} else {
    		final ReferenceConcrete refConcrete = (ReferenceConcrete) ref;
    		pos = refConcrete.getHeapPosition();
    	}
    	
    	//TODO extract this code and share with DecisionProcedureAlgorithms.getPossibleAliases
    	for (Clause c : this.pathCondition.getClauses()) {
    		if (c instanceof ClauseAssumeExpands) {
    			final ClauseAssumeExpands cExpands = (ClauseAssumeExpands) c;
    			if (cExpands.getHeapPosition() == pos) {
    				return cExpands.getObjekt();
    			}
    		}
    	}
    	return null;
    }
    
	/**
	 * Returns the state's {@link ClassHierarchy}. 
	 * 
	 * @return a {@link ClassHierarchy}.
	 */
	public ClassHierarchy getClassHierarchy() {
		return this.classHierarchy;
	}
	
	/**
	 * Returns the {@link Klass} object corresponding to 
	 * a given class name.
	 * 
	 * @param className the name of the class.
	 * @return the {@link Klass} object corresponding to 
	 *         the memory representation of the class 
	 *         {@code className}, or {@code null} 
	 *         if the class has not been initialized.
	 */
	public Klass getKlass(String className) {
		return this.staticMethodArea.get(className);
	}

	/**
	 * Creates a new {@link Array} of a given class in the heap of 
	 * the state.
	 * 
	 * @param initValue
	 *        a {@link Value} for initializing the array; if {@code initValue == null}
	 *        the default value for the array member type is used for initialization.
	 * @param length
	 *        the number of elements in the array.
	 * @param arraySignature
	 *        a {@link String}, the type of the array.
	 * @return a new  {@link ReferenceConcrete} to the newly created object.
	 * @throws InvalidTypeException if {@code arraySignature} is invalid.
	 */
	public ReferenceConcrete createArray(Value initValue, Primitive length, String arraySignature) 
	throws InvalidTypeException {
		final Array a = new Array(this.calc, false, initValue, length, arraySignature, null, Epoch.EPOCH_AFTER_START);
		return new ReferenceConcrete(this.heap.addNew(a));
	}

	/**
	 * Creates a new {@link Instance} of a given class in the 
	 * heap of the state. The {@link Instance}'s fields are initialized 
	 * with the default values for each field's type.
	 * It cannot create instances of the {@code java.lang.Class} class.
	 * 
	 * @param className the name of the class of the new object.
	 * @return a {@link ReferenceConcrete} to the newly created object.
	 */
	public ReferenceConcrete createInstance(String className) {
	    if (className.equals(JAVA_CLASS)) {
	        throw new RuntimeException(); //TODO better exception
	    }
		final Signature[] fieldsSignatures = this.classHierarchy.getAllFieldsInstance(className);
		final Instance myObj = new Instance(this.calc, className, null, Epoch.EPOCH_AFTER_START, fieldsSignatures);
		return new ReferenceConcrete(this.heap.addNew(myObj));
	}
	
    /**
     * Creates a new {@link Instance} of {@code java.lang.Class} in the 
     * heap of the state (more precisely, creates a {@link Instance_JAVA_CLASS}).
     * Its fields are initialized with the default values for each 
     * field's type (which should not be a problem since all the fields are transient).
     * 
     * @param representedClass the name of the class the created {@code Instance_JAVA_CLASS}
     *        represents.
     * @return a {@link ReferenceConcrete} to the newly created object.
     */
    private ReferenceConcrete createInstance_JAVA_CLASS(String representedClass) {
        final Signature[] fieldsSignatures = this.classHierarchy.getAllFieldsInstance(JAVA_CLASS);
        final Instance myObj = new Instance_JAVA_CLASS(this.calc, null, Epoch.EPOCH_AFTER_START, representedClass, fieldsSignatures);
        return new ReferenceConcrete(this.heap.addNew(myObj));
    }
    
	/**
	 * Creates a concrete {@link Klass} object and loads it in the 
	 * static area of this state. It does not initialize the constant 
     * fields nor loads on the stack of the state the frames for the
     * {@code <clinit>} methods. It does not create {@link Klass} objects
     * for superclasses}.
	 * If the {@link Klass} exists it does nothing.
	 * 
	 * @param className the name of the class to be loaded. The method 
	 *        creates and loads a {@link Klass} object only for {@code className}, 
	 *        not for its superclasses in the hierarchy.
     * @throws BadClassFileException when the classfile for {@code className} 
     *         cannot be found in the classpath or is ill-formed.
	 * @throws InvalidIndexException if the access to the class constant pool fails.
	 */
	private void ensureKlass(String className) throws BadClassFileException, InvalidIndexException {
	    if (initialized(className)) {
	        return;
	    }
		final ClassFile classFile = this.getClassHierarchy().getClassFile(className);
		final Signature[] fieldsSignatures = classFile.getFieldsStatic();
		final Klass k = new Klass(State.this.calc, null, Objekt.Epoch.EPOCH_AFTER_START, fieldsSignatures);
		this.staticMethodArea.set(className, k);
	}

	/**
	 * Creates a symbolic {@link Klass} object and loads it in the 
	 * static area of this state. It does not initialize the constant 
	 * fields. It does not create {@link Klass} objects
     * for superclasses.
	 * If the {@link Klass} already exists it does nothing.
	 * 
	 * @param className the name of the class to be loaded.
     * @throws BadClassFileException when the classfile for {@code className} 
     *         cannot be found in the classpath or is ill-formed.
	 * @throws InvalidIndexException if the access to the class 
	 *         constant pool fails.
	 */
	public void ensureKlassSymbolic(String className) throws BadClassFileException, InvalidIndexException {
        if (initialized(className)) {
            return;
        }
		final ClassFile classFile = this.getClassHierarchy().getClassFile(className);
		final Signature[] fieldsSignatures = classFile.getFieldsStatic();
		final Klass k = new Klass(this.calc, "[" + className + "]", Objekt.Epoch.EPOCH_BEFORE_START, fieldsSignatures);
		initWithSymbolicValues(k);
        this.staticMethodArea.set(className, k);
	}

	/**
	 * Creates a new {@link Objekt} of a given class in the heap of 
	 * the state. The {@link Objekt}'s fields are initialized with symbolic 
	 * values.
	 *  
	 * @param type the name of either a class or an array type.
	 * @param origin a {@link String}, the origin of the object.
	 * @return a {@code long}, the position in the heap of the newly 
	 *         created object.
	 * @throws NullPointerException if {@code origin} is {@code null}.
	 * @throws InvalidTypeException if {@code type} is invalid.
	 */
	private long createObjectSymbolic(String type, String origin) 
	throws InvalidTypeException {
		if (origin == null) {
			throw new NullPointerException(); //TODO improve?
		}
		final Objekt myObj = 
				(Type.isArray(type) ? newArraySymbolic(type, origin) : newInstanceSymbolic(type, origin));
		final long pos = this.heap.addNew(myObj);
		return pos;
	}

	private Array newArraySymbolic(String arraySignature, String origin) 
	throws InvalidTypeException {
		final Primitive length = (Primitive) createSymbol("" + Type.INT, origin + ".length");
		final Array obj = new Array(this.calc, true, null, length, arraySignature, origin, Epoch.EPOCH_BEFORE_START);
		return obj;
	}

	private Instance newInstanceSymbolic(String className, String origin) {
		final Signature[] fieldsSignatures = this.classHierarchy.getAllFieldsInstance(className);
		final Instance obj = new Instance(this.calc, className, origin, Epoch.EPOCH_BEFORE_START, fieldsSignatures);
		initWithSymbolicValues(obj);
		return obj;
	}

	/**
	 * Initializes an {@link Objekt} with symbolic values.
	 * 
	 * @param myObj an {@link Objekt} which will be initialized with 
	 *              symbolic values.
	 */
	private void initWithSymbolicValues(Objekt myObj) {
		for (final Signature myActualSignature : myObj.getFieldSignatures()) {
			//gets the field signature and name
			final String tmpDescriptor = myActualSignature.getDescriptor();
			final String tmpName = myActualSignature.getName();

			//builds a symbolic value from signature and name 
			//and assigns it to the field
			myObj.setFieldValue(myActualSignature, 
					createSymbol(tmpDescriptor, myObj.getOrigin() + "." + tmpName));
		}
	}
	
	/**
	 * Checks if there is a string literal in this state's heap.
	 * 
	 * @param stringLit a {@link String} representing a string literal.
	 * @return {@code true} iff there is a {@link Instance} in 
     *         this state's {@link Heap} corresponding to {@code stringLit}.
	 */
	public boolean hasStringLiteral(String stringLit) {
	    return this.stringLiterals.containsKey(stringLit);
	}

	/**
	 * Returns a {@link ReferenceConcrete} to a {@code java.lang.String} 
	 * corresponding to a string literal. 
	 * 
	 * @param stringLit a {@link String} representing a string literal.
	 * @return a {@link ReferenceConcrete} to the {@link Instance} in 
	 *         this state's {@link Heap} corresponding to 
	 *         {@code stringLit}, or {@code null} if such instance does not
	 *         exist. 
	 */
	public ReferenceConcrete referenceToStringLiteral(String stringLit) {
	    return this.stringLiterals.get(stringLit);
	}

    /**
     * Creates an {@link Instance} of class {@code java.lang.String} 
     * in this state's heap corresponding to a string literal sidestepping 
     * the constructors of {@code java.lang.String} to avoid incredible 
     * circularity issues with string constant fields. Does not
     * manage the creation of the {@link Klass} for {@code java.lang.String}
     * and its members. If the literal already exists, does nothing.
     * 
     * @param stringLit a {@link String} representing a string literal.
     */
	public void ensureStringLiteral(String stringLit) {
	    if (hasStringLiteral(stringLit)) {
	        return;
	    }
		final ReferenceConcrete value = createArrayOfChars(stringLit);
		final Simplex hash = this.calc.valInt(stringLit.hashCode());
		final Simplex zero = this.calc.valInt(0);
		final Simplex length = this.calc.valInt(stringLit.length());
		
		final ReferenceConcrete retVal = createInstance(JAVA_STRING);
		final Instance i = (Instance) this.getObject(retVal);
		i.setFieldValue(JAVA_STRING_VALUE,  value);
		i.setFieldValue(JAVA_STRING_HASH,   hash);
		i.setFieldValue(JAVA_STRING_OFFSET, zero);
		i.setFieldValue(JAVA_STRING_COUNT,  length);
		
        this.stringLiterals.put(stringLit, retVal);
	}
	
	private ReferenceConcrete createArrayOfChars(String value) {
		final Simplex stringLength = this.calc.valInt(value.length());
		final ReferenceConcrete retVal;
		try {
			retVal = createArray(null, stringLength, "" + Type.ARRAYOF + Type.CHAR);
		} catch (InvalidTypeException e) {
			//this should never happen 
			throw new UnexpectedInternalException(e);
		}
		try {
		    final Array a = (Array) this.getObject(retVal);
		    for (int k = 0; k < value.length(); ++k) {
		        final char c = value.charAt(k);
		        a.setFast(this.calc.valInt(k), this.calc.valChar(c));
		    }
		} catch (ClassCastException | InvalidOperandException | 
		         InvalidTypeException | FastArrayAccessNotAllowedException e) {
		    //this should never happen 
		    throw new UnexpectedInternalException(e);
		}

		return retVal;
	}
    
    /**
     * Checks if there is an {@link Instance} of {@code java.lang.Class} 
     * in this state's heap for some class.
     * 
     * @param className a {@link String} representing a class name.
     * @return {@code true} iff there is a {@link Instance} of {@code java.lang.Class} in 
     *         this state's {@link Heap} corresponding to {@code className}.
     */
    public boolean hasInstance_JAVA_CLASS(String className) {
        return this.classes.containsKey(className);
    }
    
    /**
     * Checks if there is an {@link Instance} of {@code java.lang.Class} 
     * in this state's heap for some primitive type.
     * 
     * @param typeName a {@link String} representing a primitive type
     *        binary name (see Java Language Specification 13.1).
     * @return {@code true} iff there is a {@link Instance} of {@code java.lang.Class} in 
     *         this state's {@link Heap} corresponding to {@code typeName}.
     */
    public boolean hasInstance_JAVA_CLASS_primitive(String typeName) {
        return this.classesPrimitive.containsKey(typeName);
    }
	
    /**
     * Returns an {@link Instance} of class {@code java.lang.Class} 
     * from the heap corresponding to a class name. 
     * 
     * @param className a {@link String} representing the name of a class.
     *        Classes of primitive types must be provided as their binary
     *        names (see Java Language Specification 13.1)
     * @return a {@link ReferenceConcrete} to the {@link Instance} in 
     *         {@code state}'s {@link Heap} of the class with name
     *         {@code className}, or {@code null} if such instance does not
     *         exist. 
     */
    public ReferenceConcrete referenceToInstance_JAVA_CLASS(String className) {
        final ReferenceConcrete retVal = this.classes.get(className);
        return retVal;
    }
   
    /**
     * Returns a {@link ReferenceConcrete} to an instance of 
     * {@code java.lang.Class} representing a primitive type. 
     * 
     * @param typeName a {@link String}, a primitive type
     *        binary name (see Java Language Specification 13.1).
     * @return a {@link ReferenceConcrete} to the {@link Instance_JAVA_CLASS} 
     *         in this state's {@link Heap}, representing the class of 
     *         the primitive type {@code typeName}, or {@code null} if 
     *         such instance does not exist in the heap. 
     */
    public ReferenceConcrete referenceToInstance_JAVA_CLASS_primitive(String typeName) {
        final ReferenceConcrete retVal = this.classesPrimitive.get(typeName);
        return retVal;
    }
   
    /**
     * Ensures an {@link Instance} of class {@code java.lang.Class} 
     * corresponding to a class name exists in the {@link Heap}. If
     * the instance does not exist, it resolves the class and creates 
     * it, otherwise it does nothing.
     * 
     * @param className a {@link String} representing the name of a class.
     * @throws ThreadStackEmptyException if this state has an empty thread stack.
     * @throws BadClassFileException if the classfile for {@code className}
     *         does not exist in the classpath or is ill-formed.
     * @throws ClassFileNotAccessibleException if the class {@code className} 
     *         is not accessible from {@code accessor}.
     */
    public void ensureInstance_JAVA_CLASS(String className) 
    throws ThreadStackEmptyException, BadClassFileException, 
    ClassFileNotAccessibleException {
        if (hasInstance_JAVA_CLASS(className)) {
            return;
        }
        final String currentClassName = getCurrentMethodSignature().getClassName();
        this.classHierarchy.resolveClass(currentClassName, className);
        //TODO resolve JAVA_CLASS
        final ReferenceConcrete retVal = createInstance_JAVA_CLASS(className);
        this.classes.put(className, retVal);
    }
    
    /**
     * Ensures an {@link Instance} of class {@code java.lang.Class} 
     * corresponding to a class name exists in the {@link Heap}. If
     * the instance does not exist, it resolves the class and creates 
     * it, otherwise it does nothing.
     * 
     * @param typeName a {@link String} representing a primitive type
     *        binary name (see Java Language Specification 13.1).
     * @throws ClassFileNotFoundException if {@code typeName} is not
     *         the binary name of a primitive type.
     */
    public void ensureInstance_JAVA_CLASS_primitive(String typeName) 
    throws ClassFileNotFoundException {
        if (hasInstance_JAVA_CLASS_primitive(typeName)) {
            return;
        }
        if (isPrimitiveBinaryClassName(typeName)) {
            final ReferenceConcrete retVal = createInstance_JAVA_CLASS(typeName);
            this.classesPrimitive.put(typeName, retVal);
        } else {
            throw new ClassFileNotFoundException(typeName + " is not the binary name of a primitive type");
        }
    }

	/**
	 * Unwinds the stack of this state until it finds an exception 
     * handler for an object. If the thread stack is empty after 
     * unwinding, sets the state to stuck with the unhandled exception
     * throw as a cause.
	 * 
	 * @param exceptionToThrow a {@link Reference} to a throwable 
	 *        {@link Objekt} in the state's {@link Heap}.
	 * @throws InvalidIndexException if the exception type field in a row of the exception table 
     *         does not contain the index of a valid CONSTANT_Class in the class constant pool.
     * @throws InvalidProgramCounterException if the program counter handle in a row 
     *         of the exception table does not contain a valid program counter.
	 */
	public void throwObject(Reference exceptionToThrow) 
	throws InvalidIndexException, InvalidProgramCounterException {
		//TODO check that exceptionToThrow is resolved/concrete
		final Objekt myException = getObject(exceptionToThrow);
		//TODO check that Objekt is Throwable

		//fills a vector with all the superclass names of the exception
		final ArrayList<String> excTypes = new ArrayList<String>();
		for (ClassFile f : this.classHierarchy.superclasses(myException.getType())) {
			excTypes.add(f.getClassName());
		}

		//unwinds the stack
		try {
		    while (true) {
		        if (this.stack.isEmpty()) {
		            setStuckException(exceptionToThrow);
		            return;
		        }
		        final Signature currentMethodSignature = getCurrentMethodSignature();
		        final String currentClassName = currentMethodSignature.getClassName();
		        final ExceptionTable myExTable = this.classHierarchy.getClassFile(currentClassName).getExceptionTable(currentMethodSignature);
		        final ExceptionTableEntry tmpEntry = myExTable.getEntry(excTypes, getPC());
		        if (tmpEntry == null) {
		            this.stack.pop();
		        } else {
		            clearOperands();
		            setProgramCounter(tmpEntry.getPCHandle());
		            pushOperand(exceptionToThrow);
		            return;				
		        }
		    }
		} catch (ThreadStackEmptyException | BadClassFileException | 
		         MethodNotFoundException | MethodCodeNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
	}

	/**
	 * Creates a new frame for a (nonnative) method and pushes it 
	 * on this state's stack. The actual parameters of the invocation are 
	 * initialized with values from the invoking frame's operand stack.
	 * 
	 * @param methodSignatureImpl
	 *        the {@link Signature} of the method for which the 
	 *        frame is built. The bytecode for the method will be
	 *        looked for in 
	 *        {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}.
	 * @param isRoot
	 *        {@code true} iff the frame is the root frame of symbolic
	 *        execution (i.e., on the top of the thread stack).
	 * @param returnPCOffset 
	 *        the offset from the current program counter of 
	 *        the return program counter. It is ignored if 
	 *        {@code isRoot == true}.
	 * @param args
	 *        varargs of method call arguments.
     * @throws NullMethodReceiverException when the method is not static
     *         and the first argument in {@code args} is the null reference.
	 * @throws BadClassFileException when the classfile with name 
	 *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
	 *         does not exist in the classpath or is ill-formed.
     * @throws MethodNotFoundException when the classfile with name.
     *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
     *         does not contain the method (i.e., the method is abstract
     *         or native).
	 * @throws MethodCodeNotFoundException when the classfile with name.
     *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
     *         does not contain bytecode for the method.
     * @throws InvalidSlotException when there are 
     *         too many {@code arg}s or some of their types are 
     *         incompatible with their respective slots types.
     * @throws ThreadStackEmptyException when {@code isRoot == false} and the 
     *         state's thread stack is empty.
     * @throws InvalidProgramCounterException when {@code isRoot == false} and
     *         {@code returnPCOffset} is not a valid program count offset for the
     *         state's current frame.
	 */
	public void pushFrame(Signature methodSignatureImpl, boolean isRoot, int returnPCOffset, Value... args) 
	throws NullMethodReceiverException, BadClassFileException, 
	MethodNotFoundException, MethodCodeNotFoundException, 
	InvalidSlotException, InvalidProgramCounterException, 
	ThreadStackEmptyException {
        final ClassFile classMethodImpl = this.classHierarchy.getClassFile(methodSignatureImpl.getClassName());
        final boolean isStatic = classMethodImpl.isMethodStatic(methodSignatureImpl);
		//checks the "this" parameter (invocation receiver) if necessary
		final Reference thisObjectRef;
		if (isStatic) {
			thisObjectRef = null;
		} else {
			if (args.length == 0 || !(args[0] instanceof Reference)) {
				throw new UnexpectedInternalException("Args for method invocation do not correspond to method signature."); //TODO better exception
			}
			thisObjectRef = (Reference) args[0];
			if (isNull(thisObjectRef)) {
				throw new NullMethodReceiverException();
			}
		}

        //creates the frame and sets its args
        final Frame f = new Frame(methodSignatureImpl, classMethodImpl);
		f.setArgs(args);

		//sets the frame's return program counter
		if (isRoot) {
			//do nothing, after creation the frame has already a dummy return program counter
		} else {
			this.setReturnProgramCounter(returnPCOffset);
		}

		//pushes the frame on the thread stack
		this.stack.push(f);
	}

	/**
	 * Makes symbolic arguments for the root method invocation. This includes the
	 * root object.
	 * @param f the root {@link Frame}.
	 * @param methodSignature the {@link Signature} of the root object method
	 *        to be invoked. It will be used both to create the root object
	 *        in the heap (the concrete class is the one specified in the 
	 *        signature), and to build all the symbolic parameters.
	 * @param isStatic
	 *        {@code true} iff INVOKESTATIC method invocation rules 
	 *        must be applied.
	 * 
	 * @return a {@link Value}{@code []}, the array of the symbolic parameters
	 *         for the method call. Note that the reference to the root object
	 *         is a {@link ReferenceSymbolic}.
	 */
	private Value[] makeArgsSymbolic(Frame f, Signature methodSignature, boolean isStatic) {
		//gets the method's signature
		final String[] paramsDescriptor = Type.splitParametersDescriptors(methodSignature.getDescriptor());
		final int numArgs = paramsDescriptor.length + (isStatic ? 0 : 1);

		//produces the args as symbolic values from the method's signature
		final String rootClassName = methodSignature.getClassName(); //TODO check that the root class has the method!!!
		final Value[] args = new Value[numArgs];
		for (int i = 0, slot = 0; i < numArgs; ++i) {
			//builds a symbolic value from signature and name
			final String origin = ROOT_FRAME_MONIKER + f.getLocalVariableDeclaredName(slot);
			if (slot == ROOT_THIS_SLOT && !isStatic) {
				args[i] = createSymbol(Type.REFERENCE + rootClassName + Type.TYPEEND, origin);
				//must assume {ROOT}:this expands to nonnull object (were it null the frame would not exist!)
				try {
					assumeExpands((ReferenceSymbolic) args[i], rootClassName);
				} catch (InvalidTypeException | ContradictionException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			} else {
				args[i] = createSymbol(paramsDescriptor[(isStatic ? i : i - 1)], origin);
			}

			//next slot
			++slot;
			if (!Type.isCat_1(args[i].getType())) {
				++slot;
			}
		}

		return args;
	}

	/**
	 * Creates a new frame for a method invocation and pushes it 
	 * on a state's stack. The actual parameters of the invocation are 
	 * initialized with symbolic values.
	 *  
	 * @param methodSignatureImpl 
     *        the {@link Signature} of the method for which the 
     *        frame is built. The bytecode for the method will be
     *        looked for in 
     *        {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}.
     * @return a {@link ReferenceSymbolic}, the "this" (target) of the method invocation
     *         if the invocation is not static, otherwise {@code null}.
     * @throws BadClassFileException when the classfile with name 
     *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
     *         does not exist in the classpath or is ill-formed.
     * @throws MethodNotFoundException when the classfile with name.
     *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
     *         does not contain the method.
     * @throws MethodCodeNotFoundException when the classfile with name.
     *         {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}
     *         does not contain bytecode for the method (i.e., the method is abstract
     *         or native).
	 */
	public ReferenceSymbolic pushFrameSymbolic(Signature methodSignatureImpl) 
	throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
	    final ClassFile classMethodImpl = this.classHierarchy.getClassFile(methodSignatureImpl.getClassName());
        final boolean isStatic = classMethodImpl.isMethodStatic(methodSignatureImpl);
	    final Frame f = new Frame(methodSignatureImpl, classMethodImpl);
	    final Value[] args = makeArgsSymbolic(f, methodSignatureImpl, isStatic);
	    try {
            f.setArgs(args);
        } catch (InvalidSlotException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
	    this.stack.push(f);
	    return (isStatic ? null : ((ReferenceSymbolic) args[0]));
	}
	
	/**
	 * Parses the signature of a method, and returns the
	 * "this" parameter as found on the operand stack. 
	 * 
	 * @param methodSignature
	 *        the {@link Signature} of a method. It is <em>not</em>
	 *        checked.
	 * @return the {@link Reference} to the receiver of
	 *         the method according to {@link methodSignature}'s 
	 *         declared list of parameters, or {@link null} if the 
	 *         operand stack has not enough items, or the
	 *         item in the position of the "this" parameter is
	 *         not a reference. 
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public Reference peekReceiverArg(Signature methodSignature) throws ThreadStackEmptyException {
	    final String[] paramsDescriptors = Type.splitParametersDescriptors(methodSignature.getDescriptor());
	    final int nParams = paramsDescriptors.length + 1;
	    final Collection<Value> opStackVals = getCurrentFrame().values();
	    int i = 1;
	    for (Value val : opStackVals) { 
	        if (i == nParams) {
	            if (! (val instanceof Reference)) {
	                return null;
	            }
	            return (Reference) val;
	        }
	        ++i;
	    }
	    return null;
	}

	/**
	 * Sets the return program counter of the current frame.
	 * 
	 * @param returnPCOffset the offset of the return program counter 
	 *        w.r.t. the current program counter.
	 * @throws InvalidProgramCounterException iff current + offset program counter
	 *        yield an invalid offset.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void setReturnProgramCounter(int returnPCOffset) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.stack.currentFrame().setReturnProgramCounter(returnPCOffset);
	}

	/**
	 * Removes the current frame from the thread stack.
	 * 
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void popCurrentFrame() throws ThreadStackEmptyException {
		this.stack.pop();
	}

	/**
	 * Removes all the frames from the thread stack.
	 */
	public void clearStack() {
		this.stack.clear();
	}

	/**
	 * Returns the root frame.
	 * 
	 * @return a {@link Frame}, the root (first  
	 *         pushed) one.
	 * @throws ThreadStackEmptyException if the 
     *         thread stack is empty.
	 */
	public Frame getRootFrame() throws ThreadStackEmptyException {
		final List<Frame> frames = this.stack.frames();
		try {
		    return frames.get(0);
		} catch (IndexOutOfBoundsException e) {
		    throw new ThreadStackEmptyException();
		}
	}

	/**
	 * Returns the current frame.
	 * 
	 * @return a {@link Frame}, the current (last 
	 * pushed) one.
	 * @throws ThreadStackEmptyException if the 
	 *         thread stack is empty.
	 */
	public Frame getCurrentFrame() throws ThreadStackEmptyException {
		return this.stack.currentFrame();
	}

	/**
	 * Returns an immutable view of the thread stack.
	 * 
	 * @return an {@link Iterable}{@code <}{@link Frame}{@code >} 
     *         of the method activation frames in the thread stack, 
     *         in their push order.
	 */
	public Iterable<Frame> getStack() {
		return this.stack.frames();
	}
	
	/**
	 * Returns the size of the thread stack.
	 * 
	 * @return an {@code int}, the size.
	 */
	public int getStackSize() {
		return this.stack.frames().size();
	}

	/**
	 * Returns an immutable view of the state's heap.
	 * 
	 * @return the state's heap as an 
	 * immutable {@link Map}{@code <}{@link Integer}{@code , }{@link Objekt}{@code >}.
	 */
	//TODO raise the abstraction level and make this method return a Map<Reference, Objekt>
	public Map<Long, Objekt> getHeap() {
		return this.heap.getObjects();
	}
	
	public Iterable<Objekt> objectsSymbolic() {
		return new Iterable<Objekt>() {
			@Override
			public Iterator<Objekt> iterator() {
				return new Iterator<Objekt>() {
					private final Iterator<Clause> it = pathCondition.getClauses().iterator();
					private boolean hasNext;
					private ClauseAssumeExpands next;
					{
						moveForward();
					}
					
					private void moveForward() {
						while (it.hasNext()) {
							final Clause next = it.next();
							if (next instanceof ClauseAssumeExpands) {
								this.hasNext = true;
								this.next = (ClauseAssumeExpands) next;
								return;
							}
						}
						this.hasNext = false;
						this.next = null;
					}
					
					@Override
					public boolean hasNext() {
						return this.hasNext;
					}

					@Override
					public Objekt next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						final Objekt retVal = getHeap().get(next.getHeapPosition());
						moveForward();
						return retVal;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Returns the static method area of this state.
	 * 
	 * @return the state's static method area as an 
	 * immutable {@link Map}{@code <}{@link String}{@code , }{@link Klass}{@code >}.
	 */
	public Map<String, Klass> getStaticMethodArea() {
		return Collections.unmodifiableMap(this.staticMethodArea.getObjects());
	}

	/**
	 * Returns the instruction in the current method pointed by 
	 * the state's current program counter.
	 * 
	 * @return a {@code byte} representing the 
	 *         bytecode pointed by the state's current program
	 *         counter.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public byte getInstruction() throws ThreadStackEmptyException {
		return(this.stack.currentFrame().getInstruction());
	}

	/**
	 * Returns the instruction in the current method pointed by 
	 * the state's current program counter plus a displacement.
	 * 
	 * @param displacement a {@code int} representing a displacement
	 *        from the current program counter.
	 * @return a {@code byte} representing the 
	 *         bytecode pointed by the state's current program
	 *         counter plus {@code displacement}.
	 * @throws InvalidProgramCounterException iff the frame's program
	 *         counter plus {@code displacement} does not point to 
	 *         a bytecode.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public byte getInstruction(int displacement) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		return(this.stack.currentFrame().getInstruction(displacement));
	}

	/**
	 * Returns the source code row corresponding to the 
	 * frame's program counter.
	 *  
	 * @return the source code row corresponding to the 
	 *         state's program counter, or {@code -1} 
	 *         iff no debug information is available. 
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public int getSourceRow() throws ThreadStackEmptyException {
		return this.stack.currentFrame().getSourceRow();
	}

    /**
     * Returns the current program counter.
     * 
     * @return an {@code int} representing the state's 
     *         current program counter.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public int getPC() throws ThreadStackEmptyException {
        return this.stack.currentFrame().getProgramCounter();
    }
    
    /**
     * Returns the return program counter of the caller frame
     * stored for a return bytecode.
     * 
     * @return an {@code int}, the return program counter.
     * @throws ThreadStackEmptyException  if the thread stack is empty.
     */
    public int getReturnPC() throws ThreadStackEmptyException {
        return this.stack.currentFrame().getReturnProgramCounter();
    }

	/**
	 * Increments/decrements the program counter by an arbitrary number.
	 * 
	 * @param n the {@code int} value to be added to the current 
	 *          program counter.
	 * @throws InvalidProgramCounterException if the incremented program counter
	 *         would not point to a valid bytecode in the current method 
	 *         (the state's program counter is not changed).
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void incProgramCounter(int n) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.setProgramCounter(getPC() + n);
	}

	/**
	 * Sets the state's program counter.
	 * 
	 * @param newPC the new program counter value.
	 * @throws InvalidProgramCounterException if {@code newPC} does not 
	 *         point to a valid bytecode in the current method (the
	 *         state's program counter is not changed).
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void setProgramCounter(int newPC) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.stack.currentFrame().setProgramCounter(newPC);
	}

	/**
	 * Assumes a predicate over primitive values (numeric assumption).
	 * Its effect is adding a clause to the path condition.
	 * 
	 * @param p the primitive clause which must be added to the state's 
	 *          path condition. It must be {@code p != null && 
	 *          ( p instanceof }{@link Expression} {@code || p instanceof }{@link Simplex}
	 *          {@code ) && p.}{@link Value#getType() getType()} {@code  == }{@link Type#BOOLEAN BOOLEAN}.
	 * @throws NullPointerException if {@code p} violates preconditions.
	 */
	public void assume(Primitive p) {
		if (p == null || p.getType() != Type.BOOLEAN || 
			(! (p instanceof Simplex) && ! (p instanceof Expression))) { 
			throw new NullPointerException(); //TODO throw a better exception
		}
		this.pathCondition.addClauseAssume(p);
		++this.nPushedClauses;
	}

	/**
	 * Assumes the expansion of a symbolic reference to a fresh object of some
	 * class. Its effects are adding the fresh object to the heap and refining
	 * the path condition.
	 * 
	 * @param r the {@link ReferenceSymbolic} which is resolved. It 
	 *          must be {@code r != null}.
	 * @param className a {@code String}, the name of the class of the fresh 
	 *        object to which {@code r} must be expanded. It must be {@code className != null}.
	 * @throws NullPointerException if either {@code r} or {@code className} is
	 *         {@code null}.
	 * @throws InvalidTypeException if {@code className} is not the name of a
	 *         valid type (class or array). 
	 * @throws ContradictionException if {@code r} is already resolved.
	 */
	public void assumeExpands(ReferenceSymbolic r, String className) 
	throws InvalidTypeException, ContradictionException {
    	if (r == null || className == null) {
    		throw new NullPointerException(); //TODO find a better exception
    	}
    	if (resolved(r)) {
    		throw new ContradictionException();
    	}
		final long pos = this.createObjectSymbolic(className, r.getOrigin());
		final Objekt o = this.heap.getObject(pos);
		this.pathCondition.addClauseAssumeExpands(r, pos, o);
		++this.nPushedClauses;
	}

	/**
	 * Assumes the resolution of a symbolic reference to some alias.  
	 * Its effects are refining all the symbolic references with 
	 * same origin, and adding a clause to the path condition.
	 * 
	 * @param r the {@link ReferenceSymbolic} which is resolved. It 
	 *        must be {@code r != null} and {@code r} must not be
	 *        already resolved.
	 * @param heapPosition the heap position of the {@link Objekt} to which 
	 *        {@code r} is resolved. It must correspond to a heap position
	 *        where an object is effectively present.
	 * @param o the {@link Objekt} to which {@code r} is resolved. It will not
	 *        be modified nor stored.
	 * @throws ContradictionException if {@code r} is already resolved.
	 * @throws NullPointerException if either {@code r} or {@code heapPosition} 
	 *         violates preconditions.
	 */
	public void assumeAliases(ReferenceSymbolic r, long heapPosition, Objekt o) 
	throws ContradictionException {
    	if (r == null || o == null) {
    		throw new NullPointerException(); //TODO find a better exception
    	}
    	if (this.resolved(r)) {
    		throw new ContradictionException();
    	}
		this.pathCondition.addClauseAssumeAliases(r, heapPosition, o.clone());
		++this.nPushedClauses;
	}

	/**
	 * Assumes the resolution of a symbolic reference to null.  
	 * Its effects are refining all the symbolic references with 
	 * same origin, and adding a clause to the path condition.
	 * 
	 * @param r the {@link ReferenceSymbolic} which is resolved. It 
	 *          must be {@code r != null} and {@code r} must not be
	 *          already resolved.
	 * @throws ContradictionException if {@code r} is already resolved.
	 * @throws NullPointerException if {@code r} violates preconditions.
	 */
	public void assumeNull(ReferenceSymbolic r) throws ContradictionException {
    	if (r == null) {
    		throw new NullPointerException(); //TODO find a better exception
    	}
    	if (this.resolved(r)) {
    		throw new ContradictionException();
    	}
		this.pathCondition.addClauseAssumeNull(r);
		++this.nPushedClauses;
	}

	/**
	 * Assumes that a class is initialized before the 
	 * start of symbolic execution. Additionally, it
	 * creates a symbolic {@link Klass} and adds it 
	 * to the static store.
	 * 
	 * @param className the corresponding concrete class 
	 *        name as a {@link String}. It must be 
	 *        {@link className != null}.
	 * @throws NullPointerException if {@code className} 
	 *         is {@code null}.
     * @throws BadClassFileException if the classfile with name 
     *         {@code className} does not exist in the classpath
     *         or is ill-formed.
	 * @throws InvalidIndexException if the access to the class 
     *         constant pool fails.
	 */
	public void assumeClassInitialized(String className) 
	throws BadClassFileException, InvalidIndexException {
		if (className == null) {
			throw new NullPointerException();
		}
		ensureKlassSymbolic(className);
		final Klass k = this.getKlass(className);
		this.pathCondition.addClauseAssumeClassInitialized(className, k);
		++this.nPushedClauses;
	}

	/**
	 * Assumes that a class is not initialized before the 
	 * start of symbolic execution. Additionally, it
     * creates a concrete {@link Klass} and adds it to the 
     * static store.
	 * 
	 * @param className the corresponding concrete class 
	 *        name as a {@link String}. It must be 
	 *        {@link className != null}.
     * @throws NullPointerException if {@code className} 
     *         is {@code null}.
     * @throws BadClassFileException if the classfile with name 
     *         {@code className} does not exist in the classpath
     *         or is ill-formed.
     * @throws InvalidIndexException if the access to the class 
     *         constant pool fails.
	 */
	public void assumeClassNotInitialized(String className) 
	throws BadClassFileException, InvalidIndexException {
		if (className == null) {
			throw new NullPointerException();
		}
		ensureKlass(className);
		this.pathCondition.addClauseAssumeClassNotInitialized(className);
		++this.nPushedClauses;
	}

	/**
	 * Returns the state's path condition clauses.
	 * 
     * @return a read-only {@link Collection}{@code <}{@link Clause}{@code >} 
     * representing all the {@link Clause}s cumulated in {@code this}. 
     * It is valid until {@code this} is modified.
	 */
	public Collection<Clause> getPathCondition() {
		return this.pathCondition.getClauses();
	}

	/**
	 * Returns the path condition clauses that have been pushed since
	 * the last call of {@link #resetLastPathConditionClauses()}. Used to determine
	 * how many clauses have not yet been sent to the decision procedure.
	 * 
     * @return a read-only {@link Iterable}{@code <}{@link Clause}{@code >} 
     * representing all the {@link Clause}s cumulated in {@code this}. 
     * It is valid until {@code this} is modified, or {@link #resetLastPathConditionClauses()}
     * is invoked.
	 */
	public Iterable<Clause> getLastPathConditionPushedClauses() {
		final ListIterator<Clause> it = this.pathCondition.getClauses().listIterator();
		final int fwdEnd = this.pathCondition.getClauses().size() - this.nPushedClauses;
		for (int i = 1; i <= fwdEnd; ++i) {
			it.next();
		}
		return () -> it;
	}

	/**
	 * Resets to zero the number of clause pushed to the 
	 * state's path condition that is returned by a call
	 * to {@link #getLastPathConditionPushedClauses()}.
	 * This method is invoked whenever the decision
	 * procedure's current assumptions are synchronized with 
	 * the state's path condition. 
	 */
	public void resetLastPathConditionClauses() {
		this.nPushedClauses = 0;
	}

	/**
	 * Sets the {@link State} stuck because of a return
	 * from the topmost method,
	 * in the case no value must be returned.
	 */
	public void setStuckReturn() {
		this.stuck = true;
		this.exc = null;
		this.val = null;
	}

	/**
	 * Sets the {@link State} stuck because of a return
	 * from the topmost method.
	 * 
	 * @param val the return {@link Value}.
	 */
	public void setStuckReturn(Value val) {
		this.setStuckReturn();
		this.val = val;
	}

	/**
	 * Returns the stuck state's return value.
	 * 
	 * @return the {@link Value} set by a 
	 *         previous call to {@link #setStuckReturn(Value)}, or {@code null} 
	 *         if <code>!this.</code>{@link #isStuck()}
	 *         or it has not been set stuck with a call to 
	 *         {@link #setStuckReturn(Value)}. 
	 */
	public Value getStuckReturn() {
		return this.val;
	}

	/**
	 * Sets a stuck state caused by an external stop.
	 */
	public void setStuckStop() {
		this.setStuckReturn();
	}

	/**
	 * Sets a stuck state caused by an unhandled throw.
	 * 
	 * @param exc a {@link Reference} to some instance 
	 *            in this {@link State}'s heap. 
	 */
	public void setStuckException(Reference exc) {
		this.setStuckReturn();
		this.exc = exc;
	}

	/**
	 * Returns the stuck state's thrown exception.
	 * 
	 * @return the {@link Reference} set by a 
	 *         previous call to {@link #setStuckException(Reference)} 
	 *         or {@code null} if <code>!this.</code>{@link #isStuck()}
	 *         or it has not been set stuck with a call to 
	 *         {@link #setStuckException(Reference)} 
	 */
	public Reference getStuckException() {
		return this.exc;
	}

	/**
	 * Tests for stuck state.
	 * 
	 * @return {@code true} iff the state is stuck, i.e., iff 
	 *         it is a leaf state in the symbolic execution tree. A stuck state is
	 *         the outcome of a halting instruction, or of a return from the topmost
	 *         stack frame, or of an unhandled throw.
	 */
	public boolean isStuck() {
		return this.stuck;
	}

	/**
	 * Adds a suffix to the state's identifier.
	 * 
	 * @param identifierSuffix 
	 *        a {@link String} representing the suffix to be 
	 *        appended to the state's identifier.
	 * @param increase 
	 */
	public void appendToIdentifier(String identifierSuffix) {
		this.identifier += identifierSuffix;
	}


	/**
	 * Sets the state's depth to {@code 1}.
	 */
	public void resetDepth() {
		this.depth = 1;
	}

	/**
	 * Increments the state's depth by {@code 1}.
	 */
	public void incDepth() {
		++this.depth;
	}

	/**
	 * Gets the state's depth in the symbolic execution tree; the depth 
	 * is the number of branches above the state.
	 * 
	 * @return the depth of the state as an {@code int} value
	 *         ({@code 0} for the topmost state).
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * Sets the state's count to {@code 1}.
	 */
	public void resetCount() {
		this.count = 1;
	}

	/**
	 * Increments the state's count by {@code 1}.
	 */
	public void incCount() {
		++this.count;
	}

	/**
	 * Gets the state's count.
	 * 
	 * @return a nonnegative {@code int} representing the 
	 *         state's count.  
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * Gets the state's identifier.
	 * 
	 * @return a {@link String} representing the 
	 *         state's identifier.
	 */
	public String getIdentifier() {
		return this.identifier;
	}
    
    /**
     * Sets whether the current state was produced by 
     * a branching decision.
     * 
     * @param branchingDecision {@code true} iff the current
     * state was produced by a branching decision.
     */
    public void setBranchingDecision(boolean branchingDecision) {
    	this.branchingDecision = branchingDecision;
    }
    
    /**
     * Checks whether the state has been produced by 
     * a branching decision.
     * 
     * @return {@code true} if {@link #setBranchingDecision}
     *         was invoked with a {@code true} parameter 
     *         since the previous invocation 
     *         of {@code branchingDecision}, {@code false} 
     *         otherwise.
     */
    public boolean branchingDecision() {
        final boolean retval = this.branchingDecision;
        
        this.branchingDecision = false;
        return retval;
    }

	/**
	 * Sets the state's sequence number to {@code 0}.
	 */
	public void resetSequenceNumber() {
		this.sequenceNumber = 0;
	}

	/**
	 * Increments the state's sequence number by {@code 1}.
	 */
	public void incSequenceNumber() {
		++this.sequenceNumber;
	}

	/**
	 * Gets the state's sequence number.
	 * 
	 * @return a nonnegative {@code int} representing the 
	 *         state's sequence number.  
	 */
	public int getSequenceNumber() {
		return this.sequenceNumber;
	}
		
    /**
     * Returns the number of assumed object of a given class.
     * 
     * @param className a {@link String}.
     * @return the number of objects with class {@code className}
     * assumed by this state, as resulting by the state's path 
     * condition.
     */
	public int getNumAssumed(String className) {
		return this.pathCondition.getNumAssumed(className);
	}

	/**
	 * Refines this state based on the path condition of another state that
	 * refines (i.e., comes temporally later than) this state.
	 *
	 * @param stateRefining another {@link State}; it must refine this state, 
	 *        meaning that this state's identifier and path condition must be prefixes 
	 *        of {@code stateRefining}'s identifier and path condition.
	 * @throws CannotRefineException when {@code stateRefining} does not refine 
	 *         {@code this}.
	 */
	public void refine(State stateRefining) throws CannotRefineException {
		final String refiningIdentifier = stateRefining.identifier;
		final PathCondition refiningPathCondition = stateRefining.pathCondition;
		
		//checks that stateRefining refines this state, and 
		//gets an iterator to the additional clauses
		final Iterator<Clause> iRefining;
		if (refiningIdentifier.startsWith(this.identifier)) {
			iRefining = refiningPathCondition.refines(this.pathCondition);
			if (iRefining == null) {
				throw new CannotRefineException();
			}
		} else {
			throw new CannotRefineException();
		}
		
		//expands the heap and the static method area
		while (iRefining.hasNext()) {
			final Clause c = iRefining.next();
			if (c instanceof ClauseAssumeExpands) {
				final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
				final long oPos = cExp.getHeapPosition();
				final Objekt o = cExp.getObjekt(); //note that the getter produces a safety copy
				this.heap.set(oPos, o);
			} else if (c instanceof ClauseAssumeClassInitialized) {
				final ClauseAssumeClassInitialized cCl = (ClauseAssumeClassInitialized) c;
				final String kName = cCl.getClassName();
				final Klass k = cCl.getKlass(); //note that the getter produces a safety copy
				this.staticMethodArea.set(kName, k);
			} //else do nothing
		}
		
		//updates the symbol factory
		this.symbolFactory = stateRefining.symbolFactory.clone();

		//finally, updates the path condition
		this.pathCondition = refiningPathCondition.clone();
	}

	/**
	 * A Factory Method for creating both reference and primitive 
	 * symbolic values.
	 * 
     * @param staticType a {@link String}, the static type of the
     *        variable from which this reference originates (as 
     *        from {@code origin}).
     * @param origin a {@link String}, the origin of this reference.
	 * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
	 *         according to {@code descriptor}.
	 */
	public Value createSymbol(String staticType, String origin) {
		return this.symbolFactory.createSymbol(staticType, origin);
	}
	
	/**
	 * Checks whether the next bytecode must be WIDE 
	 * and resets the WIDE test.
	 * 
	 * @return {@code true} iff invoked for the first 
	 *         time after a {@link #setWide()}
	 *         call. 
	 */
	public boolean nextWide() {
		final boolean wide = this.wide;
		this.wide = false;
		return wide;
	}
	
	/**
	 * Remembers that the next bytecode must be WIDE.
	 */
	public void setWide() {
		this.wide = true;
	}

	@Override
	public String toString() {
		String tmp = "[ID:\"" + this.identifier + "[" + this.sequenceNumber + "]\", ";
		if (this.isStuck()) {
			tmp += "Stuck, ";
			if (this.exc != null) 
				tmp += "Raised:" + this.exc.toString() + ", ";
			else if (this.val != null)
				tmp += "Return:" + this.val.toString() + ", ";
		} else {
			try {
				tmp += "CurrentMethod:" + this.stack.currentFrame().getCurrentMethodSignature() + ", ";
				tmp += "ProgramCounter:" + this.getPC() + ", ";
			} catch (ThreadStackEmptyException e) {
				//does nothing
			}
			tmp += "Stack:" + this.stack.toString() + ", ";
		}
		tmp += "PathCondition:'" + this.pathCondition.toString() + "', ";
		tmp += "StaticMethodArea:" + this.staticMethodArea.toString() + ", ";
		tmp += "Heap:" + this.heap.toString() + "]";
		return(tmp);
	}

	@Override
	public State clone() {
		final State o;
		try {
			o = (State) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}

		//stringLiterals
		o.stringLiterals = new HashMap<>(o.stringLiterals);

		//stack
		o.stack = o.stack.clone();

		//heap
		o.heap = o.heap.clone();
		
		//staticStore
		o.staticMethodArea = o.staticMethodArea.clone();

		//pathCondition
		o.pathCondition = o.pathCondition.clone();

		//exc and val are values, so they are immutable

		//symbolFactory
		o.symbolFactory = o.symbolFactory.clone();

		return o;
	}
}
