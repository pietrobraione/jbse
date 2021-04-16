package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.UtilJavaLangInvokeObjects.ensureInstance_JAVA_METHODHANDLE;
import static jbse.algo.UtilJavaLangInvokeObjects.ensureInstance_JAVA_METHODTYPE;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Modifiers.REF_invokeStatic;
import static jbse.bc.Modifiers.REF_invokeVirtual;
import static jbse.bc.Modifiers.REF_newInvokeSpecial;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOOLEAN_FALSE;
import static jbse.bc.Signatures.JAVA_BOOLEAN_TRUE;
import static jbse.bc.Signatures.JAVA_BYTE_BYTECACHE;
import static jbse.bc.Signatures.JAVA_BYTE_BYTECACHE_CACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_CHARACTERCACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER_CHARACTERCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_CHARACTER_VALUE;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_HIGH;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE_LOW;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_LONGCACHE;
import static jbse.bc.Signatures.JAVA_LONG_LONGCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_GETTYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKCALLSITE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKMETHOD;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_SHORTCACHE;
import static jbse.bc.Signatures.JAVA_SHORT_SHORTCACHE_CACHE;
import static jbse.bc.Signatures.JAVA_SHORT_VALUE;
import static jbse.bc.Signatures.noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX;
import static jbse.bc.Signatures.noclass_STORELINKEDMETHODADAPTERANDAPPENDIX;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import jbse.bc.CallSiteSpecifier;
import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolMethodHandle;
import jbse.bc.ConstantPoolMethodType;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Class containing methods performing the linking of a signature polymorphic nonintrinsic method
 * or of a call site.
 * 
 * @author Pietro Braione
 *
 */
