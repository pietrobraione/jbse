package jbse.mem;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLASS_NAME;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_HASH;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.common.Type.parametersNumber;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.isPrimitiveCanonicalName;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
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
import jbse.bc.Snippet;
import jbse.bc.SnippetFactory;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Objekt.Epoch;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.CannotRefineException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.MemoryPath;
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

    /** The {@link ReferenceConcrete}s to {@link Instance_JAVA_CLASS}es for nonprimitive types. */
    private HashMap<ClassFile, ReferenceConcrete> classes = new HashMap<>();

    /** The {@link ReferenceConcrete}s to {@link Instance_JAVA_CLASS}es for primitive types. */
    private HashMap<String, ReferenceConcrete> classesPrimitive = new HashMap<>();
    
    /** The identifier of the next {@link Instance_JAVA_CLASSLOADER} to be created. */
    private int nextClassLoaderIdentifier = 1;
    
    /** Maps classloader identifiers to {@link ReferenceConcrete}s to {@link Instance_JAVA_CLASSLOADER}. */
    private ArrayList<ReferenceConcrete> classLoaders = new ArrayList<>();
    
    /** 
     * Used to check whether the {@link Instance_JAVA_CLASSLOADER} for the standard (ext and app) 
     * classloader are ready (this flag is {@code false} iff they are ready). 
     */
    private boolean standardClassLoadersNotReady = true;
    
    /** The {@link ReferenceConcrete}s to {@link Instance}s of {@code java.lang.invoke.MethodType}s. */
    private HashMap<String, ReferenceConcrete> methodTypes = new HashMap<>();
    
    /** Maps file descriptors to (meta-level) open files. */
    private HashMap<Integer, Object> files = new HashMap<>();

    /** The JVM stack of the current execution thread. */
    private ThreadStack stack = new ThreadStack();

    /** The JVM heap. */
    private Heap heap;

    /** 
     * The object that fetches classfiles from the classpath, stores them, 
     * and allows visiting the whole class/interface hierarchy. 
     */
    private ClassHierarchy classHierarchy;

    /** The JVM static method area. */
    private StaticMethodArea staticMethodArea = new StaticMethodArea();
    
    /** {@code true} iff the state is in the initialization phase. */
    private boolean isPhaseInit = true;

    /** The path condition of the state in the execution tree. */
    private PathCondition pathCondition = new PathCondition();

    /** The number of pushed path condition clauses from the last reset. */ 
    private int nPushedClauses = 0;

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
    
    /** 
     * Links signature polymorphic methods that have nonintrinsic
     * (type checking) semantics to their adapter methods, indicated
     * as {@link ReferenceConcrete}s to their respective {@code java.lang.invoke.MemberName}s.
     */
    private HashMap<Signature, ReferenceConcrete> linkInvokers = new HashMap<>();
    
    /** 
     * Links signature polymorphic methods that have nonintrinsic
     * (type checking) semantics to their invocation appendices, indicated
     * as {@link ReferenceConcrete}s to {@code Object[]}s.
     */
    private HashMap<Signature, ReferenceConcrete> linkAppendices = new HashMap<>();
    
    /** The maximum length an array may have to be granted simple representation. */
    private final int maxSimpleArrayLength;

    /** The {@link Calculator}. */
    private final Calculator calc;

    /** 
     * The generator for unambiguous symbol identifiers; mutable
     * because different states at different branches may have different
     * generators, possibly starting from the same numbers. 
     */
    private SymbolFactory symbolFactory;

    /**
     * Constructor of an empty State.
     * 
     * @param maxSimpleArrayLength an {@code int}, the maximum length an array may have
     *        to be granted simple representation.
     * @param maxHeapSize the maximum size of the state's heap expressed as the
     *        maximum number of objects it can store.
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
    public State(int maxSimpleArrayLength,
                 long maxHeapSize,
                 Classpath cp, 
                 Class<? extends ClassFileFactory> fClass, 
                 Map<String, Set<String>> expansionBackdoor, 
                 Calculator calc) 
                 throws InvalidClassFileFactoryClassException {
        this.classLoaders.add(Null.getInstance()); //classloader 0 is the bootstrap classloader
        setStandardFiles();
        this.heap = new Heap(maxHeapSize);
        this.classHierarchy = new ClassHierarchy(cp, fClass, expansionBackdoor);
        this.maxSimpleArrayLength = maxSimpleArrayLength;
        this.calc = calc;
        this.symbolFactory = new SymbolFactory(this.calc);
    }
    
    private void setStandardFiles() {
        try {
            //gets reflectively some fields
            final Field fisInField = FilterInputStream.class.getDeclaredField("in");
            fisInField.setAccessible(true);
            final Field fosOutField = FilterOutputStream.class.getDeclaredField("out");
            fosOutField.setAccessible(true);
            
            //gets the stdin and registers it
            final BufferedInputStream bisIn = (BufferedInputStream) System.in;
            final FileInputStream in = (FileInputStream) fisInField.get(bisIn);
            setFile(0, in);
            
            //gets the stdout and registers it
            final PrintStream psOut = (PrintStream) System.out;
            final BufferedOutputStream bosOut = (BufferedOutputStream) fosOutField.get(psOut);
            final FileOutputStream out = (FileOutputStream) fosOutField.get(bosOut);
            setFile(1, out);
            
            //gets the err and registers it
            final PrintStream psErr = (PrintStream) System.err;
            final BufferedOutputStream bosErr = (BufferedOutputStream) fosOutField.get(psErr);
            final FileOutputStream err = (FileOutputStream) fosOutField.get(bosErr);
            setFile(2, err);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
     *         stack is empty.
     */
    public Value popOperand() throws ThreadStackEmptyException, InvalidNumberOfOperandsException {
        return getCurrentFrame().pop();
    }

    /**
     * Removes the topmost {@code num} elements in the operand stack.
     * 
     * @param num an nonnegative {@code int}.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, or if 
     *         {@code num} is negative.
     */
    public void popOperands(int num) throws ThreadStackEmptyException, InvalidNumberOfOperandsException {
        getCurrentFrame().pop(num);
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
        return getCurrentFrame().top();
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
    //TODO check that only operand stack types (int, long, float, double, reference) can be pushed, or convert smaller values automatically
    public void pushOperand(Value val) throws ThreadStackEmptyException {
        getCurrentFrame().push(val);		
    }

    /**
     * Clears the current operand stack.
     * 
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public void clearOperands() throws ThreadStackEmptyException {
        getCurrentFrame().clear();
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
     * Returns the current class.
     * 
     * @return a {@link ClassFile}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
    public ClassFile getCurrentClass() throws ThreadStackEmptyException {
        return getCurrentFrame().getCurrentClass();
    }

    /**
     * Returns the {@link Signature} of the  
     * current method.
     * 
     * @return a {@link Signature}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
    public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
        return getCurrentFrame().getCurrentMethodSignature();
    }
    
    /**
     * Returns the root class.
     * 
     * @return a {@link ClassFile}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
    public ClassFile getRootClass() throws ThreadStackEmptyException {
        return getRootFrame().getCurrentClass();
    }

    /**
     * Returns the {@link Signature} of the  
     * root method.
     * 
     * @return a {@link Signature}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
    public Signature getRootMethodSignature() throws ThreadStackEmptyException {
        return getRootFrame().getCurrentMethodSignature();
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
        final Frame rootFrame = getRootFrame();
        final Signature rootMethodSignature = getRootMethodSignature();
        try {
            if (rootFrame.getCurrentClass().isMethodStatic(rootMethodSignature)) {
                return null;
            } else {
                try {
                    return (Reference) rootFrame.getLocalVariableValue(ROOT_THIS_SLOT);
                } catch (InvalidSlotException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            }
        } catch (MethodNotFoundException e) {
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
        return getCurrentFrame().getLocalVariableDeclaredName(slot);
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
        return getCurrentFrame().getLocalVariableValue(slot);
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
        getCurrentFrame().setLocalVariableValue(slot, this.stack.currentFrame().getProgramCounter(), val);
    }


    /**
     * Tests whether a class is initialized.
     * 
     * @param classFile a {@link ClassFile}.
     * @return {@code true} iff the {@link State}'s static 
     *         store contains a {@link Klass} object for 
     *         {@link classFile}.
     */
    public boolean existsKlass(ClassFile classFile) {
        return this.staticMethodArea.contains(classFile);
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
     * Checks if this {@link State} is in its
     * initialization phase.
     * 
     * @return {@code true} until the {@link #setPhasePostInit()}
     *         method is invoked.
     */
    public boolean isPhaseInit() {
        return this.isPhaseInit;
    }
    
    /**
     * Sets this state to its post-initizialization
     * phase.
     */
    public void setPhasePostInit() {
        this.isPhaseInit = false;
    }

    /**
     * Returns the {@link Klass} object corresponding to 
     * a given class name.
     * 
     * @param classFile a {@link ClassFile}.
     * @return the {@link Klass} object corresponding to 
     *         the memory representation of the class 
     *         {@code classFile}, or {@code null} 
     *         if the class has not been initialized.
     */
    public Klass getKlass(ClassFile classFile) {
        return this.staticMethodArea.get(classFile);
    }
    
    /**
     * Checks whether a {@link Signature} is linked to an 
     * adapter method. 
     * 
     * @param signature a {@link Signature}.
     * @return {@code true} iff {@code signature} is the
     *         signature of a method that has been previously
     *         linked to an adapter method.
     */
    public boolean isMethodLinked(Signature signature) {
        return this.linkInvokers.containsKey(signature);
    }

    /**
     * Links a signature polymorphic nonintrinsic method
     * to an adapter method, represented as a reference to
     * a {@link java.lang.invoke.MemberName}.
     * 
     * @param signature a {@link Signature}. It should be 
     *        the signature of a signature polymorphic
     *        nonintrinsic method, but this is not checked.
     * @param invoker a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.invoke.MemberName},
     *        but this is not checked.
     * @param appendix a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.Object[]},
     *        but this is not checked.
     * @throws NullPointerException if {@code signature == null || invoker == null || appendix == null}.
     */
    public void link(Signature signature, ReferenceConcrete invoker, ReferenceConcrete appendix) {
        if (signature == null || invoker == null || appendix == null) {
            throw new NullPointerException(); //TODO throw better exception
        }
        this.linkInvokers.put(signature, invoker);
        this.linkAppendices.put(signature, appendix);
    }
    
    /**
     * Returns the adapter method for a linked signature 
     * polymorphic nonintrinsic method.
     * 
     * @param signature a {@link Signature}.
     * @return a {@link ReferenceConcrete} to a {@code java.lang.invoke.MemberName}
     *         set with a previous call to {@link #link(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     */
    public ReferenceConcrete getAdapter(Signature signature) {
        return this.linkInvokers.get(signature);
    }
    
    /**
     * Returns the appendix for a linked signature 
     * polymorphic nonintrinsic method.
     * 
     * @param signature a {@link Signature}.
     * @return a {@link ReferenceConcrete} to an {@code Object[]}
     *         set with a previous call to {@link #link(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     */
    public ReferenceConcrete getAppendix(Signature signature) {
        return this.linkAppendices.get(signature);
    }
    
    /**
     * Returns the file stream associated to a open file descriptor.
     * 
     * @param descriptor an {@code int}.
     * @return a {@link FileInputStream} of a {@link FileOutputStream}, or
     *         {@code null} if {@code descriptor} is not the descriptor
     *         of an open file previously associated with a call to {@link #setFile(int, Object)}.
     */
    public Object getFile(int descriptor) {
        return this.files.get(Integer.valueOf(descriptor));
    }
    
    /**
     * Associates a file stream to an open file descriptor.
     * 
     * @param descriptor an {@code int}, the descriptor of an open file.
     * @param fileStream a {@link FileInputStream} or a {@link FileOutputStream} 
     *        (if it is not an instance of one of these types the method does
     *        nothing).
     */
    public void setFile(int descriptor, Object fileStream) {
        if (fileStream instanceof FileInputStream || fileStream instanceof FileOutputStream) {
            this.files.put(Integer.valueOf(descriptor), fileStream);
        }
    }
    
    /**
     * Removes an open file descriptor and its associated file stream.
     * 
     * @param descriptor an {@code int}, the open file descriptor to remove
     *        (if it is not a previously associated open file descriptor
     *        the method does nothing).
     */
    public void removeFile(int descriptor) {
        this.files.remove(Integer.valueOf(descriptor));
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
     * @param arrayClass
     *        a {@link ClassFile}, the class of the array object.
     * @return a new  {@link ReferenceConcrete} to the newly created object.
     * @throws InvalidTypeException if {@code arrayClass} is invalid.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    public ReferenceConcrete createArray(Value initValue, Primitive length, ClassFile arrayClass) 
    throws InvalidTypeException, HeapMemoryExhaustedException {
        final Array a = new Array(this.calc, false, initValue, length, arrayClass, null, Epoch.EPOCH_AFTER_START, false, this.maxSimpleArrayLength);
        final ReferenceConcrete retVal = new ReferenceConcrete(this.heap.addNew(a));
        initDefaultHashCodeConcrete(a, retVal);
        return retVal;
    }

    /**
     * Creates a new {@link Instance} of a given class in the 
     * heap of the state. The {@link Instance}'s fields are initialized 
     * with the default values for each field's type.
     * It cannot create instances of the {@code java.lang.Class} class.
     * 
     * @param classFile the {@link ClassFile} for the class of the new object.
     * @return a {@link ReferenceConcrete} to the newly created object.
     * @throws HeapMemoryExhaustedException if the heap is full.
     * @throws InvalidTypeException  if {@code classFile} is invalid.
     */
    public ReferenceConcrete createInstance(ClassFile classFile) 
    throws HeapMemoryExhaustedException, InvalidTypeException {
        if (JAVA_CLASS.equals(classFile.getClassName())) {
            //use createInstance_JAVA_CLASS instead
            throw new InvalidTypeException("Cannot use method " + getClass().getName() + ".createInstance to create an instance of java.lang.Class.");
        }
        final Instance myObj = doCreateInstance(classFile);
        final ReferenceConcrete retVal = new ReferenceConcrete(this.heap.addNew(myObj));
        if (myObj instanceof Instance_JAVA_CLASSLOADER) {
            this.classLoaders.add(retVal);
        }
        initDefaultHashCodeConcrete(myObj, retVal);
        return retVal;
    }
    
    /**
     * Creates a new {@link Instance} of a given class in the 
     * heap of the state. It differs from {@link #createInstance(String)}
     * because this method does not check whether the heap memory 
     * was exhausted. Use it only to throw critical errors.
     * 
     * @param classFile the {@link ClassFile} for the class of the new object.
     * @return a {@link ReferenceConcrete} to the newly created object.
     * @throws InvalidTypeException if {@code classFile} is invalid.
     */
    public ReferenceConcrete createInstanceSurely(ClassFile classFile) throws InvalidTypeException {
        if (JAVA_CLASS.equals(classFile.getClassName()) || JAVA_CLASSLOADER.equals(classFile.getClassName())) {
            //cannot be used for that
            throw new InvalidTypeException("Cannot use method " + getClass().getName() + ".createInstanceSurely to create an instance of java.lang.Class or java.lang.Classloader.");
        }
        final Instance myObj = doCreateInstance(classFile);
        final ReferenceConcrete retVal = new ReferenceConcrete(this.heap.addNewSurely(myObj));
        initDefaultHashCodeConcrete(myObj, retVal);
        return retVal;
    }
    
    private Instance doCreateInstance(ClassFile classFile) throws InvalidTypeException {
        if (JAVA_CLASS.equals(classFile.getClassName())) {
            //use createInstance_JAVA_CLASS instead
            throw new RuntimeException(); //TODO better exception
        }
        final Signature[] fieldsSignatures = this.classHierarchy.getAllFields(classFile);
        final int numOfStaticFields = this.classHierarchy.numOfStaticFields(classFile);
        final ClassFile cf_JAVA_CLASSLOADER;
        try {
            cf_JAVA_CLASSLOADER = this.classHierarchy.loadCreateClass(JAVA_CLASSLOADER);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                 InvalidInputException | ClassFileNotAccessibleException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        if (JAVA_CLASSLOADER.equals(classFile.getClassName())) {
            System.out.println("Gotcha");
        }
        if (this.classHierarchy.isSubclass(classFile, cf_JAVA_CLASSLOADER)) {
            return new Instance_JAVA_CLASSLOADER(this.calc, classFile, null, Epoch.EPOCH_AFTER_START, this.nextClassLoaderIdentifier++, numOfStaticFields, fieldsSignatures);
        } else {
            return new Instance(this.calc, classFile, null, Epoch.EPOCH_AFTER_START, numOfStaticFields, fieldsSignatures);
        }
    }

    /**
     * Creates a new {@link Instance} of {@code java.lang.Class} in the 
     * heap of the state (more precisely, creates a {@link Instance_JAVA_CLASS}).
     * Its fields are initialized with the default values for each 
     * field's type (which should not be a problem since all the fields are transient).
     * 
     * @param representedClass the {@link ClassFile} of the class the new {@code Instance_JAVA_CLASS}
     *        must represent.
     * @return a {@link ReferenceConcrete} to the newly created object.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    private ReferenceConcrete createInstance_JAVA_CLASS(ClassFile representedClass) 
    throws HeapMemoryExhaustedException {
        try {
            final ClassFile cf_JAVA_CLASS = this.classHierarchy.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASS);
            if (cf_JAVA_CLASS == null) {
                throw new UnexpectedInternalException("Could not find the classfile for java.lang.Class.");
            }
            final int numOfStaticFields = this.classHierarchy.numOfStaticFields(cf_JAVA_CLASS);
            final Signature[] fieldsSignatures = this.classHierarchy.getAllFields(cf_JAVA_CLASS);
            final Instance myObj = new Instance_JAVA_CLASS(this.calc, cf_JAVA_CLASS, null, Epoch.EPOCH_AFTER_START, representedClass, numOfStaticFields, fieldsSignatures);
            final ReferenceConcrete retVal = new ReferenceConcrete(this.heap.addNew(myObj));
            
            //initializes the fields of the new instance
            
            //hash code
            initDefaultHashCodeConcrete(myObj, retVal);
            
            //name
            final String classNameBinary = binaryClassName(representedClass.getClassName());
            ensureStringLiteral(classNameBinary);
            final ReferenceConcrete classNameString = referenceToStringLiteral(classNameBinary);
            myObj.setFieldValue(JAVA_CLASS_NAME, classNameString);
            
            //class loader
            final int classLoader = (representedClass.isAnonymousUnregistered() ? CLASSLOADER_BOOT : representedClass.getDefiningClassLoader()); //Instance_JAVA_CLASS for anonymous classfiles have the classloader field set to null
            myObj.setFieldValue(JAVA_CLASS_CLASSLOADER, this.classLoaders.get(classLoader));
            
            //TODO more fields
            
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e); //TODO do something better?
        }
    }

    /**
     * Creates a concrete {@link Klass} object and loads it in the 
     * static area of this state. It does not initialize the constant 
     * fields nor loads on the stack of the state the frames for the
     * {@code <clinit>} methods. It does not create {@link Klass} objects
     * for superclasses. If the {@link Klass} already exists it does nothing.
     * 
     * @param classFile a {@link ClassFile}. The method 
     *        creates and loads a {@link Klass} object only for {@code classFile}, 
     *        not for its superclasses in the hierarchy.
     */
    public void ensureKlass(ClassFile classFile) {
        if (existsKlass(classFile)) {
            return;
        }
        final int numOfStaticFields = this.classHierarchy.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.classHierarchy.getAllFields(classFile);
        final Klass k = new Klass(this.calc, null, Objekt.Epoch.EPOCH_AFTER_START, numOfStaticFields, fieldsSignatures);
        k.setObjektDefaultHashCode(this.calc.valInt(0)); //doesn't care because it is not used
        this.staticMethodArea.set(classFile, k);
    }

    /**
     * Creates a symbolic {@link Klass} object and loads it in the 
     * static area of this state. It does not initialize the constant 
     * fields. It does not create {@link Klass} objects
     * for superclasses. If the {@link Klass} already exists it 
     * does nothing.
     * 
     * @param className the name of the class to be loaded.
     * @throws InvalidIndexException if the access to the class 
     *         constant pool fails.
     */
    public void ensureKlassSymbolic(ClassFile classFile) throws InvalidIndexException {
        if (existsKlass(classFile)) {
            return;
        }
        final int numOfStaticFields = this.classHierarchy.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.classHierarchy.getAllFields(classFile);
        final Klass k = new Klass(this.calc, MemoryPath.mkStatic(classFile), Objekt.Epoch.EPOCH_BEFORE_START, numOfStaticFields, fieldsSignatures);
        initWithSymbolicValues(k);
        k.setObjektDefaultHashCode(this.calc.valInt(0)); //doesn't care because it is not used
        this.staticMethodArea.set(classFile, k);
    }

    /**
     * Creates a new {@link Objekt} of a given class in the heap of 
     * the state. The {@link Objekt}'s fields are initialized with symbolic 
     * values.
     *  
     * @param classFile a {@link ClassFile} for either an object or an array class.
     * @param origin a {@link MemoryPath}, the origin of the object.
     * @return a {@code long}, the position in the heap of the newly 
     *         created object.
     * @throws NullPointerException if {@code origin} is {@code null}.
     * @throws InvalidTypeException if {@code classFile} is invalid.
     * @throws HeapMemoryExhaustedException if the heap is full.
     * @throws CannotAssumeSymbolicObjectException if {@code type} is
     *         a class that cannot be assumed to be symbolic
     *         (currently {@code java.lang.Class} and {@code java.lang.ClassLoader}).
     */
    private long createObjectSymbolic(ClassFile classFile, MemoryPath origin) 
    throws InvalidTypeException, HeapMemoryExhaustedException, 
    CannotAssumeSymbolicObjectException {
        if (origin == null) {
            throw new NullPointerException(); //TODO improve?
        }
        final Objekt myObj;
        if (classFile.isArray()) {
            try {
                final Array backingArray = newArraySymbolic(classFile, origin, true);
                final long posBackingArray = this.heap.addNew(backingArray);
                final ReferenceConcrete refToBackingArray = new ReferenceConcrete(posBackingArray);
                myObj = new Array(refToBackingArray, backingArray);
            } catch (InvalidOperandException | InvalidTypeException | NullPointerException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
            initDefaultHashCodeSymbolic(myObj);
        } else if (classFile.isReference()) {
            try {
                myObj = newInstanceSymbolic(classFile, origin);
            } catch (InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        } else {
            throw new InvalidTypeException("Attempted to create a symbolic object with type " + classFile.getClassName() + ".");
        }
        final long pos = this.heap.addNew(myObj);
        return pos;
    }

    private Array newArraySymbolic(ClassFile arrayClass, MemoryPath origin, boolean isInitial) 
    throws InvalidTypeException {
        final Primitive length = (Primitive) createSymbol("" + Type.INT, origin.thenArrayLength());
        final Array obj = new Array(this.calc, true, null, length, arrayClass, origin, Epoch.EPOCH_BEFORE_START, isInitial, this.maxSimpleArrayLength);
        initDefaultHashCodeSymbolic(obj);
        return obj;
    }

    private Instance newInstanceSymbolic(ClassFile classFile, MemoryPath origin) 
    throws CannotAssumeSymbolicObjectException, InvalidTypeException {
        if (JAVA_CLASS.equals(classFile.getClassName()) || JAVA_CLASSLOADER.equals(classFile.getClassName())) {
            throw new CannotAssumeSymbolicObjectException(classFile.getClassName());
        }
        final int numOfStaticFields = this.classHierarchy.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.classHierarchy.getAllFields(classFile);
        final Instance obj = new Instance(this.calc, classFile, origin, Epoch.EPOCH_BEFORE_START, numOfStaticFields, fieldsSignatures);
        initWithSymbolicValues(obj);
        initDefaultHashCodeSymbolic(obj);
        return obj;
    }

    /**
     * Initializes an {@link Objekt} with symbolic values.
     * 
     * @param myObj an {@link Objekt} which will be initialized with 
     *              symbolic values.
     */
    private void initWithSymbolicValues(Objekt myObj) {
        for (final Signature fieldSignature : myObj.getStoredFieldSignatures()) {
            //gets the field signature and name
            final String fieldType = fieldSignature.getDescriptor();
            final String fieldName = fieldSignature.getName();

            //builds a symbolic value from signature and name 
            //and assigns it to the field
            myObj.setFieldValue(fieldSignature, 
                                createSymbol(fieldType, myObj.getOrigin().thenField(fieldName)));
        }
    }

    /**
     * Initializes the hash code of an {@link Objekt} with a concrete value, 
     * the heap position of the object.
     * 
     * @param myObj the {@link Objekt} whose hash code will be initialized.
     * @param myRef a {@link ReferenceConcrete} to {@code myObj}.
     */
    private void initDefaultHashCodeConcrete(Objekt myObj, ReferenceConcrete myRef) {
        myObj.setObjektDefaultHashCode(this.calc.valInt((int) myRef.getHeapPosition()));
    }
    /**
     * Initializes the hash code of an {@link Objekt} with a symbolic value.
     * 
     * @param myObj the {@link Objekt} whose hash code will be initialized.
     */
    private void initDefaultHashCodeSymbolic(Objekt myObj) {
        myObj.setObjektDefaultHashCode((PrimitiveSymbolic) createSymbol("" + Type.INT, myObj.getOrigin().thenHashCode()));
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
     * and for the classes of the members of the created object. 
     * If the literal already exists, does nothing.
     * 
     * @param stringLit a {@link String} representing a string literal.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    public void ensureStringLiteral(String stringLit) throws HeapMemoryExhaustedException {
        if (stringLit == null) {
            throw new NullPointerException("null parameter passed to " + State.class.getName() + ".ensureStringLiteral");
        }
        if (hasStringLiteral(stringLit)) {
            return;
        }

        try {
            final ReferenceConcrete value = createArrayOfChars(stringLit);
            final Simplex hash = this.calc.valInt(stringLit.hashCode());
            final ClassFile cf_JAVA_STRING = this.classHierarchy.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING);
            if (cf_JAVA_STRING == null) {
                throw new UnexpectedInternalException("Could not find classfile for type java.lang.String.");
            }
            final ReferenceConcrete retVal = createInstance(cf_JAVA_STRING);
            final Instance i = (Instance) getObject(retVal);
            i.setFieldValue(JAVA_STRING_VALUE,  value);
            i.setFieldValue(JAVA_STRING_HASH,   hash);
            this.stringLiterals.put(stringLit, retVal);
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Creates an array of characters in this state and initializes
     * it with some text.
     * 
     * @param value the text that will be put in the array.
     * @return a {@link ReferenceConcrete} to the created {@link Instance}.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    private ReferenceConcrete createArrayOfChars(String value) throws HeapMemoryExhaustedException {
        final Simplex stringLength = this.calc.valInt(value.length());
        final ReferenceConcrete retVal;
        try {
            final ClassFile cf_arrayOfCHAR = this.classHierarchy.loadCreateClass("" + Type.ARRAYOF + Type.CHAR);
            retVal = createArray(null, stringLength, cf_arrayOfCHAR);
            final Array a = (Array) this.getObject(retVal);
            for (int k = 0; k < value.length(); ++k) {
                final char c = value.charAt(k);
                a.setFast(this.calc.valInt(k), this.calc.valChar(c));
            }
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                 ClassFileNotAccessibleException | ClassCastException | InvalidOperandException | 
                 InvalidTypeException | InvalidInputException | FastArrayAccessNotAllowedException e) {
            //this should never happen 
            throw new UnexpectedInternalException(e);
        }

        return retVal;
    }

    /**
     * Checks if there is an {@link Instance} of {@code java.lang.Class} 
     * in this state's heap for some class.
     * 
     * @param classFile a {@link ClassFile}.
     * @return {@code true} iff there is a {@link Instance} of {@code java.lang.Class} in 
     *         this state's {@link Heap} corresponding to {@code classFile}.
     */
    public boolean hasInstance_JAVA_CLASS(ClassFile classFile) {
        return (classFile.isPrimitive() ? hasInstance_JAVA_CLASS_primitive(classFile.getClassName()) : this.classes.containsKey(classFile));
    }

    /**
     * Checks if there is an {@link Instance} of {@code java.lang.Class} 
     * in this state's heap for some primitive type.
     * 
     * @param typeName a {@link String} representing a primitive type
     *        canonical name (see JLS v8, section 6.7).
     * @return {@code true} iff there is a {@link Instance} of {@code java.lang.Class} in 
     *         this state's {@link Heap} corresponding to {@code typeName}.
     */
    public boolean hasInstance_JAVA_CLASS_primitive(String typeName) {
        return this.classesPrimitive.containsKey(typeName);
    }

    /**
     * Returns a {@link ReferenceConcrete} to an {@link Instance_JAVA_CLASS} 
     * representing a class. 
     * 
     * @param classFile a {@link ClassFile}.
     * @return a {@link ReferenceConcrete} to the {@link Instance_JAVA_CLASS} in 
     *         this state's {@link Heap}, representing {@code classFile}, 
     *         or {@code null} if such instance does not exist. 
     */
    public ReferenceConcrete referenceToInstance_JAVA_CLASS(ClassFile classFile) {
        return (classFile.isPrimitive() ? referenceToInstance_JAVA_CLASS_primitive(classFile.getClassName()) : this.classes.get(classFile));
    }

    /**
     * Returns a {@link ReferenceConcrete} to an {@link Instance_JAVA_CLASS} 
     * representing a primitive type. 
     * 
     * @param typeName a {@link String} representing a primitive type
     *        canonical name (see JLS v8, section 6.7).
     * @return a {@link ReferenceConcrete} to the {@link Instance_JAVA_CLASS} 
     *         in this state's {@link Heap}, representing the class of 
     *         the primitive type {@code typeName}, or {@code null} if 
     *         such instance does not exist in the heap. 
     */
    public ReferenceConcrete referenceToInstance_JAVA_CLASS_primitive(String typeName) {
        return this.classesPrimitive.get(typeName);
    }

    /**
     * Ensures an {@link Instance_JAVA_CLASS} 
     * corresponding to a class exists in the {@link Heap}. If
     * the instance does not exist, it creates 
     * it, otherwise it does nothing.
     * 
     * @param representedClass a {@link ClassFile}, the class represented
     *        by the created {@link Instance_JAVA_CLASS}.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    public void ensureInstance_JAVA_CLASS(ClassFile representedClass) 
    throws HeapMemoryExhaustedException {
        if (hasInstance_JAVA_CLASS(representedClass)) {
            //nothing to do
            return;
        }
        if (representedClass.isPrimitive()) {
            try {
                ensureInstance_JAVA_CLASS_primitive(representedClass.getClassName());
            } catch (ClassFileNotFoundException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        } else {
            this.classes.put(representedClass, createInstance_JAVA_CLASS(representedClass));
        }
    }

    /**
     * Ensures an {@link Instance_JAVA_CLASS} 
     * corresponding to a primitive type exists in the {@link Heap}. If
     * the instance does not exist, it creates it, otherwise it does 
     * nothing.
     * 
     * @param typeName a {@link String} representing a primitive type
     *        canonical name (see JLS v8, section 6.7).
     * @throws ClassFileNotFoundException if {@code typeName} is not
     *         the canonical name of a primitive type.
     * @throws HeapMemoryExhaustedException if the heap is full.
     */
    public void ensureInstance_JAVA_CLASS_primitive(String typeName) 
    throws ClassFileNotFoundException, HeapMemoryExhaustedException {
        if (hasInstance_JAVA_CLASS_primitive(typeName)) {
            return;
        }
        if (isPrimitiveCanonicalName(typeName)) {
            try {
                final ClassFile cf = this.classHierarchy.getClassFilePrimitive(typeName);
                if (cf == null) {
                    throw new UnexpectedInternalException("Could not find the classfile for the primitive type " + typeName + ".");
                }
                final ReferenceConcrete retVal = createInstance_JAVA_CLASS(cf);
                this.classesPrimitive.put(typeName, retVal);
            } catch (InvalidInputException e) {
                throw new UnexpectedInternalException(e);
            }
        } else {
            throw new ClassFileNotFoundException(typeName + " is not the canonical name of a primitive type or void");
        }
    }
    
    /**
     * Checks if there is an {@link Instance} of {@code java.lang.ClassLoader} (or subclass) 
     * in this state's heap for some classloader identifier.
     * 
     * @param id a {@link int}, the identifier of a classloader.
     * @return {@code true} iff there is a {@link Instance} of {@code java.lang.ClassLoader} 
     *         (or subclass) in this state's {@link Heap} associated to {@code id}.
     */
    public boolean hasInstance_JAVA_CLASSLOADER(int id) {
        return 0 < id && id < this.classLoaders.size();
    }
    
    /**
     * Returns a {@link ReferenceConcrete} to an {@link Instance} 
     * of {@code java.lang.invoke.ClassLoader} (or subclass) for some classloader identifier. 
     * 
     * @param id a {@link int}, the identifier of a classloader.
     * @return a {@link ReferenceConcrete} to the {@link Instance}  of {@code java.lang.ClassLoader}
     *         (or subclass) in this state's {@link Heap} associated to {@code id},
     *         or {@code null} if there is not.
     */
    public ReferenceConcrete referenceToInstance_JAVA_CLASSLOADER(int id) {
        if (hasInstance_JAVA_CLASSLOADER(id)) {
            return this.classLoaders.get(id);
        } else {
            return null;
        }
    }
    
    /**
     * Declares that the standard (extensions and application) class loaders are ready
     * to be used.
     * 
     * @throws InvalidInputException when the {@link Instance_JAVA_CLASSLOADER}s
     *         for the standard classloaders were not created in the heap 
     *         (note that this method does not check that the 
     *         {@link Instance_JAVA_CLASSLOADER}s were also initialized).
     */
    public void setStandardClassLoadersReady() throws InvalidInputException {
        if (!this.standardClassLoadersNotReady) {
            return;
        }
        if (this.classLoaders.size() <= CLASSLOADER_APP) {
            throw new InvalidInputException("Invoked jbse.mem.state.setStandardClassLoadersReady with true parameter, but the standard class loaders were not created yet.");
        }
        this.standardClassLoadersNotReady = false;
    }
    
    /**
     * Checks whether the standard class loaders are
     * not ready to be used.
     * 
     * @return {@code false} iff they have been set ready
     *         by a previous call to 
     *         {@link #setStandardClassLoadersReady()}.
     */
    public boolean areStandardClassLoadersNotReady() {
        return this.standardClassLoadersNotReady;
    }
    
    /**
     * Checks if there is an {@link Instance} of {@code java.lang.invoke.MethodHandle} 
     * in this state's heap for some descriptor.
     * 
     * @param descriptor a {@link String} representing a descriptor. It is
     *        not checked.
     * @return {@code true} iff there is a {@link Instance} in this state's {@link Heap} associated to {@code descriptor}
     *         by a previous call to {@link #setReferenceToInstance_JAVA_METHODTYPE(String, ReferenceConcrete)}.
     */
    public boolean hasInstance_JAVA_METHODTYPE(String descriptor) {
        return this.methodTypes.containsKey(descriptor);
    }
    
    /**
     * Returns a {@link ReferenceConcrete} to an {@link Instance} 
     * of {@code java.lang.invoke.MethodHandle} representing a descriptor. 
     * 
     * @param descriptor a {@link String} representing a descriptor. It is
     *        not checked.
     * @return a {@link ReferenceConcrete} to the {@link Instance} 
     *         in this state's {@link Heap} associated to {@code descriptor}
     *         by a previous call to {@link #setReferenceToInstance_JAVA_METHODTYPE(String, ReferenceConcrete)},
     *         or {@code null} if there is not.
     */
    public ReferenceConcrete referenceToInstance_JAVA_METHODTYPE(String descriptor) {
        return this.methodTypes.get(descriptor);
    }
    
    /**
     * Associates a descriptor to a {@link ReferenceConcrete} to an {@link Instance} 
     * of {@code java.lang.invoke.MethodType} representing it. 
     * 
     * @param descriptor a {@link String} representing a descriptor. It is
     *        not checked.
     * @return a {@link ReferenceConcrete}. It should refer an {@link Instance}
     *         of {@code java.lang.invoke.MethodType} 
     *         in this state's {@link Heap} that is semantically equivalent to
     *         {@code descriptor}, but this is not checked.
     */
    public void setReferenceToInstance_JAVA_METHODTYPE(String descriptor, ReferenceConcrete ref) {
        this.methodTypes.put(descriptor, ref);
    }

    /**
     * Unwinds the stack of this state until it finds an exception 
     * handler for an object. If the thread stack is empty after 
     * unwinding, sets the state to stuck with the unhandled exception
     * throw as a cause.
     * 
     * @param exceptionToThrow a {@link Reference} to a throwable 
     *        {@link Objekt} in the state's {@link Heap}.
     * @throws InvalidInputException if {@code exceptionToThrow} is an unresolved symbolic reference, 
     *         or is a null reference, or is a reference to an object that does not extend {@code java.lang.Throwable}.
     * @throws InvalidIndexException if the exception type field in a row of the exception table 
     *         does not contain the index of a valid CONSTANT_Class in the class constant pool.
     * @throws InvalidProgramCounterException if the program counter handle in a row 
     *         of the exception table does not contain a valid program counter.
     */
    public void unwindStack(Reference exceptionToThrow) 
    throws InvalidInputException, InvalidIndexException, InvalidProgramCounterException {
        //checks that exceptionToThrow is resolved to a throwable Objekt
        final Objekt myException = getObject(exceptionToThrow);
        final ClassFile cf_JAVA_THROWABLE;
        try {
            cf_JAVA_THROWABLE = this.classHierarchy.loadCreateClass(JAVA_THROWABLE);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                 InvalidInputException | ClassFileNotAccessibleException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        if (myException == null || !this.classHierarchy.isSubclass(myException.getType(), cf_JAVA_THROWABLE)) {
            throw new InvalidInputException("Attempted to throw an unresolved or null reference, or a reference to an object that is not Throwable.");
        }

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
                if (getCurrentFrame() instanceof SnippetFrameNoContext) {
                    //cannot catch anything and has no current method either
                    popCurrentFrame();
                    continue; 
                }
                final Signature currentMethodSignature = getCurrentMethodSignature();
                final ExceptionTable myExTable = getCurrentClass().getExceptionTable(currentMethodSignature);
                final ExceptionTableEntry tmpEntry = myExTable.getEntry(excTypes, getPC());
                if (tmpEntry == null) {
                    popCurrentFrame();
                } else {
                    clearOperands();
                    setProgramCounter(tmpEntry.getPCHandle());
                    pushOperand(exceptionToThrow);
                    return;				
                }
            }
        } catch (ThreadStackEmptyException | MethodNotFoundException | 
                 MethodCodeNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Creates a new frame for a (nonnative) method and pushes it 
     * on this state's stack. The actual parameters of the invocation are 
     * initialized with values from the invoking frame's operand stack.
     * 
     * @param classMethodImpl
     *        the {@link ClassFile} containing the bytecode for the method.
     * @param methodSignatureImpl
     *        the {@link Signature} of the method for which the 
     *        frame is built.
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
     * @throws MethodNotFoundException when {@code classMethodImpl}
     *         does not contain a declaration for {@code methodSignatureImpl}.
     * @throws MethodCodeNotFoundException when {@code classMethodImpl}
     *         does not contain bytecode for {@code methodSignatureImpl}.
     * @throws InvalidSlotException when there are 
     *         too many {@code arg}s or some of their types are 
     *         incompatible with their respective slots types.
     * @throws InvalidTypeException when narrowing of an argument (performed to match
     *         the method's signature) fails.
     * @throws InvalidProgramCounterException when {@code isRoot == false} and
     *         {@code returnPCOffset} is not a valid program count offset for the
     *         state's current frame.
     * @throws ThreadStackEmptyException when {@code isRoot == false} and the 
     *         state's thread stack is empty.
     */
    public void pushFrame(ClassFile classMethodImpl, Signature methodSignatureImpl, boolean isRoot, int returnPCOffset, Value... args) 
    throws NullMethodReceiverException, MethodNotFoundException, MethodCodeNotFoundException, InvalidSlotException, 
    InvalidTypeException, InvalidProgramCounterException, ThreadStackEmptyException {
        final boolean isStatic = classMethodImpl.isMethodStatic(methodSignatureImpl);
        
        //checks the "this" parameter (invocation receiver) if necessary
        if (!isStatic) {
            if (args.length == 0 || !(args[0] instanceof Reference)) {
                throw new UnexpectedInternalException("Args for method invocation do not correspond to method signature."); //TODO better exception
            }
            if (isNull((Reference) args[0])) {
                throw new NullMethodReceiverException();
            }
        }
        
        //sets the return program counter
        if (isRoot) {
            //do nothing, after creation the frame has already a dummy return program counter
        } else {
            setReturnProgramCounter(returnPCOffset);
        }

        //narrows the int args if the method signature requires a narrower type
        narrowArgs(args, methodSignatureImpl, isStatic);

        //creates the new frame and sets its args
        final MethodFrame f = new MethodFrame(methodSignatureImpl, classMethodImpl);
        f.setArgs(args);

        //pushes the new frame on the thread stack
        this.stack.push(f);
    }
    
    /**
     * Creates a {@link SnippetFactory} for snippets
     * that can be pushed on the current stack with
     * {@link #pushSnippetFrame(Snippet, int) pushSnippetFrame}.
     * 
     * @return a {@link SnippetFactory}.
     */
    public SnippetFactory snippetFactory() {
        return new SnippetFactory();
    }
    
    /**
     * Creates a new frame for a {@link Snippet} and
     * pushes it on this state's stack. The created frame 
     * will inherit the context of the current frame, and 
     * will operate on its operand stack and local variables.
     * Note that it is possible to wrap only a {@link MethodFrame}, 
     * not another snippet frame.
     * 
     * @param snippet a {@link Snippet}.
     * @param returnPCOffset the offset from the current 
     *        program counter of the return program counter.
     * @throws InvalidProgramCounterException if {@code returnPCOffset} 
     *         is not a valid program count offset for the state's current frame.
     * @throws ThreadStackEmptyException if the state's thread stack is empty.
     * @throws InvalidInputException if {@code wrapCurrentFrame == true} and
     *         {@link #getCurrentFrame()} does not return a {@link MethodFrame}.
     */
    public void pushSnippetFrameWrap(Snippet snippet, int returnPCOffset) 
    throws InvalidProgramCounterException, ThreadStackEmptyException, InvalidInputException {
        try {
            //sets the return program counter
            setReturnProgramCounter(returnPCOffset);

            //creates the new snippet frame
            final Frame f = new SnippetFrameContext(snippet, (MethodFrame) getCurrentFrame());

            //replaces the current frame with the snippet frame
            //that wraps it
            this.stack.pop();
            this.stack.push(f);
        } catch (ClassCastException e) {
            throw new InvalidInputException("Cannot push a snippet frame whose context is not a method frame.");
        }
    }
    
    /**
     * Creates a new frame for a {@link Snippet} and
     * pushes it on this state's stack. The created frame
     * will have its own operand stack and no local variables.
     * 
     * @param snippet a {@link Snippet}.
     * @param returnPCOffset the offset from the current 
     *        program counter of the return program counter.
     * @param definingClassLoader an {@code int}, the defining
     *        class loader that is assumed for the current class
     *        of the frame.
     * @param packageName a {@code String}, the name of the
     *        package that is assumed for the current class
     *        of the frame.
     * @throws InvalidProgramCounterException if {@code returnPCOffset} 
     *         is not a valid program count offset for the state's current frame.
     * @throws ThreadStackEmptyException if the state's thread stack is empty.
     */
    public void pushSnippetFrameNoWrap(Snippet snippet, int returnPCOffset, int definingClassLoader, String packageName) 
    throws InvalidProgramCounterException, ThreadStackEmptyException {
        //sets the return program counter
        setReturnProgramCounter(returnPCOffset);

        //creates the new snippet frame
        final Frame f = new SnippetFrameNoContext(snippet, definingClassLoader, packageName);

        this.stack.push(f);
    }
    
    private void narrowArgs(Value[] args, Signature methodSignatureImpl, boolean isStatic) throws InvalidTypeException {
        final String[] paramsDescriptor = Type.splitParametersDescriptors(methodSignatureImpl.getDescriptor());
        for (int i = 0; i < paramsDescriptor.length; ++i) {
            if (Type.isPrimitive(paramsDescriptor[i]) && ! Type.isPrimitiveOpStack(paramsDescriptor[i].charAt(0))) {
                final int indexArg = (isStatic ? i : i + 1);
                args[indexArg] = ((Primitive) args[indexArg]).narrow(paramsDescriptor[i].charAt(0));
            }
        }
    }

    /**
     * Makes symbolic arguments for the root method invocation. This includes the
     * root object.
     * @param f the root {@link MethodFrame}.
     * @param isStatic
     *        {@code true} iff INVOKESTATIC method invocation rules 
     *        must be applied.
     * @return a {@link Value}{@code []}, the array of the symbolic parameters
     *         for the method call. Note that the reference to the root object
     *         is a {@link ReferenceSymbolic}.
     * @throws HeapMemoryExhaustedException if the heap is full.
     * @throws CannotAssumeSymbolicObjectException if the root object has class 
     *         {@code java.lang.Class} or {@code java.lang.ClassLoader}.
     */
    private Value[] makeArgsSymbolic(MethodFrame f, boolean isStatic) 
    throws HeapMemoryExhaustedException, CannotAssumeSymbolicObjectException {
        final Signature methodSignature = f.getCurrentMethodSignature();
        final String[] paramsDescriptors = Type.splitParametersDescriptors(methodSignature.getDescriptor());
        final int numArgs = parametersNumber(methodSignature.getDescriptor(), isStatic);

        //produces the args as symbolic values from the method's signature
        final ClassFile currentClass = f.getCurrentClass();
        final String currentClassName = currentClass.getClassName();
        final Value[] args = new Value[numArgs];
        for (int i = 0, slot = 0; i < numArgs; ++i) {
            //builds a symbolic value from signature and name
            final MemoryPath origin = MemoryPath.mkLocalVariable(f.getLocalVariableDeclaredName(slot));
            if (slot == ROOT_THIS_SLOT && !isStatic) {
                args[i] = createSymbol(Type.REFERENCE + currentClassName + Type.TYPEEND, origin);
                //must assume {ROOT}:this expands to nonnull object (were it null the frame would not exist!)
                try {
                    assumeExpands((ReferenceSymbolic) args[i], currentClass);
                } catch (InvalidTypeException | ContradictionException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            } else {
                args[i] = createSymbol(paramsDescriptors[(isStatic ? i : i - 1)], origin);
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
     * @param classMethodImpl
     *        the {@link ClassFile} containing the bytecode for the method.
     * @param methodSignatureImpl 
     *        the {@link Signature} of the method for which the 
     *        frame is built. The bytecode for the method will be
     *        looked for in 
     *        {@code methodSignatureImpl.}{@link Signature#getClassName() getClassName()}.
     * @return a {@link ReferenceSymbolic}, the "this" (target) of the method invocation
     *         if the invocation is not static, otherwise {@code null}.
     * @throws MethodNotFoundException when {@code classMethodImpl}
     *         does not contain a declaration for {@code methodSignatureImpl}.
     * @throws MethodCodeNotFoundException when {@code classMethodImpl}
     *         does not contain bytecode for {@code methodSignatureImpl}.
     * @throws HeapMemoryExhaustedException if the heap is full.
     * @throws CannotAssumeSymbolicObjectException if the target of the method invocation 
     *         has class {@code java.lang.Class} or {@code java.lang.ClassLoader}.
     */
    public ReferenceSymbolic pushFrameSymbolic(ClassFile classMethodImpl, Signature methodSignatureImpl) 
    throws MethodNotFoundException, MethodCodeNotFoundException, 
    HeapMemoryExhaustedException, CannotAssumeSymbolicObjectException {
        final boolean isStatic = classMethodImpl.isMethodStatic(methodSignatureImpl);
        final MethodFrame f = new MethodFrame(methodSignatureImpl, classMethodImpl);
        final Value[] args = makeArgsSymbolic(f, isStatic);
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
     * {@code this} parameter as found on the operand stack. 
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
        getCurrentFrame().setReturnProgramCounter(returnPCOffset);
    }

    /**
     * Removes the current {@link Frame} from the thread stack.
     * 
     * @return the popped {@link Frame}.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public Frame popCurrentFrame() throws ThreadStackEmptyException {
        final Frame popped = this.stack.pop();
        if (popped instanceof SnippetFrameContext) {
            //reinstates the activation context of the popped frame
            this.stack.push(((SnippetFrameContext) popped).getContextFrame());
        }
        return popped;
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
     * @return a {@link MethodFrame}, the root (first  
     *         pushed) one.
     * @throws ThreadStackEmptyException if the 
     *         thread stack is empty.
     */
    public MethodFrame getRootFrame() throws ThreadStackEmptyException {
        return (MethodFrame) this.stack.rootFrame();
    }

    /**
     * Returns the current frame.
     * 
     * @return an {@link Frame}, the current (last 
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
     * @return a {@link List}{@code <}{@link Frame}{@code >} 
     *         of the method activation frames in the thread stack, 
     *         in their push order.
     */
    public List<Frame> getStack() {
        return this.stack.frames();
    }

    /**
     * Returns the size of the thread stack.
     * 
     * @return an {@code int}, the size.
     */
    public int getStackSize() {
        return getStack().size();
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

    /**
     * Returns all the symbolic objects of this state according
     * to its path condition.
     * 
     * @return an {@link Iterable}{@code <}{@link Objekt}{@code >}
     *         that iterates through all the objects in the {@link ClauseAssumeExpands}
     *         in the state's path condition.
     */
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
     * immutable {@link Map}{@code <}{@link ClassFile}{@code , }{@link Klass}{@code >}.
     */
    public Map<ClassFile, Klass> getStaticMethodArea() {
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
        return getCurrentFrame().getInstruction();
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
        return getCurrentFrame().getInstruction(displacement);
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
        return getCurrentFrame().getSourceRow();
    }

    /**
     * Returns the current program counter.
     * 
     * @return an {@code int} representing the state's 
     *         current program counter.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public int getPC() throws ThreadStackEmptyException {
        return getCurrentFrame().getProgramCounter();
    }

    /**
     * Returns the return program counter of the caller frame
     * stored for a return bytecode.
     * 
     * @return an {@code int}, the return program counter.
     * @throws ThreadStackEmptyException  if the thread stack is empty.
     */
    public int getReturnPC() throws ThreadStackEmptyException {
        return getCurrentFrame().getReturnProgramCounter();
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
        setProgramCounter(getPC() + n);
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
        getCurrentFrame().setProgramCounter(newPC);
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
     * @param classFile a {@code ClassFile}, the class of the fresh 
     *        object to which {@code r} must be expanded. It must be {@code classFile != null}.
     * @throws NullPointerException if either {@code r} or {@code classFile} is
     *         {@code null}.
     * @throws InvalidTypeException if {@code classFile} is invalid. 
     * @throws ContradictionException if {@code r} is already resolved.
     * @throws HeapMemoryExhaustedException if the heap is full.
     * @throws CannotAssumeSymbolicObjectException if {@code classFile} is
     *         a class that cannot be assumed to be symbolic
     *         (currently {@code java.lang.Class} and {@code java.lang.ClassLoader}).
     */
    public void assumeExpands(ReferenceSymbolic r, ClassFile classFile) 
    throws InvalidTypeException, ContradictionException, HeapMemoryExhaustedException, 
    CannotAssumeSymbolicObjectException {
        if (r == null || classFile == null) {
            throw new NullPointerException(); //TODO find a better exception
        }
        if (resolved(r)) {
            throw new ContradictionException();
        }
        final long pos = createObjectSymbolic(classFile, r.getOrigin());
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
     * start of symbolic execution.
     * 
     * @param classFile the corresponding concrete class. 
     *        It must be {@code classFile != null}.
     * @param k the symbolic {@link Klass} for {@code classFile}, 
     *        or {@code null} if the initial class is not symbolic. 
     * @throws NullPointerException if {@code classFile == null}.
     * @throws InvalidIndexException if the access to the class 
     *         constant pool fails.
     */
    public void assumeClassInitialized(ClassFile classFile, Klass k) 
    throws InvalidIndexException {
        if (classFile == null) {
            throw new NullPointerException();
        }
        this.pathCondition.addClauseAssumeClassInitialized(classFile, k);
        ++this.nPushedClauses;
    }

    /**
     * Assumes that a class is not initialized before the 
     * start of symbolic execution.
     * 
     * @param classFile a {@link ClassFile}.
     * @throws NullPointerException if {@code classFile} 
     *         is {@code null}.
     */
    public void assumeClassNotInitialized(ClassFile classFile) {
        if (classFile == null) {
            throw new NullPointerException();
        }
        this.pathCondition.addClauseAssumeClassNotInitialized(classFile);
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
        //TODO this method doesn't work with arrays!!!
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
                final ClassFile cf = cCl.getClassFile();
                final Klass k = cCl.getKlass(); //note that the getter produces a safety copy
                if (k != null) {
                    this.staticMethodArea.set(cf, k);
                }
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
     * @param origin a {@link MemoryPath}, the origin of this reference.
     * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
     *         according to {@code descriptor}.
     */
    public Value createSymbol(String staticType, MemoryPath origin) {
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
                tmp += "CurrentMethod:" + getCurrentFrame().getCurrentMethodSignature() + ", ";
                tmp += "ProgramCounter:" + getPC() + ", ";
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

        //classes
        o.classes = new HashMap<>(o.classes);

        //classesPrimitive
        o.classesPrimitive = new HashMap<>(o.classesPrimitive);
        
        //methodTypes
        o.methodTypes = new HashMap<>(o.methodTypes);
        
        //files
        try {
            o.files = new HashMap<>();
            final Field fisPath = FileInputStream.class.getDeclaredField("path");
            fisPath.setAccessible(true);
            final Field fosPath = FileOutputStream.class.getDeclaredField("path");
            fosPath.setAccessible(true);
            for (Map.Entry<Integer, Object> entry : this.files.entrySet()) {
                if (entry.getValue() instanceof FileInputStream) {
                    final FileInputStream fisClone;
                    if (entry.getKey() == 0) {
                        fisClone = (FileInputStream) entry.getValue();
                    } else {
                        final FileInputStream fisThis = (FileInputStream) entry.getValue();
                        final String path = (String) fisPath.get(fisThis);
                        fisClone = new FileInputStream(path);
                        fisClone.skip(fisThis.getChannel().position());
                    }
                    o.files.put(entry.getKey(), fisClone);
                } else {
                    final FileOutputStream fosClone;
                    if (entry.getKey() == 1 ||  entry.getKey() == 2) {
                        fosClone = (FileOutputStream) entry.getValue();
                    } else {
                        final FileOutputStream fosThis = (FileOutputStream) entry.getValue();
                        final String path = (String) fosPath.get(fosThis);
                        fosClone = new FileOutputStream(path);
                    }
                    o.files.put(entry.getKey(), fosClone);
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | 
                 IllegalAccessException | IOException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }

        //stack
        o.stack = o.stack.clone();

        //heap
        o.heap = o.heap.clone();
        
        //classHierarchy
        o.classHierarchy = o.classHierarchy.clone();

        //staticStore
        o.staticMethodArea = o.staticMethodArea.clone();

        //pathCondition
        o.pathCondition = o.pathCondition.clone();

        //exc and val are values, so they are immutable

        //symbolFactory
        o.symbolFactory = o.symbolFactory.clone();
        
        //linkInvokers
        o.linkInvokers = new HashMap<>(o.linkInvokers);
        
        //linkAppendices
        o.linkAppendices = new HashMap<>(o.linkAppendices);
        
        //all other members are immutable

        return o;
    }
}
