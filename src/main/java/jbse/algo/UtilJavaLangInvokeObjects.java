package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Modifiers.methodHandleKindIsField;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKMETHODHANDLECONSTANT;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.noclass_REGISTERMETHODHANDLE;
import static jbse.bc.Signatures.noclass_REGISTERMETHODTYPE;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

/**
 * Class containing methods to create and manage objects from package 
 * {@code java.lang.invoke} (method handles, method types, member names).
 * 
 * @author Pietro Braione
 *
 */
public final class UtilJavaLangInvokeObjects {
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
    	
    /**
     * Gets a descriptor from a {@code java.lang.invoke.MemberName} by accessing
     * its {@code type} field. If this contains a {@code java.lang.invoke.MethodType},
     * obtains the descriptor by invoking 
     * {@link #getDescriptorFromMethodType(State, Reference) getDescriptorFromMethodType}.
     * 
     * @param state a {@link State}.
     * @param memberNameReference a {@link Reference}. It must refer an {@link Instance}
     *        of class {@code java.lang.invoke.MemberName}.
     * @return the {@link String} value corresponding to the descriptor that is stored in
     *         the {@code type} field of {@code memberNameReference}.
     * @throws InvalidInputException if {@code state == null} or {@code memberNameReference == null}
     *         or {@code methodTypeReference} is symbolic and unresolved, or {@code methodTypeReference} is
     *         concrete and pointing to an empty heap slot, ot {@code methodTypeReference} is the {@link jbse.val.Null Null}
     *         reference.
     * @throws ThreadStackEmptyException if the {@code state}'s stack is empty.
     * @throws InterruptException if the execution of the invoking {@link Algorithm} must be interrupted.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    public static String getDescriptorFromMemberName(State state, Reference memberNameReference) 
    throws InvalidInputException, ThreadStackEmptyException, InterruptException, UndefinedResultException {
    	if (state == null || memberNameReference == null) {
    		throw new InvalidInputException("Invoked " + UtilJavaLangInvokeObjects.class.getCanonicalName() + ".getDescriptorFromMemberName with null state or memberNameReference parameter");
    	}
    	
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
     *        of class {@code java.lang.invoke.MethodType}.
     * @return the {@link String} value of the {@code methodDescriptor} field
     *         of {@code methodTypeReference}.
     * @throws InvalidInputException if {@code state == null} or {@code methodTypeReference == null}
     *         or {@code methodTypeReference} is symbolic and unresolved, or {@code methodTypeReference} is
     *         concrete and pointing to an empty heap slot, ot {@code methodTypeReference} is the {@link jbse.val.Null Null}
     *         reference.
     * @throws ThreadStackEmptyException if the {@code state}'s stack is empty.
     * @throws InterruptException if the execution of the invoking {@link Algorithm} must be interrupted.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    public static String getDescriptorFromMethodType(State state, Reference methodTypeReference) 
    throws InvalidInputException, ThreadStackEmptyException, InterruptException, FrozenStateException {
    	if (state == null || methodTypeReference == null) {
    		throw new InvalidInputException("Invoked " + UtilJavaLangInvokeObjects.class.getCanonicalName() + ".getDescriptorFromMethodType with null state or methodTypeReference parameter");
    	}
    	
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
    
	/** Do not instantiate! */
	private UtilJavaLangInvokeObjects() {
		//nothing to do
	}
}