public final class UtilLinking {
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
     *         because a class referred in {@code polymorphicMethodSignatureSpecialized} must be loaded by a 
     *         user-defined classloader.
     * @throws ClassFileNotFoundException if the bytecode for any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} is not found in the classpath.
     * @throws ClassFileIllFormedException if the bytecode for any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} has a version number
     *         that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException if the name of any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} is wrong.
     * @throws IncompatibleClassFileException if the superclass of any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} is resolved to an 
     *         interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the classfile for any 
     *         class referred in {@code polymorphicMethodSignatureSpecialized} is not accessible by {@code accessor}.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty (should never happen).
     * @throws InterruptException always thrown at the end of the method, to interrupt 
     *         the execution of the invoking {@link Algorithm} and perform the upcall, 
     *         might be also thrown during the execution of this method.
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
    
    /**
     * Ensures that a dynamic call site is linked to an adapter/appendix. 
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param caller the {@link ClassFile} of the invoker method of the dynamic call site.
     * @param callerDescriptor a {@link String}, the descriptor of the invoker method of 
     *        the dynamic call site.
     * @param callerName a {@link String}, the name of the invoker method of 
     *        the dynamic call site.
     * @param callerProgramCounter an {@code int}, the program counter of the dynamic
     *        call site invocation. 
     * @param css the {@link CallSiteSpecifier} of the dynamic call site.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty (should never happen).
     * @throws InvalidInputException if an invalid input is used by some method call.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ClassFileNotFoundException  if the bytecode for any 
     *         class referred in {@code css} (essentially, in its bootstrap method)
     *         is not found in the classpath.
     * @throws ClassFileIllFormedException if the bytecode for any 
     *         class referred in {@code css} (essentially, in its bootstrap method) 
     *         is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for any 
     *         class referred in {@code css} (essentially, in its bootstrap method)
     *         has a version number that is unsupported by the current version of JBSE.
     * @throws RenameUnsupportedException if the bytecode for any 
     *         class referred in {@code css} (essentially, in its bootstrap method)
     *         does not support renaming.
     * @throws WrongClassNameException if the name for any 
     *         class referred in {@code css} (essentially, in its bootstrap method)
     *         is wrong.
     * @throws IncompatibleClassFileException if the superclass of any 
     *         class referred in {@code css} (essentially, in its bootstrap method) 
     *         is resolved to an interface type, or any superinterface is resolved to 
     *         an object type.
     * @throws ClassFileNotAccessibleException if the classfile for any 
     *         class referred in {@code css} (essentially, in its bootstrap method) 
     *         is not accessible by {@code caller}.
     * @throws PleaseLoadClassException if the execution of the invoking {@link Algorithm} must be interrupted 
     *         because a class referred in {@code css} (essentially, in its bootstrap method) 
     *         must be loaded by a user-defined classloader.
     * @throws InterruptException always thrown at the end of the method, to interrupt 
     *         the execution of the invoking {@link Algorithm} and perform the upcall, 
     *         might be also thrown during the execution of this method. 
     * @throws ClasspathException is some standard library class is not found on the classpath.
     */
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
    
    private static Reference doPrimitiveBoxing(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, HeapMemoryExhaustedException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
    	final char type = val.getType();
    	if (type == BOOLEAN) {
    		return doPrimitiveBoxingBoolean(state, val);
    	} else if (type == BYTE) {
    		return doPrimitiveBoxingByte(state, calc, val);
    	} else if (type == CHAR) {
    		return doPrimitiveBoxingChar(state, calc, val);
    	} else if (type == DOUBLE) {
    		return doPrimitiveBoxingDouble(state, calc, val);
    	} else if (type == INT) {
    		return doPrimitiveBoxingInt(state, calc, val);
    	} else if (type == FLOAT) {
    		return doPrimitiveBoxingFloat(state, calc, val);
    	} else if (type == LONG) {
    		return doPrimitiveBoxingLong(state, calc, val);
    	} else { //(type == SHORT)
    		return doPrimitiveBoxingShort(state, calc, val);
    	}
    }
    
    private static Reference doPrimitiveBoxingBoolean(State state, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException {
		final boolean valIsTrue = ((Boolean) val.getActualValue()).booleanValue();
		final ClassFile cf_JAVA_BOOLEAN = state.getClassHierarchy().loadCreateClass(JAVA_BOOLEAN);
		final Klass klassBoolean = state.getKlass(cf_JAVA_BOOLEAN);
		return (Reference) (valIsTrue ? klassBoolean.getFieldValue(JAVA_BOOLEAN_TRUE) : klassBoolean.getFieldValue(JAVA_BOOLEAN_FALSE));
    }
    
    private static Reference doPrimitiveBoxingByte(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException {
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
    }
    
    private static Reference doPrimitiveBoxingChar(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
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
    }

    private static Reference doPrimitiveBoxingDouble(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
		final ClassFile cf_JAVA_DOUBLE = state.getClassHierarchy().loadCreateClass(JAVA_DOUBLE);
		final ReferenceConcrete referenceDouble = state.createInstance(calc, cf_JAVA_DOUBLE);
		final HeapObjekt doubleObject = state.getObject(referenceDouble);
		doubleObject.setFieldValue(JAVA_DOUBLE_VALUE, val);
		return referenceDouble;
    }

    private static Reference doPrimitiveBoxingInt(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
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
    }

    private static Reference doPrimitiveBoxingFloat(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
		final ClassFile cf_JAVA_FLOAT = state.getClassHierarchy().loadCreateClass(JAVA_FLOAT);
		final ReferenceConcrete referenceFloat = state.createInstance(calc, cf_JAVA_FLOAT);
		final HeapObjekt floatObject = state.getObject(referenceFloat);
		floatObject.setFieldValue(JAVA_FLOAT_VALUE, val);
		return referenceFloat;
    }
    
    private static Reference doPrimitiveBoxingLong(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
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
    }
    
    private static Reference doPrimitiveBoxingShort(State state, Calculator calc, Simplex val) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, HeapMemoryExhaustedException {
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
    
	/** Do not instantiate! */
	private UtilLinking() {
		//nothing to do
	}
}
