package jbse.mem;

import static jbse.Util.ROOT_FRAME_MONIKER;
import static jbse.mem.Util.JAVA_STRING;
import static jbse.mem.Util.JAVA_STRING_HASH;
import static jbse.mem.Util.JAVA_STRING_OFFSET;
import static jbse.mem.Util.JAVA_STRING_COUNT;
import static jbse.mem.Util.JAVA_STRING_VALUE;

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

import jbse.Type;
import jbse.Util;
import jbse.bc.ClassFile;
import jbse.bc.ClassFileFactory;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.ExceptionTable;
import jbse.bc.ExceptionTableEntry;
import jbse.bc.LineNumberTable;
import jbse.bc.LocalVariableTable;
import jbse.bc.Signature;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.AttributeNotFoundException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.FieldNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.bc.MethodCodeNotFoundException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.bc.NoMethodReceiverException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.CannotRefineException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.FastArrayAccessNotAllowedException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Objekt.Epoch;
import jbse.rewr.CalculatorRewriting;

/**
 * Class that represents the state of execution.
 */
public class State implements Cloneable {
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
	private HashMap<String, ReferenceConcrete> stringLiterals = new HashMap<String, ReferenceConcrete>();

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

	/** The generator for unambiguous symbol identifiers. */
	private SymbolFactory symbolFactory = new SymbolFactory();
	
	/** May symbolic execution from this state violate an assumption? */
	private boolean mayViolateAssumption = true;

	/** {@code true} iff the next bytecode must be executed in its WIDE variant. */
	private boolean wide = false;
	
	/** 
	 * The object that fetches classfiles from the classpath, stores them, 
	 * and allows visiting the whole class/interface hierarchy. 
	 */
	private final ClassHierarchy classHierarchy;
	
	/** The {@link CalculatorRewriting}. */
	private final CalculatorRewriting calc;

	/**
	 * Constructor of an empty State.
	 * 
	 * @param cp a {@link Classpath}.
	 * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
	 *        The class must have an accessible constructor with two parameters, the first a 
	 *        {@link ClassFileInterface}, the second a {@link Classpath}.
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
	public State(Classpath cp, Class<? extends ClassFileFactory> fClass, Map<String, Set<String>> expansionBackdoor, CalculatorRewriting calc) 
	throws InvalidClassFileFactoryClassException {
		this.classHierarchy = new ClassHierarchy(cp, fClass, expansionBackdoor);
		this.calc = calc;
	}

	
	/**
	 * Getter for this state's classpath.
	 * 
	 * @return a {@link Classpath}.
	 */
	public Classpath getClasspath() {
		return this.classHierarchy.getClassPath();
	}
	
	/**
	 * Getter for this state's calculator.
	 * 
	 * @return a {@link CalculatorRewriting}.
	 */
	public CalculatorRewriting getCalculator() {
		return this.calc;
	}

	/**
	 * Returns and deletes the value from the top of the current 
	 * operand stack.
	 * 
	 * @return the {@link Value} on the top of the current 
	 * operand stack.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws OperandStackEmptyException if the current operand 
	 * stack is empty
	 */
	public Value pop() throws ThreadStackEmptyException, OperandStackEmptyException {
		return this.stack.currentFrame().pop();
	}

