package jbse.algo;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOOLEAN_FALSE;
import static jbse.bc.Signatures.JAVA_BOOLEAN_TRUE;
import static jbse.bc.Signatures.JAVA_BYTE_BYTECACHE;
import static jbse.bc.Signatures.JAVA_BYTE_BYTECACHE_CACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_CHARACTERCACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER_CHARACTERCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER_VALUE;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_LOADCLASS;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_LOW;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_HIGH;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_LONGCACHE;
import static jbse.bc.Signatures.JAVA_LONG_LONGCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_GETTYPE;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKCALLSITE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKMETHOD;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKMETHODHANDLECONSTANT;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_SHORTCACHE;
import static jbse.bc.Signatures.JAVA_SHORT_SHORTCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_SHORT_VALUE;
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
import static jbse.bc.Signatures.noclass_REGISTERMETHODHANDLE;
import static jbse.bc.Signatures.noclass_REGISTERMETHODTYPE;
import static jbse.bc.Signatures.noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX;
import static jbse.bc.Signatures.noclass_STORELINKEDMETHODADAPTERANDAPPENDIX;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.INT;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import jbse.algo.exc.BaseUnsupportedException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.CallSiteSpecifier;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolMethodHandle;
import jbse.bc.ConstantPoolMethodType;
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
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlgorithms.ArrayAccessInfo;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Clause;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.mem.Frame;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Klass;
import jbse.mem.SnippetFrameNoWrap;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Util {
    //taken from java.lang.reflect.Modifier
    public static final short BRIDGE    = 0x0040;
    public static final short VARARGS   = 0x0080;
    public static final short SYNTHETIC = 0x1000;
    public static final short ENUM      = 0x4000;
    
    //taken from hotspot:/src/share/vm/prims/jvm.h, lines 1216-1237
    public static final short JVM_RECOGNIZED_FIELD_MODIFIERS = 
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.VOLATILE  | 
        Modifier.TRANSIENT | ENUM             | SYNTHETIC;

    public static final short JVM_RECOGNIZED_METHOD_MODIFIERS =
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED    | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.SYNCHRONIZED | 
        BRIDGE             | VARARGS          | Modifier.NATIVE       |
        Modifier.ABSTRACT  | Modifier.STRICT  | SYNTHETIC;

    //taken from java.lang.invoke.MethodHandleNatives.Constants
    public static final int  IS_METHOD            = 0x00010000; // method (not constructor)
    public static final int  IS_CONSTRUCTOR       = 0x00020000; // constructor
    public static final int  IS_FIELD             = 0x00040000; // field
    public static final int  IS_TYPE              = 0x00080000; // nested type
    public static final int  CALLER_SENSITIVE     = 0x00100000; // @CallerSensitive annotation detected
    public static final int  REFERENCE_KIND_SHIFT = 24; // refKind
    public static final int  REFERENCE_KIND_MASK  = 0x0F000000 >> REFERENCE_KIND_SHIFT;
    public static final int  SEARCH_SUPERCLASSES  = 0x00100000;
    public static final int  SEARCH_INTERFACES    = 0x00200000;
    public static final byte REF_getField         = 1;
    public static final byte REF_getStatic        = 2;
    public static final byte REF_putField         = 3;
    public static final byte REF_putStatic        = 4;
    public static final byte REF_invokeVirtual    = 5;
    public static final byte REF_invokeStatic     = 6;
    public static final byte REF_invokeSpecial    = 7;
    public static final byte REF_newInvokeSpecial = 8;
    public static final byte REF_invokeInterface  = 9;

    //taken from java.lang.invoke.MemberName
    public static boolean isMethod(int flags) {
        return (flags & IS_METHOD) == IS_METHOD;
    }

    //taken from java.lang.invoke.MemberName
    public static boolean isConstructor(int flags) {
        return (flags & IS_CONSTRUCTOR) == IS_CONSTRUCTOR;
    }

    //taken from java.lang.invoke.MemberName
    public static boolean isField(int flags) {
        return (flags & IS_FIELD) == IS_FIELD;
    }
    
    //taken from java.lang.invoke.MemberName
    public static boolean isStatic(int flags) {
        return Modifier.isStatic(flags);
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    public static boolean isInvokeInterface(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) == REF_invokeInterface;
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    public static boolean isSetter(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) > REF_getStatic;
    }
    
	//The check has some code taken from CallInfo::CallInfo(Method*, Klass*)
	//for unreflecting the method, see hotspot:/src/share/vm/interpreter/linkResolver.cpp,
	//that is invoked by MethodHandles::init_MemberName at row 169, 
	//hotspot:/src/share/vm/prims/methodHandles.cpp (note that we skip
	//the case miranda+default because they seem specific to the
	//Hotspot case)
    public static int getMemberNameFlagsMethod(ClassFile cfMethod, Signature methodSignature) throws MethodNotFoundException {
		int flags = (short) (((short) cfMethod.getMethodModifiers(methodSignature)) & JVM_RECOGNIZED_METHOD_MODIFIERS);
		if (canBeStaticallyBound(cfMethod, methodSignature)) {
			if (cfMethod.isMethodStatic(methodSignature)) {
				flags |= IS_METHOD      | (REF_invokeStatic  << REFERENCE_KIND_SHIFT);
			} else if ("<init>".equals(methodSignature.getName()) || "<clinit>".equals(methodSignature.getName())) {
				flags |= IS_CONSTRUCTOR | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
			} else { //it is private (invokespecial) or is final (invokevirtual)
				flags |= IS_METHOD      | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
			}
		} else if (cfMethod.isInterface()) {
			flags |= IS_METHOD | (REF_invokeInterface << REFERENCE_KIND_SHIFT);
		} else {
			flags |= IS_METHOD | (REF_invokeVirtual << REFERENCE_KIND_SHIFT);
		}
		
        if (cfMethod.isMethodCallerSensitive(methodSignature)) {
            flags |= CALLER_SENSITIVE;
        }
        
        return flags;
    }
    	
	private static boolean canBeStaticallyBound(ClassFile cfMethod, Signature methodSignature) throws MethodNotFoundException {
		//a default method cannot be statically bound
		if (cfMethod.isInterface() && !cfMethod.isMethodAbstract(methodSignature)) {
			return false;
		}
		
		//a final method can be statically bound
		if (cfMethod.isMethodFinal(methodSignature) || cfMethod.isFinal()) {
			return true;
		}
		
		//a static method is statically bound (invokestatic)
		if (cfMethod.isMethodStatic(methodSignature)) {
			return true;
		}
		
		//a constructor is statically bound (invokespecial)
		if ("<init>".equals(methodSignature.getName()) || "<clinit>".equals(methodSignature.getName())) {
			return true;
		}
		
		//a private method is statically bound (invokespecial)
		if (cfMethod.isMethodPrivate(methodSignature)) {
			return true;
		}
		
		//no other method can be statically bound
		return false;
	}
	
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
     * Ensures that a signature polymorphic nonintrinsic method is linked to an adapter/appendix. 
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param accessorJavaClass the {@link Reference} to the {@link Instance_JAVA_CLASS} of the accessor.
     *        It can be a {@link jbse.val.Null Null} reference (e.g., {@code null} is allowed as second 
     *        parameter of a {@link java.lang.invoke.MethodHandleNatives#resolve(java.lang.invoke.MemberName, Class) resolve} call), 
     *        but when it is not it must represent the {@code accessor} parameter.
     * @param accessor a {@link ClassFile}, the accessor class invoking the signature-polymorphic 
     *        method.
     * @param polymorphicMethodSignatureSpecialized the {@link Signature} of the signature-polymorphic method, with 
     *        the descriptor as declared in the member name (i.e., specialized on the actual arguments).
     * @throws PleaseLoadClassException if the execution of the invoking {@link Algorithm} must be interrupted 
     *         because a class referred in {@code resolved} must be loaded by a user-defined classloader.
     * @throws ClassFileNotFoundException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} is not found in the classpath.
     * @throws ClassFileIllFormedException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} has a version number
     *         that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException if the name of any 
     *         class referred in {@code polymorphicMethodDescriptor} is wrong.
     * @throws IncompatibleClassFileException if the superclass of any 
     *         class referred in {@code polymorphicMethodDescriptor} is resolved to an 
     *         interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the classfile for any 
     *         class referred in {@code polymorphicMethodDescriptor} is not accessible by {@code accessor}.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty (should never happen).
     * @throws InterruptException always thrown at the end of the method, to interrupt the execution of 
     *         this {@link Algorithm} and perform the upcall.
     * @throws InvalidInputException if an invalid input is used by some method call. 
     */
    public static void ensureMethodLinked(State state, Calculator calc, Reference accessorJavaClass, ClassFile accessor, Signature polymorphicMethodSignatureSpecialized) 
    throws PleaseLoadClassException, ClassFileNotFoundException, ClassFileIllFormedException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, HeapMemoryExhaustedException, 
    ThreadStackEmptyException, InterruptException, InvalidInputException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException {
        //fast track: the method is already linked
        if (state.isMethodLinked(polymorphicMethodSignatureSpecialized)) {
            return;
        }
        
        final String polymorphicMethodName = polymorphicMethodSignatureSpecialized.getName();

        //upcalls java.lang.invoke.MethodHandleNatives.linkMethod
        //and stores the link, see hotspot:/src/share/vm/prims/systemDictionary.cpp 
        //lines 2377-2394        

        //prepares the parameters for the upcall:

        //1- instance of java.lang.Class<java.lang.invoke.MethodHandle>
        ClassFile cf_JAVA_METHODHANDLE = null; //to keep the compiler happy
        try {
        	cf_JAVA_METHODHANDLE = state.getClassHierarchy().loadCreateClass(JAVA_METHODHANDLE);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
        		BadClassFileVersionException | WrongClassNameException | ClassFileNotAccessibleException e) {
        	//this should never happen
        	failExecution(e);
        }
        state.ensureInstance_JAVA_CLASS(calc, cf_JAVA_METHODHANDLE);
        final ReferenceConcrete mhClassRef = state.referenceToInstance_JAVA_CLASS(cf_JAVA_METHODHANDLE);

        //2- the name of the resolved method 
        state.ensureStringLiteral(calc, polymorphicMethodName);
        final ReferenceConcrete mhNameRef = state.referenceToStringLiteral(polymorphicMethodName);

        //3- a java.lang.invoke.MethodType for its descriptor
        final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(accessor, polymorphicMethodSignatureSpecialized.getDescriptor(), state.bypassStandardLoading());
        ensureInstance_JAVA_METHODTYPE(state, calc, descriptorResolved);
        final ReferenceConcrete mtRef = state.referenceToInstance_JAVA_METHODTYPE(descriptorResolved);

        //4- an array with length 1 to host the returned appendix (if any)
        ClassFile cf_arrayOfJAVA_OBJECT = null; //to keep the compiler happy
        try {
        	cf_arrayOfJAVA_OBJECT = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
        		BadClassFileVersionException | WrongClassNameException | ClassFileNotAccessibleException e) {
        	//this should never happen
        	failExecution(e);
        }
        final ReferenceConcrete appendixBox = state.createArray(calc, null, calc.valInt(1), cf_arrayOfJAVA_OBJECT);

        //upcalls
        final Snippet snippet = state.snippetFactoryNoWrap()
        		//parameters for the upcall to noclass_STORELINKEDMETHODANDAPPENDIX
        		.addArg(mhNameRef) //name of the method, either invoke or invokeExact
        		.addArg(mtRef) //java.lang.invoke.MethodType instance for the method's descriptor
        		.addArg(appendixBox) //appendix
        		//parameters for the upcall to JAVA_METHODHANDLENATIVES_LINKMETHOD                    
        		.addArg(accessorJavaClass)
        		.addArg(calc.valInt(REF_invokeVirtual)) //kind (MUST be REF_invokeVirtual)
        		.addArg(mhClassRef) //class where the method is defined (MUST be java.lang.invoke.MethodHandle)
        		.addArg(mhNameRef) //name of the method, either invoke or invokeExact
        		.addArg(mtRef) //java.lang.invoke.MethodType instance for the method's descriptor
        		.addArg(appendixBox) //appendix
        		//pushes everything
        		.op_aload((byte) 0)
        		.op_aload((byte) 1)
        		.op_aload((byte) 2)
        		.op_aload((byte) 3)
        		.op_iload((byte) 4)
        		.op_aload((byte) 5)
        		.op_aload((byte) 6)
        		.op_aload((byte) 7)
        		.op_aload((byte) 8)
        		.op_invokestatic(JAVA_METHODHANDLENATIVES_LINKMETHOD)
        		//the next call to getType ensures that the returned MemberName 
        		//is normalized, so that its type field is a MethodType
        		.op_dup()
        		.op_invokevirtual(JAVA_MEMBERNAME_GETTYPE)
        		.op_pop() //we care only of the side effect
        		//stores the linked method and the appendix in the state
        		.op_invokestatic(noclass_STORELINKEDMETHODADAPTERANDAPPENDIX)
        		.op_return()
        		.mk();
        
        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
        try {
        	state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME);  //zero offset so that upon return from the snippet will reexecute the current algorithm
        } catch (InvalidProgramCounterException e) {
        	//this should never happen
        	failExecution(e);
        }
        exitFromAlgorithm();
    }
    
    private static Reference doPrimitiveBoxing(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, HeapMemoryExhaustedException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
    	final char type = val.getType();
    	if (type == BOOLEAN) {
    		final boolean valIsTrue = ((Boolean) val.getActualValue()).booleanValue();
    		final ClassFile cf_JAVA_BOOLEAN = state.getClassHierarchy().loadCreateClass(JAVA_BOOLEAN);
    		final Klass klassBoolean = state.getKlass(cf_JAVA_BOOLEAN);
    		return (Reference) (valIsTrue ? klassBoolean.getFieldValue(JAVA_BOOLEAN_TRUE) : klassBoolean.getFieldValue(JAVA_BOOLEAN_FALSE));
    	} else if (type == BYTE) {
    		final byte valByte = ((Byte) val.getActualValue()).byteValue();
    		final ClassFile cf_JAVA_BYTE_BYTECACHE = state.getClassHierarchy().loadCreateClass(JAVA_BYTE_BYTECACHE);
    		final Klass klassBytecache = state.getKlass(cf_JAVA_BYTE_BYTECACHE);
    		final Reference referenceCache = (Reference) klassBytecache.getFieldValue(JAVA_BYTE_BYTECACHE_CACHE);
    		final Array cache = (Array) state.getObject(referenceCache);
    		final Simplex index = calc.valInt(((int) valByte) + 128);
    		try {
				final AccessOutcomeInValue outcome = (AccessOutcomeInValue) cache.getFast(calc, index);
				return (Reference) outcome.getValue();
			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
    	} else if (type == CHAR) {
    		final char valChar = ((Character) val.getActualValue()).charValue();
    		if (valChar <= 127) { //the cached values
        		final ClassFile cf_JAVA_CHARACTER_CHARACTERCACHE = state.getClassHierarchy().loadCreateClass(JAVA_CHARACTER_CHARACTERCACHE);
        		final Klass klassCharactercache = state.getKlass(cf_JAVA_CHARACTER_CHARACTERCACHE);
        		final Reference referenceCache = (Reference) klassCharactercache.getFieldValue(JAVA_CHARACTER_CHARACTERCACHE_CACHE);
        		final Array cache = (Array) state.getObject(referenceCache);
        		final Simplex index = calc.valInt((int) valChar);
        		try {
    				final AccessOutcomeInValue outcome = (AccessOutcomeInValue) cache.getFast(calc, index);
    				return (Reference) outcome.getValue();
    			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
    				//this should never happen
    				throw new UnexpectedInternalException(e);
    			}
    		} else {
        		final ClassFile cf_JAVA_CHARACTER = state.getClassHierarchy().loadCreateClass(JAVA_CHARACTER);
    			final ReferenceConcrete referenceCharacter = state.createInstance(calc, cf_JAVA_CHARACTER);
    			final HeapObjekt characterObject = state.getObject(referenceCharacter);
    			characterObject.setFieldValue(JAVA_CHARACTER_VALUE, val);
    			return referenceCharacter;
    		}
    	} else if (type == DOUBLE) {
    		final ClassFile cf_JAVA_DOUBLE = state.getClassHierarchy().loadCreateClass(JAVA_DOUBLE);
			final ReferenceConcrete referenceDouble = state.createInstance(calc, cf_JAVA_DOUBLE);
			final HeapObjekt doubleObject = state.getObject(referenceDouble);
			doubleObject.setFieldValue(JAVA_DOUBLE_VALUE, val);
			return referenceDouble;
    	} else if (type == INT) {
    		final int valInt = ((Integer) val.getActualValue()).intValue();
    		final ClassFile cf_JAVA_INTEGER_INTEGERCACHE = state.getClassHierarchy().loadCreateClass(JAVA_INTEGER_INTEGERCACHE);
    		final Klass klassIntegercache = state.getKlass(cf_JAVA_INTEGER_INTEGERCACHE);
    		final int low = ((Integer) ((Simplex) klassIntegercache.getFieldValue(JAVA_INTEGER_INTEGERCACHE_LOW)).getActualValue()).intValue();
    		final int high = ((Integer) ((Simplex) klassIntegercache.getFieldValue(JAVA_INTEGER_INTEGERCACHE_HIGH)).getActualValue()).intValue();
    		if (valInt >= low && valInt <= high) { //the cached values
        		final Reference referenceCache = (Reference) klassIntegercache.getFieldValue(JAVA_INTEGER_INTEGERCACHE_CACHE);
        		final Array cache = (Array) state.getObject(referenceCache);
        		final Simplex index = calc.valInt(valInt + (-low));
        		try {
    				final AccessOutcomeInValue outcome = (AccessOutcomeInValue) cache.getFast(calc, index);
    				return (Reference) outcome.getValue();
    			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
    				//this should never happen
    				throw new UnexpectedInternalException(e);
    			}
    		} else {
        		final ClassFile cf_JAVA_INTEGER = state.getClassHierarchy().loadCreateClass(JAVA_INTEGER);
    			final ReferenceConcrete referenceInteger = state.createInstance(calc, cf_JAVA_INTEGER);
    			final HeapObjekt integerObject = state.getObject(referenceInteger);
    			integerObject.setFieldValue(JAVA_INTEGER_VALUE, val);
    			return referenceInteger;
    		}    		
    	} else if (type == FLOAT) {
    		final ClassFile cf_JAVA_FLOAT = state.getClassHierarchy().loadCreateClass(JAVA_FLOAT);
			final ReferenceConcrete referenceFloat = state.createInstance(calc, cf_JAVA_FLOAT);
			final HeapObjekt floatObject = state.getObject(referenceFloat);
			floatObject.setFieldValue(JAVA_FLOAT_VALUE, val);
			return referenceFloat;
    	} else if (type == LONG) {
    		final long valLong = ((Long) val.getActualValue()).longValue();
    		if (valLong >= -128 && valLong <= 127) { //the cached values
        		final ClassFile cf_JAVA_LONG_LONGCACHE = state.getClassHierarchy().loadCreateClass(JAVA_LONG_LONGCACHE);
        		final Klass klassLongcache = state.getKlass(cf_JAVA_LONG_LONGCACHE);
        		final Reference referenceCache = (Reference) klassLongcache.getFieldValue(JAVA_LONG_LONGCACHE_CACHE);
        		final Array cache = (Array) state.getObject(referenceCache);
        		final Simplex index = calc.valInt(((int) valLong) + 128);
        		try {
    				final AccessOutcomeInValue outcome = (AccessOutcomeInValue) cache.getFast(calc, index);
    				return (Reference) outcome.getValue();
    			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
    				//this should never happen
    				throw new UnexpectedInternalException(e);
    			}
    		} else {
        		final ClassFile cf_JAVA_LONG = state.getClassHierarchy().loadCreateClass(JAVA_LONG);
    			final ReferenceConcrete referenceLong = state.createInstance(calc, cf_JAVA_LONG);
    			final HeapObjekt longObject = state.getObject(referenceLong);
    			longObject.setFieldValue(JAVA_LONG_VALUE, val);
    			return referenceLong;
    		}
    	} else { //(type == SHORT)
    		final long valShort = ((Short) val.getActualValue()).shortValue();
    		if (valShort >= -128 && valShort <= 127) { //the cached values
        		final ClassFile cf_JAVA_SHORT_SHORTCACHE = state.getClassHierarchy().loadCreateClass(JAVA_SHORT_SHORTCACHE);
        		final Klass klassShortcache = state.getKlass(cf_JAVA_SHORT_SHORTCACHE);
        		final Reference referenceCache = (Reference) klassShortcache.getFieldValue(JAVA_SHORT_SHORTCACHE_CACHE);
        		final Array cache = (Array) state.getObject(referenceCache);
        		final Simplex index = calc.valInt(((int) valShort) + 128);
        		try {
    				final AccessOutcomeInValue outcome = (AccessOutcomeInValue) cache.getFast(calc, index);
    				return (Reference) outcome.getValue();
    			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
    				//this should never happen
    				throw new UnexpectedInternalException(e);
    			}
    		} else {
        		final ClassFile cf_JAVA_SHORT = state.getClassHierarchy().loadCreateClass(JAVA_SHORT);
    			final ReferenceConcrete referenceShort = state.createInstance(calc, cf_JAVA_SHORT);
    			final HeapObjekt shortObject = state.getObject(referenceShort);
    			shortObject.setFieldValue(JAVA_SHORT_VALUE, val);
    			return referenceShort;
    		}
    	}
    }
    
    public static void ensureCallSiteLinked(State state, Calculator calc, ClassFile caller, String callerDescriptor, String callerName, int callerProgramCounter, CallSiteSpecifier css) 
    throws ThreadStackEmptyException, InvalidInputException, HeapMemoryExhaustedException, ClassFileNotFoundException, 
    ClassFileIllFormedException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, PleaseLoadClassException, InterruptException, 
    ClasspathException {
        //fast track: the call site is already linked
        if (state.isCallSiteLinked(caller, callerDescriptor, callerName, callerProgramCounter)) {
            return;
        }
        
        //upcalls java.lang.invoke.MethodHandleNatives.linkCallSite
        //and stores the link, see hotspot:/src/share/vm/prims/systemDictionary.cpp 
        //lines 2557-2612        

        //prepares the parameters for the upcall:
        //1- the caller
        state.ensureInstance_JAVA_CLASS(calc, caller);
        final ReferenceConcrete rcaller = state.referenceToInstance_JAVA_CLASS(caller);
        
        //2- a MethodHandle to the bootstrap method
        final Signature bmSignature = css.getBootstrapMethodSignature();
        final int bmRefKind = ("<init>".equals(bmSignature.getName()) ? REF_newInvokeSpecial : REF_invokeStatic);
        final ClassFile bmContainer = state.getClassHierarchy().resolveClass(caller, bmSignature.getClassName(), state.bypassStandardLoading());
    	final ClassFile[] bmDescriptorResolved = state.getClassHierarchy().resolveMethodType(caller, bmSignature.getDescriptor(), state.bypassStandardLoading());
        ensureInstance_JAVA_METHODHANDLE(state, calc, caller, bmRefKind, bmContainer, bmDescriptorResolved, bmSignature.getName());
        final ReferenceConcrete rmhbootstrap = state.referenceToInstance_JAVA_METHODHANDLE(bmRefKind, bmContainer, bmDescriptorResolved, bmSignature.getName());
        
        //3- the name
        state.ensureStringLiteral(calc, css.getName());
        final ReferenceConcrete rname = state.referenceToStringLiteral(css.getName());
        
        //4- the type
    	final ClassFile[] dmDescriptorResolved = state.getClassHierarchy().resolveMethodType(caller, css.getDescriptor(), state.bypassStandardLoading());
        ensureInstance_JAVA_METHODTYPE(state, calc, dmDescriptorResolved);
        final ReferenceConcrete rtype = state.referenceToInstance_JAVA_METHODTYPE(dmDescriptorResolved);
        
        //5- the constant parameters to the bootstrap method
        for (ConstantPoolValue cpv : css.getBootstrapParameters()) {
        	if (cpv instanceof ConstantPoolClass) {
                final String classSignature = ((ConstantPoolClass) cpv).getValue();
                final ClassFile resolvedClass = state.getClassHierarchy().resolveClass(caller, classSignature, state.bypassStandardLoading());
                state.ensureInstance_JAVA_CLASS(calc, resolvedClass);
            } else if (cpv instanceof ConstantPoolMethodType) {
            	final String descriptor = ((ConstantPoolMethodType) cpv).getValue();
            	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(caller, descriptor, state.bypassStandardLoading());
            	ensureInstance_JAVA_METHODTYPE(state, calc, descriptorResolved);
        	} else if (cpv instanceof ConstantPoolMethodHandle) {
            	final ConstantPoolMethodHandle cpvMH = (ConstantPoolMethodHandle) cpv;
            	final int refKind = cpvMH.getKind();
            	final Signature sig = cpvMH.getValue();
            	final ClassFile callee = state.getClassHierarchy().resolveClass(caller, sig.getClassName(), state.bypassStandardLoading());
            	final String descriptor = sig.getDescriptor();
            	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(caller, descriptor, state.bypassStandardLoading());
            	ensureInstance_JAVA_METHODHANDLE(state, calc, caller, refKind, callee, descriptorResolved, sig.getName());
        	}
        }
        ClassFile cf_arrayOfJAVA_OBJECT = null; //to keep the compiler happy
        try {
        	cf_arrayOfJAVA_OBJECT = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND);
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
        		BadClassFileVersionException | WrongClassNameException | ClassFileNotAccessibleException e) {
        	//this should never happen
        	failExecution(e);
        }
        final int nBootParams = css.getBootstrapParameters().size();
        final ReferenceConcrete rbmparams = state.createArray(calc, null, calc.valInt(nBootParams), cf_arrayOfJAVA_OBJECT);
        final Array bmparams = (Array) state.getObject(rbmparams);
        for (int i = 0; i < nBootParams; ++i) {
        	final ConstantPoolValue cpv = css.getBootstrapParameters().get(i);
        	Reference val = null; //to keep the compiler happy
            if (cpv instanceof ConstantPoolPrimitive) {
                val = doPrimitiveBoxing(state, calc, calc.val_(cpv.getValue()));
            } else if (cpv instanceof ConstantPoolString) {
                final String stringLit = ((ConstantPoolString) cpv).getValue();
                state.ensureStringLiteral(calc, stringLit);
                val = state.referenceToStringLiteral(stringLit);
            } else if (cpv instanceof ConstantPoolClass) {
                final String classSignature = ((ConstantPoolClass) cpv).getValue();
                final ClassFile resolvedClass = state.getClassHierarchy().resolveClass(caller, classSignature, state.bypassStandardLoading());
                val = state.referenceToInstance_JAVA_CLASS(resolvedClass);
            } else if (cpv instanceof ConstantPoolMethodType) {
            	final String descriptor = ((ConstantPoolMethodType) cpv).getValue();
            	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(caller, descriptor, state.bypassStandardLoading());
            	val = state.referenceToInstance_JAVA_METHODTYPE(descriptorResolved);
            } else if (cpv instanceof ConstantPoolMethodHandle) {
            	final ConstantPoolMethodHandle cpvMH = (ConstantPoolMethodHandle) cpv;
            	final int refKind = cpvMH.getKind();
            	final Signature sig = cpvMH.getValue();
            	final ClassFile callee = state.getClassHierarchy().resolveClass(caller, sig.getClassName(), state.bypassStandardLoading());
            	final String descriptor = sig.getDescriptor();
            	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(caller, descriptor, state.bypassStandardLoading());
            	val = state.referenceToInstance_JAVA_METHODHANDLE(refKind, callee, descriptorResolved, sig.getName());
            } else {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            }
            try {
				bmparams.setFast(calc.valInt(i), val);
			} catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
				//this should never happen
				failExecution(e);
			}
        }
        
        //6- an array with length 1 to host the returned appendix (if any)
        final ReferenceConcrete appendixBox = state.createArray(calc, null, calc.valInt(1), cf_arrayOfJAVA_OBJECT);
        
    	//7- the method descriptor of the dynamic call site
    	final ReferenceConcrete rcallerDescriptor = state.createMetaLevelBox(calc, callerDescriptor);

    	//8- the method name of the dynamic call site
    	final ReferenceConcrete rcallerName = state.createMetaLevelBox(calc, callerName);

    	//9- the program counter of the dynamic call site
    	final ReferenceConcrete rcallerProgramCounter = state.createMetaLevelBox(calc, callerProgramCounter);

        //upcalls
        final Snippet snippet = state.snippetFactoryNoWrap()
        		.addArg(rcaller)
        		.addArg(rmhbootstrap)
        		.addArg(rname)
        		.addArg(rtype)
        		.addArg(rbmparams)
        		.addArg(appendixBox)
        		.addArg(rcallerDescriptor)
        		.addArg(rcallerName)
        		.addArg(rcallerProgramCounter)
    			//pushes the arguments for the call to JAVA_METHODHANDLENATIVES_LINKCALLSITE and upcalls
        		.op_aload((byte) 0)
        		.op_aload((byte) 1)
        		.op_aload((byte) 2)
        		.op_aload((byte) 3)
        		.op_iload((byte) 4)
        		.op_aload((byte) 5)
        		.op_invokestatic(JAVA_METHODHANDLENATIVES_LINKCALLSITE)
    			//pushes the additional arguments for the call to noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX and upcalls
        		.op_aload((byte) 5)
        		.op_aload((byte) 0)
        		.op_aload((byte) 6)
        		.op_aload((byte) 7)
        		.op_aload((byte) 8)
        		.op_invokestatic(noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX)
        		.op_return()
        		.mk();
        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
        try {
        	state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME);  //zero offset so that upon return from the snippet will reexecute the current algorithm
        } catch (InvalidProgramCounterException e) {
        	//this should never happen
        	failExecution(e);
        }
        exitFromAlgorithm();
    }
    
    /**
     * Ensures that an instance of {@code java.lang.invoke.MethodType} exists in 
     * a state for a resolved method or field descriptor.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param descriptorResolved a {@link ClassFile}{@code []}, the resolved method or field descriptor.
     * @throws InvalidInputException if {@code calc} is {@code null}, {@code descriptorResolved} is invalid, or {@code state} is frozen.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ClassFileNotFoundException if any class referred in {@code resolved} 
     *         does not exist.
     * @throws ClassFileIllFormedException if any class referred in {@code resolved} 
     *         is ill-formed.
     * @throws ClassFileNotAccessibleException if any class referred in {@code resolved} 
     *         is not accessible by {@code accessor}.
     * @throws IncompatibleClassFileException if the superclass of any class referred in {@code resolved}
     *         is resolved to an interface type, or a superinterface is resolved to an object type.
     * @throws BadClassFileVersionException if any class referred in {@code resolved}
     *         has a version number that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException if the bytecode of any class referred in {@code resolved}
     *         has a name that is different from what expected (the corresponding name in 
     *         {@code resolved}).
     * @throws ThreadStackEmptyException if the thread stack is empty.
     * @throws InterruptException if the execution of the invoking {@link Algorithm} must be interrupted.
     */
    public static void ensureInstance_JAVA_METHODTYPE(State state, Calculator calc, ClassFile[] descriptorResolved) 
    throws InvalidInputException, HeapMemoryExhaustedException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    BadClassFileVersionException, WrongClassNameException, ThreadStackEmptyException, 
    InterruptException {
    	//fast track: the MethodType already exists in the state's cache
    	if (state.hasInstance_JAVA_METHODTYPE(descriptorResolved)) {
    		return;
    	}

    	//in the case the state does not cache the MethodType
    	//we upcall java.lang.invoke.MethodHandleNatives.findMethodHandleType(Class, Class[]);
    	//(see hotspot:/src/share/vm/classfile/systemDictionary.cpp 
    	//lines 2419-2507, function SystemDictionary::find_method_handle_type)
    	//then upcalls the internal (pseudo)method noclass_REGISTERMETHODTYPE
    	//to store the returned method type in the state's cache

    	//prepares the parameters to upcalls: 

    	//1-the return type
    	final ClassFile returnType = descriptorResolved[descriptorResolved.length - 1];
    	state.ensureInstance_JAVA_CLASS(calc, returnType);
    	final ReferenceConcrete rtype = state.referenceToInstance_JAVA_CLASS(returnType);

    	//2-the parameter types
		ReferenceConcrete ptypes = null; //to keep the compiler happy
    	try {
    		final ClassFile cf_arrayOfJAVA_CLASS = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
    		ptypes = state.createArray(calc, null, calc.valInt(descriptorResolved.length - 1), cf_arrayOfJAVA_CLASS);
    		final Array ptypesArray = (Array) state.getObject(ptypes);
    		for (int i = 0; i < descriptorResolved.length - 1; ++i) {
    			final ClassFile parameterType = descriptorResolved[i];
    			state.ensureInstance_JAVA_CLASS(calc, parameterType);
    			ptypesArray.setFast(calc.valInt(i), state.referenceToInstance_JAVA_CLASS(parameterType));
    		}
    	} catch (FastArrayAccessNotAllowedException | InvalidTypeException | RenameUnsupportedException e) {
    		//this should never happen
    		failExecution(e);
    	}

    	//3-the descriptorResolved itself
    	final ReferenceConcrete descr = state.createMetaLevelBox(calc, descriptorResolved);

    	//upcalls
    	final Snippet snippet = state.snippetFactoryNoWrap()
    			.addArg(rtype)
    			.addArg(ptypes)
    			.addArg(descr)
    			//pushes the arguments for the call to JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE and upcalls
    			.op_aload((byte) 0)
    			.op_aload((byte) 1)
    			.op_invokestatic(JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE)
    			//upcalls JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING to populate the descriptor string
    			.op_dup()
    			.op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
    			.op_pop() //we care only of the side effect
    			//pushes the additional arguments for the call to noclass_REGISTERMETHODTYPE and upcalls
    			.op_aload((byte) 2)
    			.op_invokestatic(noclass_REGISTERMETHODTYPE)
    			.op_return()
    			.mk();
        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
    	try {
    		state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME); //zero offset so that upon return from the snippet will reexecute the invoking bytecode
    	} catch (InvalidProgramCounterException e) {
			//this should never happen
			failExecution(e);
    	}	
    	exitFromAlgorithm();
    }
    
    
    /**
     * Ensures that an instance of {@code java.lang.invoke.MethodHandle} exists in 
     * a state for a given key.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param caller a {@link ClassFile}, the caller that is requesting the {@code MethodHandle}.
     * @param refKind a {@code int}, representing the method handle behavior (see the JVM
     *        Specification v.8, Table 5.4.3.5-A).
     * @param container a {@link ClassFile}, representing the class containing the field
     *        or method. It must not be {@code null}.
     * @param descriptorResolved a {@link ClassFile}{@code []}, the resolved method or field descriptor.
     *        It must not be {@code null}.
     * @param name a {@link String}, the name of the method or field.  It must not be {@code null}.
     * @throws InvalidInputException if {@code calc} is {@code null}, a parameter is invalid (null) or 
     *         {@code state} is frozen.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ClassFileNotFoundException if any class referred in {@code resolved} 
     *         does not exist.
     * @throws ClassFileIllFormedException if any class referred in {@code resolved} 
     *         is ill-formed.
     * @throws ClassFileNotAccessibleException if any class referred in {@code resolved} 
     *         is not accessible by {@code accessor}.
     * @throws IncompatibleClassFileException if the superclass of any class referred in {@code resolved}
     *         is resolved to an interface type, or a superinterface is resolved to an object type.
     * @throws BadClassFileVersionException if any class referred in {@code resolved}
     *         has a version number that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException if the bytecode of any class referred in {@code resolved}
     *         has a name that is different from what expected (the corresponding name in 
     *         {@code resolved}).
     * @throws ThreadStackEmptyException if the thread stack is empty.
     * @throws InterruptException if the execution of the invoking {@link Algorithm} must be interrupted.
     */
    public static void ensureInstance_JAVA_METHODHANDLE(State state, Calculator calc, ClassFile caller, int refKind, ClassFile container, ClassFile[] descriptorResolved, String name) 
    throws InvalidInputException, HeapMemoryExhaustedException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, WrongClassNameException, ThreadStackEmptyException, InterruptException 
    {
    	//fast track: the MethodHandle already exists in the state's cache
    	if (state.hasInstance_JAVA_METHODHANDLE(refKind, container, descriptorResolved, name)) {
    		return;
    	}

    	//in the case the state does not cache the MethodHandle
    	//we upcall java.lang.invoke.MethodHandleNatives.linkMethodHandleConstant(Class, int, Class, String, Object);
    	//then upcalls the internal (pseudo)method noclass_REGISTERMETHODHANDLE
    	//to store the returned method handle in the state's cache
    	
    	//prepares the parameters to upcalls: 
    	
    	//1-the caller
    	state.ensureInstance_JAVA_CLASS(calc, caller);
    	final ReferenceConcrete rcaller = state.referenceToInstance_JAVA_CLASS(caller);
    	
    	//2-the refKind
    	final Primitive refKindPrimitive = calc.valInt(refKind);

    	//3-the callee
    	state.ensureInstance_JAVA_CLASS(calc, container);
    	final ReferenceConcrete rcallee = state.referenceToInstance_JAVA_CLASS(container);
    	
    	//4-the name
    	state.ensureStringLiteral(calc, name);
    	final ReferenceConcrete rname = state.referenceToStringLiteral(name);
    	
    	//5-the type
    	final ReferenceConcrete rtype;
    	if (methodHandleKindIsField(refKind)) {
    		final ClassFile fieldType = descriptorResolved[0];
        	state.ensureInstance_JAVA_CLASS(calc, fieldType);
        	rtype = state.referenceToInstance_JAVA_CLASS(fieldType);
    	} else {
    		ensureInstance_JAVA_METHODTYPE(state, calc, descriptorResolved);
    		rtype = state.referenceToInstance_JAVA_METHODTYPE(descriptorResolved);
    	}
    	
    	//6-the descriptorResolved itself
    	final ReferenceConcrete descr = state.createMetaLevelBox(calc, descriptorResolved);

    	//upcalls
        final Snippet snippet = state.snippetFactoryNoWrap()
        	.addArg(rcaller)
        	.addArg(refKindPrimitive)
        	.addArg(rcallee)
        	.addArg(rname)
        	.addArg(rtype)
        	.addArg(descr)
        	//pushes the arguments for the call to JAVA_METHODHANDLENATIVES_LINKMETHODHANDLECONSTANT and upcalls
			.op_aload((byte) 0)
			.op_aload((byte) 1)
			.op_aload((byte) 2)
			.op_aload((byte) 3)
			.op_aload((byte) 4)
            .op_invokestatic(JAVA_METHODHANDLENATIVES_LINKMETHODHANDLECONSTANT)
            //pushes the additional arguments for the call to noclass_REGISTERMETHODHANDLE and upcalls
			.op_aload((byte) 1)
			.op_aload((byte) 2)
			.op_aload((byte) 5)
			.op_aload((byte) 3)
            .op_invokestatic(noclass_REGISTERMETHODHANDLE)
            .op_return()
            .mk();
        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
        try {
        	state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME); //zero offset so that upon return from the snippet will reexecute the invoking bytecode 
    	} catch (InvalidProgramCounterException e) {
			//this should never happen
			failExecution(e);
    	}	
        exitFromAlgorithm();
    }
    
    private static boolean methodHandleKindIsField(int refKind) {
    	return (REF_getField <= refKind && refKind <= REF_putStatic);
    }
    
    
    public static String getDescriptorFromMemberName(State state, Reference memberNameReference) 
    throws InvalidInputException, ThreadStackEmptyException, InterruptException, UndefinedResultException {
    	//gets the MemberName
    	final Instance memberNameInstance = (Instance) state.getObject(memberNameReference);
    	if (memberNameInstance == null) {
    		throw new InvalidInputException("The memberNameReference parameter of getDescriptorFromMemberName method is a Null reference, or an unresolved symbolic reference, or an invalid concrete reference.");
    	}
    	
    	//gets the type field
        final Reference memberNameDescriptorReference = (Reference) memberNameInstance.getFieldValue(JAVA_MEMBERNAME_TYPE);
        if (memberNameDescriptorReference == null) {
            //TODO missing field: who is to blame?
            failExecution("Unexpected null value while accessing to the type field of MemberName memberNameReference (missing field).");
        }
    	final Instance memberNameDescriptorObject = (Instance) state.getObject(memberNameDescriptorReference);
    	if (memberNameDescriptorObject == null) {
    		throw new InvalidInputException("The type field of the memberNameReference parameter of getDescriptorFromMemberName method is a Null reference, or an unresolved symbolic reference, or an invalid concrete reference.");
    	}
    	
    	//gets the descriptor
        //From the source code of java.lang.invoke.MemberName the type field of a MemberName is either null 
        //(if the field is not initialized), or a MethodType (if the MemberName is a method call), or a
        //Class (if the MemberName is a field get/set or a type), or a String (all cases, if the field is 
        //initialized but not yet converted to a MethodType/Class), or an array of (arrays of) classes 
        //(only when MemberName is a method call, if the field is initialized but not yet converted to a 
        //MethodType). See also the code of MemberName.getMethodType() and MemberName.getFieldType(), that 
        //populate/normalize the type field. Apparently the assumptions of MethodHandles::resolve_MemberName 
        //is that the type field is either a method type, or a class, or a String, see 
        //hotspot:/src/share/vm/prims/methodHandles.cpp line 654 and the invoked MethodHandles::lookup_signature, 
        //line 392.
        final String memberNameDescriptor;
        if (JAVA_METHODTYPE.equals(memberNameDescriptorObject.getType().getClassName())) {
            memberNameDescriptor = getDescriptorFromMethodType(state, memberNameDescriptorReference);
        } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType().getClassName())) {
            //memberNameDescriptorObject is an Instance of java.lang.String:
            //gets its String value and puts it in memberNameDescriptor
            memberNameDescriptor = valueString(state, memberNameDescriptorObject);
        } else {
            //memberNameDescriptorObject is neither a MethodType nor a String:
            //just fails
            throw new UndefinedResultException("The MemberName self parameter to java.lang.invoke.MethodHandleNatives.resolve represents a method invocation, but self.type is neither a MethodType nor a String.");
        }
        
        return memberNameDescriptor;
    }
    /**
     * Gets the {@code methodDescriptor} field of a {@code java.lang.invoke.MethodType}, 
     * possibly populating the field if it is still {@code null}.
     * 
     * @param state a {@link State}.
     * @param methodTypeReference a {@link Reference}. It must refer an {@link Instance}
     *        of class {@code java.lang.invoke.MemberName}.
     * @return the {@link String} value of the {@code methodDescriptor} field
     *         of {@code methodType}.
     * @throws InvalidInputException if {@code state == null} or {@code methodTypeReference == null}
     *         or {@code methodTypeReference} is symbolic and unresolved, or {@code methodTypeReference} is
     *         concrete and pointing to an empty heap slot, ot {@code methodTypeReference} is the {@link jbse.val.Null Null}
     *         reference.
     * @throws ThreadStackEmptyException if the {@code state}'s stack is empty.
     * @throws InterruptException if the execution of this {@link Algorithm} must be interrupted.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    public static String getDescriptorFromMethodType(State state, Reference methodTypeReference) 
    throws InvalidInputException, ThreadStackEmptyException, InterruptException, FrozenStateException {
    	//gets the MethodType
    	final Instance methodTypeInstance = (Instance) state.getObject(methodTypeReference);
    	if (methodTypeInstance == null) {
    		throw new InvalidInputException("The methodTypeReference parameter of getDescriptorFromMethodType method is a Null reference, or an unresolved symbolic reference, or an invalid concrete reference.");
    	}
    	
        //gets the methodDescriptor field
        final Reference methodTypeDescriptorStringReference = (Reference) methodTypeInstance.getFieldValue(JAVA_METHODTYPE_METHODDESCRIPTOR);
        if (methodTypeDescriptorStringReference == null) {
            //TODO missing field: who is to blame?
            failExecution("Unexpected null value while accessing to MethodType self.type.methodDescriptor parameter to java.lang.invoke.MethodHandleNatives.resolve (missing field).");
        }

        //the methodDescriptor field of a MethodType is a cache: 
        //If it is null, invoke java.lang.invoke.MethodType.toMethodDescriptorString()
        //to fill it, and then repeat this bytecode
        if (state.isNull(methodTypeDescriptorStringReference)) {
            try {
                final Snippet snippet = state.snippetFactoryNoWrap()
                    .addArg(methodTypeReference)
                    .op_aload((byte) 0)
                    .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                    .op_pop() //we cannot use the return value so we need to clean the stack
                    .op_return()
                    .mk();
                final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
                state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve and reexecute this algorithm 
                exitFromAlgorithm();
            } catch (InvalidProgramCounterException | InvalidInputException e) {
                //this should never happen
                failExecution(e);
            }
        }

        //now the methodDescriptor field is not null: gets  
        //its String value
        return valueString(state, methodTypeDescriptorStringReference);
    }
    
    /**
     * Performs lookup of a method implementation (bytecode or native).
     * See JVMS v8, invokeinterface, invokespecial, invokestatic and invokevirtual
     * bytecodes specification.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param resolutionClass the {@link ClassFile} of the resolved method. It must not be {@code null}.
     * @param methodSignature the {@link Signature} of the method
     *        whose implementation must be looked up. The class name encapsulated 
     *        in it will be ignored and {@code resolutionClass} will be considered
     *        instead. It must not be {@code null}.
     * @param isInterface {@code true} iff the method is declared interface.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param isStatic {@code true} iff the method is declared static.
     * @param receiverClass a {@link ClassFile}, the class of the receiver
     *        of the method invocation. It can be {@code null} when 
     *        {@code isStatic == true || isSpecial == true}.
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
        return retVal;
    }

    /**
     * Determines whether a base-level or a meta-level overriding implementation 
     * for a method exists. In the base-level case returns the signature of the
     * overriding method. In the meta-level case interrupts the execution of the
     * current algorithm and triggers continuation with the one implementing
     * the method.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param ctx an {@link ExecutionContext}. It must not be {@code null}.
     * @param implementationClass the {@link ClassFile} where the method implementation is, 
     *        or {@code null} if the method is classless.
     * @param methodSignatureImplementation the {@link Signature} of the implementation.
     *        It must not be {@code null}.
     * @param isInterface {@code true} iff the method is declared interface.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param isStatic {@code true} iff the method is declared static.
     * @param isNative {@code true} iff the method is declared native.
     * @param doPop {@code true} iff in the case of a meta-level overriding
     *        implementation the topmost item on the operand stack must be 
     *        popped before transferring control (necessary to the implementation 
     *        of {@code MethodHandle.linkToXxxx} methods).
     * @return {@code null} if no overriding implementation exists, otherwise the
     *         {@link Signature} of a base-level overriding method.
     * @throws InvalidInputException if {@code state == null || ctx == null || 
     *         methodImplSignature == null}.
     * @throws MetaUnsupportedException if it is unable to find the specified {@link Algorithm}, 
     *         to load it, or to instantiate it for any reason (misses from the meta-level classpath, 
     *         has insufficient visibility, does not implement {@link Algorithm}...).
     * @throws ThreadStackEmptyException if {@code state}'s stack is empty.
     * @throws ClasspathException if a standard class is missing from the classpath.
     * @throws BaseUnsupportedException if the base-level overriding fails for some reason 
     *         (missing class, wrong classfile...).
     * @throws InterruptException if the execution fails or a meta-level implementation is found, 
     *         in which case the current {@link Algorithm} is interrupted with the 
     *         {@link Algorithm} for the overriding implementation as continuation. 
     * @throws InvalidNumberOfOperandsException if there are no operands on {@code state}'s operand stack
     *         and {@code doPop == true}.
     */
    public static Signature lookupMethodImplOverriding(State state, ExecutionContext ctx, ClassFile implementationClass, Signature methodSignatureImplementation, boolean isInterface, boolean isSpecial, boolean isStatic, boolean isNative, boolean doPop) 
    throws InvalidInputException, MetaUnsupportedException, InterruptException, ClasspathException, ThreadStackEmptyException, BaseUnsupportedException, InvalidNumberOfOperandsException {
        if (state == null || ctx == null || methodSignatureImplementation == null) {
            throw new InvalidInputException("Invoked " + Util.class.getName() + ".lookupMethodImplOverriding with a null parameter.");
        }
        if (ctx.isMethodBaseLevelOverridden(methodSignatureImplementation)) {
            try {
                final ClassHierarchy hier = state.getClassHierarchy();
                final Signature methodSignatureOverridingFromCtx = ctx.getBaseOverride(methodSignatureImplementation);
                final ClassFile classFileMethodOverridingFromCtx = hier.loadCreateClass(CLASSLOADER_APP, methodSignatureOverridingFromCtx.getClassName(), state.bypassStandardLoading());
                final ClassFile classFileMethodOverridingResolved = hier.resolveMethod(classFileMethodOverridingFromCtx, methodSignatureOverridingFromCtx, classFileMethodOverridingFromCtx.isInterface(), state.bypassStandardLoading()); //TODO is the isInterface parameter ok? And the accessor parameter?
                return new Signature(classFileMethodOverridingResolved.getClassName(), methodSignatureOverridingFromCtx.getDescriptor(), methodSignatureOverridingFromCtx.getName());
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException | 
                     RenameUnsupportedException | WrongClassNameException | ClassFileNotAccessibleException | 
                     IncompatibleClassFileException | MethodNotFoundException | MethodNotAccessibleException e) {
                throw new BaseUnsupportedException(e);
            }        
        } else {
            try {
                if (ctx.dispatcherMeta.isMeta(implementationClass, methodSignatureImplementation)) {
                    final Algo_INVOKEMETA<?, ?, ?, ?> algo = ctx.dispatcherMeta.select(methodSignatureImplementation);
                    algo.setFeatures(isInterface, isSpecial, isStatic, isNative, methodSignatureImplementation);
                    if (doPop) {
                    	state.popOperand();
                    }
                    continueWith(algo);
                }
            } catch (MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
            }
        }
        return null; //no overriding implementation
    }

    /**
     * Checks that the base-level overriding method returned by 
     * an invocation to {@link #lookupMethodImplOverriding}
     * can indeed override a method implementation.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param classFileMethodOverridden the {@link ClassFile} where the overridden method 
     *        implementation is, or {@code null} if the method is classless.
     * @param methodSignatureOverridden the {@link Signature} of the overridden 
     *        method implementation. It must not be {@code null}.
     * @param classFileMethodOverridingResolved the {@link ClassFile} where the overriding method implementation is, 
     *        or {@code null} if the method is classless.. It must not be {@code null}.
     * @param methodSignatureOverriding. It must not be {@code null}.
     * @throws BaseUnsupportedExceptionBaseUnsupportedException if the base-level overriding fails 
     *         because the overriding method is abstract or has a signature that is incompatible 
     *         with the one of the overridden method.
     */
    public static void checkOverridingMethodFits(State state, ClassFile classFileMethodOverridden, Signature methodSignatureOverridden, ClassFile classFileMethodOverriding, Signature methodSignatureOverriding) 
    throws BaseUnsupportedException, MethodNotFoundException {
        if (!classFileMethodOverriding.hasMethodImplementation(methodSignatureOverriding)) {
            throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " is abstract.");
        }
        final boolean overridingStatic;
        final boolean overriddenStatic;
        try {
            overridingStatic = classFileMethodOverriding.isMethodStatic(methodSignatureOverriding);
            overriddenStatic = (classFileMethodOverridden == null ? true : classFileMethodOverridden.isMethodStatic(methodSignatureOverridden));
        } catch (MethodNotFoundException e) {
            throw new BaseUnsupportedException(e);
        }
        if (overriddenStatic == overridingStatic) {
            if (methodSignatureOverridden.getDescriptor().equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else if (!overriddenStatic && overridingStatic) {
            if (descriptorAsStatic(methodSignatureOverridden).equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else { //(overriddenStatic && !overridingStatic)
            if (descriptorAsStatic(methodSignatureOverriding).equals(methodSignatureOverridden.getDescriptor())) {
                return;
            }
        }
        throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " has signature incompatible with overridden " + methodSignatureOverridden);
    }

    private static String descriptorAsStatic(Signature sig) {
        return "(" + REFERENCE + sig.getClassName() + TYPEEND + sig.getDescriptor().substring(1);
    }

	
    /**
     * Checks if a method name is the name of a static signature polymorphic
     * method.
     * 
     * @param methodName a {@link String}.
     * @return {@code true} if {@code methodName} is one of  
     *         {@code linkToInterface}, {@code linkToSpecial}, {@code linkToStatic},
     *         or {@code linkToVirtual}. 
     */
    public static boolean isSignaturePolymorphicMethodStatic(String methodName) {
        return 
        (JAVA_METHODHANDLE_LINKTOINTERFACE.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.getName().equals(methodName));
    }
	
    /**
     * Checks if a method name is the name of an intrinsic signature polymorphic
     * method.
     * 
     * @param methodName a {@link String}.
     * @return {@code true} if {@code methodName} is one of {@code invokeBasic}, 
     *         {@code linkToInterface}, {@code linkToSpecial}, {@code linkToStatic},
     *         or {@code linkToVirtual}. 
     */
    public static boolean isSignaturePolymorphicMethodIntrinsic(String methodName) {
        return 
        (JAVA_METHODHANDLE_INVOKEBASIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOINTERFACE.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.getName().equals(methodName));
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
     *         in {@code s} (also when {@code ref} is {@link Null}), or 
     *         if it refers an {@link Instance} but its 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or if its type is the 
     *         {@code java.lang.String} but its value field
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
        if (i == null) {
        	return null;
        }
        return valueString(s, i);
    }
    
    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param i an {@link Instance}. It must not be {@code null}. 
     * @return a {@link String} corresponding to the {@code value} of 
     *         the {@code i}, 
     *         or {@code null} if such {@link Instance}'s 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or its value
     *         is not a simple array of {@code char}s.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public static String valueString(State s, Instance i) throws FrozenStateException {
        final ClassFile cf_JAVA_STRING = s.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING); //surely loaded
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

    /**
     * Ensures that a {@link State} has a {@link Klass} in its 
     * static store for one or more classes, possibly creating the necessary
     * frames for the {@code <clinit>} methods to initialize them, 
     * or initializing them symbolically. If necessary it also recursively 
     * initializes their superclasses. It is equivalent
     * to {@link #ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
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
     * to {@link #ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
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
     * to {@link #ensureClassInitialized(State, ExecutionContext, Set, Signature, ClassFile...) ensureClassInitialized}
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
	            if (this.s.existsKlass(classFile) && this.s.getKlass(classFile).initializationStarted()) {
	            	continue;
	            }
	
	            //saves classFile in the list of the newly
	            //created Klasses
	            this.classesForPhase2.add(classFile);
	            
	            //decides whether the class is assumed pre-initialized and whether
	            //a symbolic or concrete Klass object should be created
	            //TODO here we assume mutual exclusion of the initialized/not initialized assumptions. Withdraw this assumption and branch.
	            final ClassHierarchy hier = this.s.getClassHierarchy();
	            final boolean klassAlreadyExists = this.s.existsKlass(classFile);
	            final boolean symbolicKlass;
	            boolean assumeInitialized = false; //bogus initialization to keep the compiler happy
	            //invariant: symbolicKlass implies assumeInitialized
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
	
	            //creates the Klass object
	            if (symbolicKlass) {
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
	                if (assumeInitialized) { 
	                    final Klass k = this.s.getKlass(classFile);
	                    this.s.assumeClassInitialized(classFile, k);
	                } else {
	                    this.s.assumeClassNotInitialized(classFile);
	                }
	            }
	
	            //if the created Klass is concrete but 
	            //the class is assumed to be pre-initialized, 
	            //schedules the Klass to become symbolic (if
	            //the corresponding flag is active)
	            if (!symbolicKlass && assumeInitialized && this.makePreInitClassesSymbolic
	            && !JBSE_BASE.equals(classFile.getClassName()) /* HACK */) {
	                this.preInitializedClasses.add(classFile);
	            }
	
	            //if classFile denotes a class rather than an interface
	            //and has a superclass, then recursively performs phase1 
	            //on its superclass and superinterfaces, according to
	            //JVMS v8 section 5.5, point 7
	            if (!classFile.isInterface() || recurSuperinterfaces) {
	                for (ClassFile superinterface : reverse(classFile.getSuperInterfaces())) {
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
                    	try {
                    		if (this.preInitializedClasses.contains(classFile)) {
                    			this.s.ensureStringLiteral(this.ctx.getCalculator(), classFile.getClassName());
                    			this.s.pushFrame(this.ctx.getCalculator(), this.cf_JBSE_BASE, JBSE_BASE_MAKEKLASSSYMBOLIC, root(), 0, this.ctx.getCalculator().valInt(classFile.getDefiningClassLoader()), this.s.referenceToStringLiteral(classFile.getClassName()));
                    			++this.createdFrames;
                    		}
                    		if (this.boxExceptionMethodSignature != null && exceptionBoxFrameYetToPush) {
                    			this.s.pushFrame(this.ctx.getCalculator(), this.cf_JBSE_BASE, this.boxExceptionMethodSignature, root(), 0);
                    			++this.createdFrames;
                    		}
                    		this.s.pushFrame(this.ctx.getCalculator(), classFile, sigClinit, root(), 0);
                    		++this.createdFrames;
                    	} catch (InvalidInputException e) {
                            //this should never happen
                            failExecution("Could not find the classfile for " + classFile.getClassName() + " or for jbse/base/Base.");
                    	}
                    }
                }
                if (this.pushClinitFor_JAVA_OBJECT) {
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
        	final Calculator calc = ctx.getCalculator();
            final Array array = (Array) state.getObject(arrayReference);
            if (array.hasSimpleRep() && index instanceof Simplex) {
                array.setFast((Simplex) index, valueToStore);
            } else {
                final Iterator<? extends Array.AccessOutcomeIn> entries = array.entriesPossiblyAffectedByAccess(calc, index, valueToStore);
                ctx.decisionProcedure.constrainArrayForSet(state.getClassHierarchy(), entries, index);
                array.set(calc, index, valueToStore);
            }
        } catch (InvalidInputException | InvalidTypeException | ClassCastException | 
                 FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    public static void invokeClassLoaderLoadClass(State state, Calculator calc, PleaseLoadClassException e) 
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
            state.ensureStringLiteral(calc, className);
            final ReferenceConcrete classNameReference = state.referenceToStringLiteral(className);

            //upcalls ClassLoader.loadClass
            //first, creates the snippet
            final Snippet snippet = state.snippetFactoryNoWrap()
                .op_invokevirtual(JAVA_CLASSLOADER_LOADCLASS) //loads the class...
                .op_invokestatic(noclass_REGISTERLOADEDCLASS) //...and registers it with the initiating loader
                .op_return()
                .mk();
	    	final ClassFile cf_JAVA_CLASSLOADER = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASSLOADER); //surely loaded
            state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_CLASSLOADER);
            //TODO if ClassLoader.loadClass finds no class we should either propagate the thrown ClassNotFoundException or wrap it inside a NoClassDefFoundError.
            //then, pushes the parameters for noclass_REGISTERLOADEDCLASS
            state.pushOperand(calc.valInt(initiatingLoader));
            //finally, pushes the parameters for JAVA_CLASSLOADER_LOADCLASS
            state.pushOperand(classLoaderReference);
            state.pushOperand(classNameReference);
        } catch (HeapMemoryExhaustedException exc) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
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
     * Returns the outcomes of an access to an array, performing transitive
     * access to backing arrays, creating fresh symbols if necessary, and
     * producing a result that can be passed to {@link DecisionProcedureAlgorithms}
     * methods.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param arrayRef a {@link Reference} to the array that is being accessed.
     *         It must not be {@code null}.
     * @param index a {@link Primitive}, the index of the array access.
     *         It must not be {@code null}.
     * @return a {@link List}{@code <}{@link ArrayAccessInfo}{@code >}.
     * @throws InvalidInputException if any parameter is {@code null}, or
     *         {@code state} is frozen.
     */
    public static List<ArrayAccessInfo> getFromArray(State state, Calculator calc, Reference arrayRef, Primitive index) 
    throws InvalidInputException {
        if (state == null || calc == null || arrayRef == null || index == null) {
            throw new InvalidInputException("Invoked getFromArray with a null parameter.");
        }
        final ArrayList<ArrayAccessInfo> retVal = new ArrayList<>();
        final LinkedList<Reference> refToArraysToProcess = new LinkedList<>();
        final LinkedList<Expression> accessConditions = new LinkedList<>();
        final LinkedList<Term> indicesFormal = new LinkedList<>();
        final LinkedList<Primitive> offsets = new LinkedList<>();
        refToArraysToProcess.add(arrayRef);
        accessConditions.add(null);
        indicesFormal.add(null);
        offsets.add(calc.valInt(0));
        while (!refToArraysToProcess.isEmpty()) {
            final Reference refToArrayToProcess = refToArraysToProcess.remove();
            final Primitive referringArrayAccessCondition = accessConditions.remove();
            final Term referringArrayIndexFormal = indicesFormal.remove();
            final Primitive referringArrayOffset = offsets.remove();
            Array arrayToProcess = null; //to keep the compiler happy
            try {
                arrayToProcess = (Array) state.getObject(refToArrayToProcess);
            } catch (ClassCastException exc) {
                //this should never happen
                failExecution(exc);
            }
            if (arrayToProcess == null) {
                //this should never happen
                failExecution("An initial array that backs another array is null.");
            }
            Collection<Array.AccessOutcome> entries = null; //to keep the compiler happy
            try {
                final Primitive indexPlusOffset = calc.push(index).add(referringArrayOffset).pop();
                entries = arrayToProcess.get(calc, indexPlusOffset);
            } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
                //this should never happen
                failExecution(e);
            }
            for (Array.AccessOutcome e : entries) {
                if (e instanceof Array.AccessOutcomeInInitialArray) {
                    final Array.AccessOutcomeInInitialArray eCast = (Array.AccessOutcomeInInitialArray) e;
                    refToArraysToProcess.add(eCast.getInitialArray());
                    accessConditions.add(e.getAccessCondition());
                    indicesFormal.add(arrayToProcess.getIndex());
                    offsets.add(eCast.getOffset());
                } else { 
                    //puts in val the value of the current entry, or a fresh symbol, 
                    //or null if the index is out of bounds
                    Value val;
                    boolean fresh = false;  //true iff val is a fresh symbol
                    if (e instanceof Array.AccessOutcomeInValue) {
                        val = ((Array.AccessOutcomeInValue) e).getValue();
                        if (val == null) {
                            try {
                                final ClassFile memberClass = arrayToProcess.getType().getMemberClass();
                                final String memberType = memberClass.getInternalTypeName(); 
                                final String memberGenericSignature = memberClass.getGenericSignatureType();
                                val = (Value) state.createSymbolMemberArray(memberType, memberGenericSignature, arrayToProcess.getOrigin(), calc.push(index).add(referringArrayOffset).pop());
                            } catch (InvalidOperandException | InvalidTypeException exc) {
                                //this should never happen
                                failExecution(exc);
                            }
                            fresh = true;
                        }
                    } else { //e instanceof Array.AccessOutcomeOut
                        val = null;
                    }

                    try {
                        final Expression accessCondition;
                        final Term indexFormal;
                        if (referringArrayAccessCondition == null) {
                            accessCondition = e.getAccessCondition();
                            indexFormal = arrayToProcess.getIndex();
                        } else {
                            final Primitive entryAccessConditionShifted = calc.push(e.getAccessCondition()).replace(arrayToProcess.getIndex(), calc.push(referringArrayIndexFormal).add(referringArrayOffset).pop()).pop();
                            accessCondition = (Expression) calc.push(referringArrayAccessCondition).and(entryAccessConditionShifted).pop();
                            indexFormal = referringArrayIndexFormal;
                        }
                        retVal.add(new ArrayAccessInfo(refToArrayToProcess, accessCondition, indexFormal, index, val, fresh));
                    } catch (InvalidOperandException | InvalidTypeException exc) {
                        //this should never happen
                        failExecution(exc);
                    }
                }
            }
        }

        return retVal;
    }
    
    /** 
     * Do not instantiate it!
     */
    private Util() { }
}