	/**
	 * Returns the topmost element in the current operand stack, 
	 * without removing it.
	 * 
	 * @return a {@link Value}.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws OperandStackEmptyException if the current operand
	 *         stack is empty. 
	 */
	public Value top() throws ThreadStackEmptyException, OperandStackEmptyException {
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
	public void push(Value val) throws ThreadStackEmptyException {
		this.stack.currentFrame().push(val);		
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
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
		return this.stack.currentFrame().getCurrentMethodSignature();
	}
	
	/**
	 * Returns the {@link Signature} of the  
	 * root method.
	 * 
	 * @return a {@link Signature}.
	 * @throws ThreadStackEmptyException  if the stack is empty.
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
		final Signature s = this.getRootMethodSignature();
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
		} catch (MethodNotFoundException | ClassFileNotFoundException e) {
			throw new UnexpectedInternalException(e);
		}
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
		this.stack.currentFrame().setLocalVariableValue(slot, this.stack.currentFrame().getPC(), val);
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
	 * {@code ref has been resolved.
	 * @throws NullPointerException if 
	 * {@link #resolved}{@code (ref) == false}.
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
     */
    public boolean isNull(Reference ref) {
    	if (ref instanceof ReferenceSymbolic) {
    		final ReferenceSymbolic refS = (ReferenceSymbolic) ref;
    		return (this.resolved(refS) && this.getResolution(refS) == jbse.mem.Util.POS_NULL);
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
     *         anything (e.g., is {@link Null}, or is an unresolved 
     *         symbolic reference, or is resolved to null).
     */
    public Objekt getObject(Reference ref) {
		final Objekt retVal;
    	if (ref.isSymbolic()) {
    		final ReferenceSymbolic refSymbolic = (ReferenceSymbolic) ref;
    		if (this.resolved(refSymbolic)) {
    			final long pos = this.getResolution(refSymbolic);
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
    		if (this.resolved(refSymbolic)) {
    			pos = this.getResolution(refSymbolic);
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
	 * 
	 * @param className the name of the class of the new object.
	 * @return a {@link ReferenceConcrete} to the newly created object.
	 */
	public ReferenceConcrete createInstance(String className) {    	
		final Signature[] mySgnArray = this.classHierarchy.getAllFieldsInstance(className);
		final Instance myObj = new Instance(this.calc, mySgnArray, className, null, Epoch.EPOCH_AFTER_START);
		return new ReferenceConcrete(this.heap.addNew(myObj));
	}
	
	/**
	 * Creates a concrete {@link Klass} object and loads it in the 
	 * static area of this state.
	 * 
	 * @param className the name of the class to be loaded. The method 
	 *        creates and loads a {@link Klass} object only for {@code className}, 
	 *        not for its superclasses in the hierarchy.
     * @throws ClassFileNotFoundException when the class file cannot be 
     *         found in the classpath.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void createKlass(String className) 
	throws ClassFileNotFoundException, InvalidIndexException {
		final ClassFile classFile = this.getClassHierarchy().getClassFile(className);
		final Signature[] sgnFields = classFile.getFieldsStatic();
		final Klass k = new Klass(State.this.calc, sgnFields, null, Objekt.Epoch.EPOCH_AFTER_START);
		this.staticMethodArea.set(className, k);
		initConstantFields(className, k);
	}

	/**
	 * Creates a symbolic {@link Klass} object and loads it in the 
	 * static area of this state.
	 * 
	 * @param className the name of the class to be loaded.
     * @throws ClassFileNotFoundException if {@code className} does 
     *         not correspond to a valid class in the classpath.
	 * @throws InvalidIndexException if the access to the class 
	 *         constant pool fails.
	 */
	private void createKlassSymbolic(String className) 
	throws ClassFileNotFoundException, InvalidIndexException {
		final ClassFile classFile = this.getClassHierarchy().getClassFile(className);
		final Signature[] sgnFields = classFile.getFieldsStatic();
		final Klass k = new Klass(this.calc, sgnFields, "[" + className + "]", Objekt.Epoch.EPOCH_BEFORE_START);
		initWithSymbolicValues(k);
		initConstantFields(className, k);
		this.staticMethodArea.set(className, k);
	}
	
	/**
	 * Initializes the constant fields of a {@link Klass} with the 
	 * constant values specified in the classfile, if present.
	 * Currently, these constant fields are only used to keep the 
	 * {@link Klass} coherent with its definition (and prettyprint 
	 * it correctly), because all bytecodes fetch constant field values
	 * directly since {@link Klass}es can be not initialized.
	 * 
	 * @param className the class to be initialized.
	 * @param k the corresponding {@link Klass} object.
	 * @throws ClassFileNotFoundException if {@code className} is not the name
	 *         of a valid class in the classpath.
	 * @throws InvalidIndexException if the access to the class constant pool fails.
	 */
	private void initConstantFields(String className, Klass k) 
	throws ClassFileNotFoundException, InvalidIndexException {
		final ClassFile cf = this.getClassHierarchy().getClassFile(className);
		final Signature[] flds = cf.getFieldsStatic();
		for (final Signature sig : flds) {
			try {
				if (cf.isFieldConstant(sig)) {
					//sig is directly extracted from the classfile, 
					//so no resolution is necessary
					Value v = this.calc.val_(cf.fieldConstantValue(sig));
					if (v instanceof ConstantPoolString) {
						v = this.referenceToStringLiteral(v.toString());
					}
					k.setFieldValue(sig, v);
				}
			} catch (FieldNotFoundException | AttributeNotFoundException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
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
		final Instance obj = new Instance(this.calc, fieldsSignatures, className, origin, Epoch.EPOCH_BEFORE_START);
		initWithSymbolicValues(obj);
		return obj;
	}

	/**
	 * Initializes an {@link Instance} with symbolic values.
	 * 
	 * @param myObj an {@link Instance} which will be initialized with 
	 *              symbolic values.
	 */
	private void initWithSymbolicValues(Instance myObj) {
		final Signature[] mySgnArray = myObj.getFieldSignatures();
		for (final Signature myActualSignature : mySgnArray) {
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
	 * Returns an {@link Instance} of class {@code java.lang.String} 
	 * from a {@link State}'s {@link Heap} corresponding to a string literal. 
	 * In the case such {@link Instance} does not exist, it creates it and
	 * puts it in the {@link State}'s {@link Heap}.
	 * 
	 * @param value a {@link String} representing a string literal.
	 * @return a {@link ReferenceConcrete} to the {@link Instance} in 
	 *         {@code state}'s {@link Heap} corresponding to 
	 *         {@code value}. 
	 */
	public ReferenceConcrete referenceToStringLiteral(String value) {
		ReferenceConcrete retVal = this.getStringLiteral(value);
		if (retVal == null) {
			retVal = this.createStringLiteral(value);
			this.setStringLiteral(value, retVal);
		}

		return retVal;
	}

	private ReferenceConcrete createStringLiteral(String value) {
		final ReferenceConcrete valueAsArray = createArrayOfChars(value);
		final Simplex hash = this.calc.valInt(value.hashCode());
		final Simplex zero = this.calc.valInt(0);
		final Simplex length = this.calc.valInt(value.length());
		
		final ReferenceConcrete retVal = createInstance(JAVA_STRING);
		final Instance i = (Instance) this.getObject(retVal);
		i.setFieldValue(JAVA_STRING_VALUE, valueAsArray);
		i.setFieldValue(JAVA_STRING_HASH, hash);
		i.setFieldValue(JAVA_STRING_OFFSET, zero);
		i.setFieldValue(JAVA_STRING_COUNT, length);
		
		return retVal;
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
		final Array a = (Array) this.getObject(retVal);
		for (int k = 0; k < value.length(); k++) {
			char c = value.charAt(k);
			try {
				a.setFast(this.calc.valInt(k), this.calc.valChar(c));
			} catch (InvalidOperandException | InvalidTypeException
					| FastArrayAccessNotAllowedException e) {
				//this should never happen 
				throw new UnexpectedInternalException(e);
			}
		}

		return retVal;
	}
	
	/**
	 * Returns a reference to an {@link Instance} corresponding to a 
	 * string literal.
	 * 
	 * @param value a {@link String}.
	 * @return a {@link ReferenceConcrete} to an {@link Instance} 
	 *         with class {@code java.lang.String} which equals 
	 *         {@code value}, if such instance exists in the 
	 *         state's heap, {@code null} otherwise.
	 */
	public ReferenceConcrete getStringLiteral(String value) {
		return this.stringLiterals.get(value);
	}

	/**
	 * Creates an {@link Instance} in the state's heap for a 
	 * string literal.
	 * 
	 * @param value a {@link String}.
	 * @param ref a {@link ReferenceConcrete} to the created 
	 *            {@link Instance}.
	 */
	private void setStringLiteral(String value, ReferenceConcrete ref) {
		this.stringLiterals.put(value, ref);
	}

	/**
	 * Creates a new {@link Instance} of a given throwable class in the 
	 * heap of this state. The created instance's fields are initialized 
	 * with the default values for each field's type. Then, unwinds the 
	 * stack of this state a handler for the created instance is found.
	 * 
	 * @param state the {@link State} whose {@link Heap} will receive 
	 *              the new object.
	 * @param classNameThrowable the name of the class of the new instance.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void createThrowableAndThrowIt(String classNameThrowable) 
	throws ThreadStackEmptyException {
		//TODO check that classNameException is Throwable??
		final ReferenceConcrete myExceRef = createInstance(classNameThrowable);
		push(myExceRef);
		throwIt(myExceRef);
	}

	/**
	 * Unwinds the stack of this state until a handler for a suitable 
	 * throwable {@link Objekt} is found.
	 * 
	 * @param exceptionToThrow a {@link Reference} to a throwable 
	 *        {@link Objekt} in the state's {@link Heap}.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void throwIt(Reference exceptionToThrow) 
	throws ThreadStackEmptyException {
		//TODO check that exceptionToThrow is resolved/concrete
		final Objekt myException = this.getObject(exceptionToThrow);
		//TODO check that Objekt is Throwable

		//fills a vector with all the superclass names of the exception
		final ArrayList<String> excTypes = new ArrayList<String>();
		for (ClassFile f : this.classHierarchy.superclasses(myException.getType())) {
			excTypes.add(f.getClassName());
		}

		//unwinds the stack
		while (true) {
			final Signature currentMethodSignature = getCurrentMethodSignature();
			final String classToOpen = currentMethodSignature.getClassName();
			final ExceptionTable myExTable;
			try {
				myExTable = this.classHierarchy.getClassFile(classToOpen).getExceptionTable(currentMethodSignature);
			} catch (ClassFileNotFoundException | MethodNotFoundException | MethodCodeNotFoundException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			} catch (InvalidIndexException e) {
				//TODO check whether this recursive call is ok!
				final Reference myExceRef = createInstance(Util.VERIFY_ERROR);
				push(myExceRef);
				throwIt(myExceRef);
				return;
			}
			final ExceptionTableEntry tmpEntry = myExTable.getEntry(excTypes, this.getPC());
			if (tmpEntry == null) {
				this.stack.pop();
				if (this.stack.isEmpty()) {
					this.setStuckException(exceptionToThrow);
					return;
				}
			} else {
				try {
					this.setPC(tmpEntry.getPCHandle());
					this.stack.currentFrame().clear();
					push(exceptionToThrow);
				} catch (InvalidProgramCounterException e) {
					//TODO check whether this recursive call is ok!
					final Reference myExceRef = createInstance(Util.VERIFY_ERROR);
					push(myExceRef);
					throwIt(myExceRef);
				}
				return;				
			}
		}
	}

	/**
	 * Creates a new frame for a method invocation and pushes it 
	 * on this state's stack. The actual parameters of the invocation are 
	 * initialized with values from the invoking frame's operand stack.
	 * 
	 * @param methodSignature
	 *        the {@link Signature} of the method for which the 
	 *        frame is built. If {@code isSpecial == true} it must 
	 *        be the {@link Signature} of the resolved method, 
	 *        otherwise the class indicated in {@code methodSignature}
	 *        is ignored.
	 * @param isRoot
	 *        {@code true} iff the frame is the root frame of symbolic
	 *        execution (i.e., on the top of the thread stack).
	 * @param isStatic
	 *        {@code true} iff INVOKESTATIC method invocation rules 
	 *        must be applied.
	 * @param isSpecial
	 *        {@code true} if INVOKESPECIAL method code lookup rules 
	 *        must be applied, {@code false} if INVOKEVIRTUAL/INVOKEINTERFACE 
	 *        code lookup rules must be applied. 
	 * @param returnPCOffset 
	 *        the offset from the current program counter of 
	 *        the return program counter. It is ignored if 
	 *        {@code isRoot == true}.
	 * @param args
	 *        varargs of method call arguments. It must match 
	 *        {@code methodSignature} and {@code isStatic}.
	 * @throws ClassFileNotFoundException when the class with name 
	 *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}
	 *         does not exist.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when a method with signature 
	 *         {@code methodSignature} exists but its features differ from
	 *         {@code isStatic} and {@code isSpecial}.
	 * @throws ThreadStackEmptyException when {@code isSpecial == true && isRoot == true}.
	 * @throws PleaseDoNativeException when the method is native.
	 * @throws InvalidProgramCounterException when {@code returnPCOffset} is invalid
	 * @throws NoMethodReceiverException when {@code isStatic == false && isSpecial == false}
	 *         and the first argument in {@code args} is a reference to null.
     * @throws InvalidSlotException when there are 
     *         too many {@code arg}s or some of their types are 
     *         incompatible with their respective slots types.
	 */
	public void pushFrame(Signature methodSignature, boolean isRoot, boolean isStatic, boolean isSpecial, int returnPCOffset, Value... args) 
	throws ClassFileNotFoundException, MethodNotFoundException, IncompatibleClassFileException, ThreadStackEmptyException, 
	PleaseDoNativeException, InvalidProgramCounterException, NoMethodReceiverException, InvalidSlotException {
		//checks the "this" parameter (invocation receiver) if necessary
		final Reference thisObject;
		if (isStatic) {
			thisObject = null;
		} else {
			if (args.length == 0 || !(args[0] instanceof Reference)) {
				throw new UnexpectedInternalException("args for method invocation do not correspond to method signature"); //TODO better exception
			}
			thisObject = (Reference) args[0];
			if (isNull(thisObject)) {
				throw new NoMethodReceiverException();
			}
		}

		//creates and initializes the frame, and pushes it on the state's 
		//frame stack
		final Frame f = newFrame(methodSignature, isRoot, isStatic, isSpecial, thisObject);
		f.setArgs(args);

		//sets the frame's return program counter
		if (isRoot) {
			//do nothing, after creation the frame has already a dummy return program counter
		} else {
			this.setReturnProgramCounter(returnPCOffset);
		}

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
			final String origin = ROOT_FRAME_MONIKER + f.getLocalVariableName(slot);
			if (slot == ROOT_THIS_SLOT && !isStatic) {
				args[i] = createSymbol(Type.REFERENCE + rootClassName + Type.TYPEEND, origin);
				//must assume {ROOT}:this epands to nonnull object (were it null the frame would not exist!)
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
	 * @param methodSignatureResolved 
	 *        the {@link Signature} of the method for which the 
	 *        frame is built.
	 * @param isStatic
	 *        {@code true} iff INVOKESTATIC method invocation rules 
	 *        must be applied.
	 * @throws ClassFileNotFoundException when the class with name 
	 *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}
	 *         does not exist.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when a method with signature 
	 *         {@code methodSignature} exists but its features differ from
	 *         {@code isStatic}.
	 * @throws PleaseDoNativeException when the method is declared native.
	 */
	public void pushFrameSymbolic(Signature methodSignatureResolved, boolean isStatic) 
	throws ClassFileNotFoundException, MethodNotFoundException, IncompatibleClassFileException, 
	PleaseDoNativeException {
		//creates and initializes the frame, and pushes on the state's 
		//stack frame
		try {
			final Frame f = newFrame(methodSignatureResolved, true, isStatic, false, null);
			final Value[] args = makeArgsSymbolic(f, methodSignatureResolved, isStatic);
			f.setArgs(args);
			this.stack.push(f);
		} catch (InvalidSlotException | ThreadStackEmptyException e) {
			throw new UnexpectedInternalException(e);
		}
	}

	/**
	 * Parses the signature of a method, and pops from the operand stack 
	 * a number of values consistent with the number of method parameters.
	 * @param methodSignature
	 *        the {@link Signature} of a method.
	 * @param isStatic
	 *        {@code true} iff the method is static.
	 * @return a {@link Value}{@code []} of all the parameters, where the i-th 
	 *         array element is the value for the i-th parameter in the 
	 *         {@code methodSignature}. 
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 * @throws OperandStackEmptyException if there are not enough values
	 *         in the current operand stack. 
	 */
	public Value[] popMethodCallArgs(Signature methodSignature, boolean isStatic) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		String[] arrayDescriptor = Type.splitParametersDescriptors(methodSignature.getDescriptor());
		int count = (isStatic ? arrayDescriptor.length : arrayDescriptor.length + 1);
		Value[] argArray = new Value[count];
		for (int i = 1; i <= count; i++) { 
			argArray[count - i] = this.pop();
		}
		return argArray;
	}

	/**
	 * Creates a new frame for a method invocation.
	 * 
	 * @param methodSignatureResolved
	 *        the {@link Signature} of the resolved method for 
	 *        which the frame must be built.
	 * @param isRoot
	 *        {@code true} iff this is a root frame (i.e., on top 
	 *        of the thread stack).
	 * @param isStatic
	 *        {@code true} iff INVOKESTATIC method invocation rules 
	 *        must be applied.
	 * @param isSpecial
	 *        {@code true} if INVOKESPECIAL method invocation rules 
	 *        must be applied, {@code false} if INVOKEVIRTUAL/INVOKEINTERFACE 
	 *        method invocation rules must be applied. It is ignored if 
	 *        {@code isStatic == true}.
	 * @param thisObject
	 *        a {@link Reference} to the target ("this") of the method invocation.
	 *        It can be {@code null} unless when {@code isStatic == false &&
	 *        isSpecial == false && isRoot == false}.
	 * @return the created {@link Frame}.
	 * @throws ClassFileNotFoundException when the class with name 
	 *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}
	 *         does not exist.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when a method with signature 
	 *         {@code methodSignature} exists but its features differ from
	 *         {@code isStatic} and {@code isSpecial}.
	 * @throws ThreadStackEmptyException when {@code isSpecial == true && isRoot == true}.
	 * @throws PleaseDoNativeException when the method is declared native.
	 */
	private Frame newFrame(Signature methodSignatureResolved, boolean isRoot, boolean isStatic, boolean isSpecial, Reference thisObject) 
	throws ClassFileNotFoundException, MethodNotFoundException, IncompatibleClassFileException, 
	ThreadStackEmptyException, PleaseDoNativeException  {
		//performs method code lookup
		final ClassFile classMethodImpl;
		if (isStatic) {               //INVOKESTATIC
			classMethodImpl = this.classHierarchy.lookupMethodImplStatic(methodSignatureResolved);
		} else if (isSpecial) {       //INVOKESPECIAL
			final String luStartClassName = this.getCurrentMethodSignature().getClassName();
			final ClassFile luStartClass = this.getClassHierarchy().getClassFile(luStartClassName); //current class
			classMethodImpl = this.classHierarchy.lookupMethodImplSpecial(luStartClass, methodSignatureResolved);
		} else {                      //INVOKEVIRTUAL, INVOKEINTERFACE 
			final String luStartClassName = (isRoot ? methodSignatureResolved.getClassName() : getObject(thisObject).getType());
			//TODO can we do better with root frames than taking the static type of the method signature? should we search through the class substitutions?
			final ClassFile luStartClass = this.getClassHierarchy().getClassFile(luStartClassName);
			classMethodImpl = this.classHierarchy.lookupMethodImplVirtualInterface(luStartClass, methodSignatureResolved);
		}

		try {
			//throws if the method is native
			if (classMethodImpl.isMethodNative(methodSignatureResolved)) {
				throw new PleaseDoNativeException();
			}

			//creates the signature of the method that is actually invoked
			final Signature methodSignatureImpl = new Signature(classMethodImpl.getClassName(), methodSignatureResolved.getDescriptor(), methodSignatureResolved.getName());

			//gets the line number table
			final LineNumberTable lnt = classMethodImpl.getLineNumberTable(methodSignatureImpl);

			//gets the method bytecode
			final byte[] bytecode = classMethodImpl.getMethodCodeBySignature(methodSignatureImpl);

			//creates the frame's local variable area from the local variable table
			final LocalVariableTable lvt = classMethodImpl.getLocalVariableTable(methodSignatureImpl);
			final LocalVariablesArea lva = new LocalVariablesArea(lvt);

			//creates the frame
			final Frame f = new Frame(methodSignatureImpl, lnt, bytecode, lva);

			return f;
		} catch (MethodNotFoundException | MethodCodeNotFoundException e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
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
	 * pushed) one.
	 */
	public Frame getRootFrame() {
		final List<Frame> frames = this.stack.frames();
		return frames.get(0);
	}

	/**
	 * Returns the current frame.
	 * 
	 * @return a {@link Frame}, the current (last 
	 * pushed) one.
	 */
	public Frame getCurrentFrame() {
		final List<Frame> frames = this.stack.frames();
		return frames.get(frames.size() - 1);
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
	 * Returns the current program counter.
	 * 
	 * @return an {@code int} representing the state's 
	 *         current program counter.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public int getPC() throws ThreadStackEmptyException {
		return this.stack.currentFrame().getPC();
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
	 * Increments/decrements the program counter by an arbitrary number.
	 * 
	 * @param n the {@code int} value to be added to the current 
	 *          program counter.
	 * @throws InvalidProgramCounterException if the incremented program counter
	 *         would not point to a valid bytecode in the current method 
	 *         (the state's program counter is not changed).
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void incPC(int n) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.setPC(this.getPC() + n);
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
	public void setPC(int newPC) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.stack.currentFrame().setProgramCounter(newPC);
	}

	/**
	 * Copies the state's return program counter into the program counter.
	 * 
	 * @throws InvalidProgramCounterException if the return program 
	 *         counter does not point to a valid bytecode in the current 
	 *         method (the state's program counter is not changed).
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void useReturnPC() 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.stack.currentFrame().useReturnProgramCounter();
	}

	/**
	 * Increments the program counter by {@code 1}.
	 * 
	 * @throws InvalidProgramCounterException if the incremented program counter
	 *         would not point to a valid bytecode in the current method 
	 *         (the state's program counter is not changed).
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void incPC() 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		this.incPC(1);
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
	 * start of symbolic execution.
	 * 
	 * @param className the corresponding concrete class 
	 *        name as a {@link String}. It must be 
	 *        {@link className != null}.
	 * @throws NullPointerException if {@code className} 
	 *         is {@code null}.
     * @throws ClassFileNotFoundException if {@code className} does 
     *         not correspond to a valid class in the classpath.
	 * @throws ThreadStackEmptyException if the thread stack is empty.
	 */
	public void assumeClassInitialized(String className) 
	throws ClassFileNotFoundException, InvalidIndexException {
		if (className == null) {
			throw new NullPointerException();
		}
		if (initialized(className)) {
			; //does nothing
		} else {
			createKlassSymbolic(className);
		}
		final Klass k = this.getKlass(className);
		this.pathCondition.addClauseAssumeClassInitialized(className, k);
		++this.nPushedClauses;
	}

	/**
	 * Assumes that a class is not initialized before the 
	 * start of symbolic execution.
	 * 
	 * @param className the corresponding concrete class 
	 *        name as a {@link String}. It must be 
	 *        {@link className != null}.
	 * @throws NullPointerException if {@code className} 
	 *         violates preconditions.
	 */
	public void assumeClassNotInitialized(String className) {
		if (className == null) {
			throw new NullPointerException();
		}
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
	 * @param descriptor the descriptor of the symbolic value's type.
	 * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
	 *         according to {@code descriptor}.
	 */
	public Value createSymbol(String descriptor, String origin) {
		return this.symbolFactory.createSymbol(descriptor, origin, this.calc);
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
		o.stringLiterals = new HashMap<String, ReferenceConcrete>();
		for (Map.Entry<String, ReferenceConcrete> e : stringLiterals.entrySet()) {
			o.stringLiterals.put(e.getKey(), (ReferenceConcrete) e.getValue().clone());
		}

		//stack
		o.stack = o.stack.clone();

		//heap
		o.heap = o.heap.clone();
		
		//staticStore
		o.staticMethodArea = o.staticMethodArea.clone();

		//pathCondition
		o.pathCondition = o.pathCondition.clone();

		//exc
		if (o.exc != null) {
			o.exc = o.exc.clone();
		}

		//val
		if (o.val != null) {
			o.val = o.val.clone();
		}

		//symbolFactory
		o.symbolFactory = this.symbolFactory.clone();

		return o;
	}
}