package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.algo.meta.Util.CALLER_SENSITIVE;
import static jbse.algo.meta.Util.isConstructor;
import static jbse.algo.meta.Util.isField;
import static jbse.algo.meta.Util.isInvokeInterface;
import static jbse.algo.meta.Util.isMethod;
import static jbse.algo.meta.Util.isSetter;
import static jbse.algo.meta.Util.isSignaturePolymorphicMethodIntrinsic;
import static jbse.algo.meta.Util.JVM_RECOGNIZED_FIELD_MODIFIERS;
import static jbse.algo.meta.Util.JVM_RECOGNIZED_METHOD_MODIFIERS;
import static jbse.algo.meta.Util.IS_CONSTRUCTOR;
import static jbse.algo.meta.Util.IS_FIELD;
import static jbse.algo.meta.Util.IS_METHOD;
import static jbse.algo.meta.Util.REFERENCE_KIND_SHIFT;
import static jbse.algo.meta.Util.REF_getField;
import static jbse.algo.meta.Util.REF_getStatic;
import static jbse.algo.meta.Util.REF_invokeSpecial;
import static jbse.algo.meta.Util.REF_invokeStatic;
import static jbse.algo.meta.Util.REF_invokeVirtual;
import static jbse.algo.meta.Util.REF_putField;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FLAGS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_GETTYPE;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_LINKMETHOD;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.bc.Signatures.noclass_REGISTERMETHODTYPE;
import static jbse.bc.Signatures.noclass_STORELINKEDMETHODANDAPPENDIX;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;
import static jbse.common.Type.isVoid;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.invoke.MethodHandleNatives#resolve(java.lang.invoke.MemberName, Class)}.
 * 
 * @author Pietro Braione
 */
/* TODO it is unclear whether this method should return a new MemberName or modify the one
 * it receives; However, the only invoker of this method is safe w.r.t. this issue because
 * it creates a copy of the method handle to resolve, invokes this method, and then uses the
 * returned MemberName. Since both alternatives are ok for this kind of use, we opt for the
 * easiest one and modify + return the received MemberName. 
 */
public final class Algo_JAVA_METHODHANDLENATIVES_RESOLVE extends Algo_INVOKEMETA_Nonbranching {
    private Instance memberNameObject; //set by cookMore
    private ClassFile resolvedClass; //set by cookMore
    private Signature resolvedSignature; //set by cookMore
    private Signature polymorphicMethodSignature; //set by cookMore
    private boolean isMethod; //set by cookMore
    private boolean isSetter; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @FunctionalInterface
    private interface ErrorAction {
        void doIt(String s) throws InterruptException, SymbolicValueNotAllowedException, ClasspathException;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, InvalidInputException {
        final ErrorAction OK                                         = msg -> { };
        final ErrorAction FAIL_JBSE                                  = msg -> { throw new UnexpectedInternalException(msg); };
        final ErrorAction THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION      = msg -> { throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION); exitFromAlgorithm(); };
        final ErrorAction THROW_JAVA_INTERNAL_ERROR                  = msg -> { throwNew(state, INTERNAL_ERROR); exitFromAlgorithm(); };
        final ErrorAction THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION = msg -> { throw new SymbolicValueNotAllowedException(msg); };
 
        try {
            //gets the first parameter (the MemberName)
            this.memberNameObject = getInstance(state, this.data.operand(0), "MemberName self", FAIL_JBSE, THROW_JAVA_INTERNAL_ERROR, THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);

            //now we will get all the fields of the MemberName; in the case these fields
            //are null we throw IllegalArgumentException as Hotspot does, see 
            //hotspot:/src/share/vm/prims/methodHandles.cpp line 1127 (C++ method MHN_resolve_Mem, 
            //the native implementation of java.lang.invoke.MethodHandleNatives.resolve) and line
            //589 (C++ method MethodHandles::resolve_MemberName, invoked by the former, does the 
            //heavy lifting of resolution).
            
            //gets the container class of the MemberName
            final Instance_JAVA_CLASS memberNameContainerClassObject = 
                (Instance_JAVA_CLASS) getInstance(state, this.memberNameObject.getFieldValue(JAVA_MEMBERNAME_CLAZZ), "Class self.clazz", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final ClassFile memberNameContainerClass = memberNameContainerClassObject.representedClass();

            //gets the descriptor of the MemberName (field type)
            final Instance memberNameDescriptorObject = getInstance(state, this.memberNameObject.getFieldValue(JAVA_MEMBERNAME_TYPE), "Object self.type", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
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

            //gets the field name of the MemberName
            final Instance memberNameNameObject = getInstance(state, this.memberNameObject.getFieldValue(JAVA_MEMBERNAME_NAME), "String self.name", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final String memberNameName = valueString(state, memberNameNameObject);
            if (memberNameName == null) {
                //TODO who is to blame?
                failExecution("Unexpected null value while accessing to String self.name parameter to java.lang.invoke.MethodHandleNatives.resolve (nonconcrete string or missing field).");
            }

            //gets the field flags of the MemberName
            final int memberNameFlags = ((Integer) ((Simplex) this.memberNameObject.getFieldValue(JAVA_MEMBERNAME_FLAGS)).getActualValue()).intValue();

            //gets the second parameter (the Class of the member accessor)
            final Instance_JAVA_CLASS accessorClassInstance = (Instance_JAVA_CLASS) getInstance(state, this.data.operand(1), "Class caller", FAIL_JBSE, OK, THROW_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final ClassFile accessorClass = (accessorClassInstance == null ? memberNameContainerClass : accessorClassInstance.representedClass());
            
            //performs resolution based on memberNameFlags
            if (isMethod(memberNameFlags) || isConstructor(memberNameFlags)) {
                this.isMethod = true;
                
                //memberNameDescriptorObject is an Instance of java.lang.invoke.MethodType
                //or of java.lang.String
                
                //gets the descriptor
                final String memberNameDescriptor;
                if (JAVA_METHODTYPE.equals(memberNameDescriptorObject.getType().getClassName())) {
                    memberNameDescriptor = getDescriptorFromMethodType(state, memberNameDescriptorObject);
                } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType().getClassName())) {
                    //memberNameDescriptorObject is an Instance of java.lang.String:
                    //gets its String value and puts it in memberNameDescriptor
                    memberNameDescriptor = valueString(state, memberNameDescriptorObject);
                } else {
                    //memberNameDescriptorObject is neither a MethodType nor a String:
                    //just fails
                    throw new UndefinedResultException("The MemberName self parameter to java.lang.invoke.MethodHandleNatives.resolve represents a method invocation, but self.type is neither a MethodType nor a String.");
                }
                if (memberNameDescriptor == null) {
                    //TODO who is to blame?
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }

                //builds the signature of the method to resolve
                final Signature methodToResolve = new Signature(memberNameContainerClass.getClassName(), memberNameDescriptor, memberNameName);

                //performs resolution
                final boolean isInterface = isInvokeInterface(memberNameFlags);
                this.resolvedClass = state.getClassHierarchy().resolveMethod(accessorClass, methodToResolve, isInterface, state.bypassStandardLoading());
                
                final boolean methodIsSignaturePolymorphic = !isInterface && this.resolvedClass.hasOneSignaturePolymorphicMethodDeclaration(methodToResolve.getName());
                final boolean methodIsSignaturePolymorphicNonIntrinsic = methodIsSignaturePolymorphic && !isSignaturePolymorphicMethodIntrinsic(methodToResolve.getName());
                if (methodIsSignaturePolymorphicNonIntrinsic) {
                    this.polymorphicMethodSignature = new Signature(this.resolvedClass.getClassName(), SIGNATURE_POLYMORPHIC_DESCRIPTOR, methodToResolve.getName());
                    
                    //links it, if it is the case
                    linkMethod(state, accessorClass, methodToResolve.getDescriptor());
                
                    //if the method has an appendix throws an error, 
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp, 
                    //lines 687-692 
                    if (state.getAppendix(this.polymorphicMethodSignature) != null) {
                        throwNew(state, INTERNAL_ERROR);
                        exitFromAlgorithm();
                    }
                    
                    //returns the adapter instead of the resolved method
                    //TODO is it correct?
                    final Instance invoker = getInstance(state, state.getAdapter(this.polymorphicMethodSignature), "invoker for the (signature polymorphic) MemberName self", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
                    final String invokerName = valueString(state, (Reference) invoker.getFieldValue(JAVA_MEMBERNAME_NAME));
                    final Instance invokerMethodType = (Instance) state.getObject((Reference) invoker.getFieldValue(JAVA_MEMBERNAME_TYPE));
                    final String invokerDescriptor = getDescriptorFromMethodType(state, invokerMethodType);
                    final Reference invokerClassRef = (Reference) invoker.getFieldValue(JAVA_MEMBERNAME_CLAZZ);
                    final ClassFile invokerClass = ((Instance_JAVA_CLASS) state.getObject(invokerClassRef)).representedClass();
                    this.resolvedClass = invokerClass;
                    this.resolvedSignature = new Signature(this.resolvedClass.getClassName(), invokerDescriptor, invokerName);
                    this.polymorphicMethodSignature = this.resolvedSignature; //TODO is the adapter always nonpolymorphic?
                } else {
                    this.resolvedSignature = new Signature(this.resolvedClass.getClassName(), methodToResolve.getDescriptor(), methodToResolve.getName());
                    this.polymorphicMethodSignature = (methodIsSignaturePolymorphic ?
                                                       new Signature(this.resolvedClass.getClassName(), SIGNATURE_POLYMORPHIC_DESCRIPTOR, methodToResolve.getName()) :
                                                       this.resolvedSignature);
                }
            } else if (isField(memberNameFlags)) {
                this.isMethod = false;
                this.isSetter = isSetter(memberNameFlags); 
                //memberNameDescriptorObject is an Instance of java.lang.Class
                //or of java.lang.String
                
                //gets the type of the MemberName as a string
                final String memberNameType;
                if (JAVA_CLASS.equals(memberNameDescriptorObject.getType().getClassName())) {
                    //memberNameDescriptorObject is an Instance of java.lang.Class:
                    //gets the name of the represented class and puts it in memberNameType
                    memberNameType = ((Instance_JAVA_CLASS) memberNameDescriptorObject).representedClass().getClassName();
                } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType().getClassName())) {
                    //memberNameDescriptorObject is an Instance of java.lang.String:
                    //gets its String value and puts it in memberNameDescriptor
                    memberNameType = valueString(state, memberNameDescriptorObject);
                } else {
                    //memberNameDescriptorObject is neither a Class nor a String:
                    //just fails
                    throw new UndefinedResultException("The MemberName self parameter to java.lang.invoke.MethodHandleNatives.resolve represents a field access, but self.type is neither a Class nor a String.");
                }
                if (memberNameType == null) {
                    //TODO who is to blame?
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }

                //builds the signature of the field to resolve
                final Signature fieldToResolve = new Signature(memberNameContainerClass.getClassName(), memberNameType, memberNameName);

                //performs resolution
                this.resolvedClass = state.getClassHierarchy().resolveField(accessorClass, fieldToResolve, state.bypassStandardLoading());
                this.resolvedSignature = new Signature(this.resolvedClass.getClassName(), fieldToResolve.getDescriptor(), fieldToResolve.getName());
            } else { //the member name is a type declaration, or the flags field is ill-formed
                //see hotspot:/src/share/vm/prims/methodHandles.cpp lines 658-730
                throwNew(state, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, e);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassFileIllFormedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadClassFileVersionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WrongClassNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleClassFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MethodNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MethodNotAccessibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FieldNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassFileNotAccessibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FieldNotAccessibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            //TODO is it ok?
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
    
    private static Instance getInstance(State state, Value ref, String paramName, 
                                        ErrorAction whenNoRef, ErrorAction whenNull, ErrorAction whenUnresolved)
    throws InterruptException, SymbolicValueNotAllowedException, ClasspathException, FrozenStateException {
        if (ref == null) {
            whenNoRef.doIt("Unexpected null value while accessing " + paramName + ".");
            return null;
        }
        final Reference theReference = (Reference) ref;
        if (state.isNull(theReference)) {
            whenNull.doIt("The " + paramName + " parameter to java.lang.invoke.MethodHandleNatives.resolve was null.");
            return null;
        }
        final Instance theInstance = (Instance) state.getObject(theReference);
        if (theInstance == null) {
            whenUnresolved.doIt("The " + paramName + " parameter to java.lang.invoke.MethodHandleNatives.resolve was an unresolved symbolic reference on the operand stack.");
        }
        return theInstance;
    }
    
    /**
     * Gets the {@code methodDescriptor} field from an {@link Instance} of
     * {@code java.lang.invoke.MethodType}, possibly populating the field if
     * it is still {@code null}.
     * 
     * @param state a {@link State}.
     * @param methodType an {@link Instance}. It must be of class
     *        {@code java.lang.invoke.MethodType}.
     * @return the {@link String} value of the {@code methodDescriptor} field
     *         of {@code methodType}.
     * @throws ThreadStackEmptyException if the {@code state}'s stack is empty.
     * @throws InterruptException if the execution of this {@link Algorithm} must be interrupted.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    private String getDescriptorFromMethodType(State state, Instance methodType) 
    throws ThreadStackEmptyException, InterruptException, FrozenStateException {
        //gets the methodDescriptor field
        final Reference memberNameDescriptorStringReference = (Reference) methodType.getFieldValue(JAVA_METHODTYPE_METHODDESCRIPTOR);
        if (memberNameDescriptorStringReference == null) {
            //TODO missing field: who is to blame?
            failExecution("Unexpected null value while accessing to MethodType self.type.methodDescriptor parameter to java.lang.invoke.MethodHandleNatives.resolve (missing field).");
        }

        //the methodDescriptor field of a MethodType is a cache: 
        //If it is null, invoke java.lang.invoke.MethodType.toMethodDescriptorString()
        //to fill it, and then repeat this bytecode
        if (state.isNull(memberNameDescriptorStringReference)) {
            try {
                state.pushOperand(this.memberNameObject.getFieldValue(JAVA_MEMBERNAME_TYPE));
                final Snippet snippet = state.snippetFactoryWrap()
                    .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                    .op_pop() //we cannot use the return value so we need to clean the stack
                    .op_return()
                    .mk();
                state.pushSnippetFrameWrap(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve and reexecute this bytecode 
                exitFromAlgorithm();
            } catch (InvalidProgramCounterException | InvalidInputException e) {
                //this should never happen
                failExecution(e);
            }
        }

        //the methodDescriptor field is not null: gets  
        //its String value and puts it in memberNameDescriptor
        return valueString(state, memberNameDescriptorStringReference);
    }
    
    private ClassFile resolveTypeNameReturn(State state, ClassFile accessor, String returnTypeName) 
    throws InvalidInputException, ClassFileNotFoundException, IncompatibleClassFileException, 
    ClassFileIllFormedException, BadClassFileVersionException, WrongClassNameException, 
    ClassFileNotAccessibleException, PleaseLoadClassException {
        final ClassFile retVal;
        if (isPrimitive(returnTypeName) || isVoid(returnTypeName)) {
            retVal = state.getClassHierarchy().getClassFilePrimitiveOrVoid(toPrimitiveOrVoidCanonicalName(returnTypeName));
        } else if (isArray(returnTypeName) || isReference(returnTypeName)) {
            retVal = state.getClassHierarchy().resolveClass(accessor, className(returnTypeName), state.bypassStandardLoading());
        } else {
            throw new InvalidInputException("Wrong return type name " + returnTypeName + ".");
        }
        return retVal;
    }
    
    private ClassFile resolveTypeNameParameter(State state, ClassFile accessor, String parameterTypeName) 
    throws InvalidInputException, ClassFileNotFoundException, IncompatibleClassFileException, 
    ClassFileIllFormedException, BadClassFileVersionException, WrongClassNameException, 
    ClassFileNotAccessibleException, PleaseLoadClassException {
        final ClassFile retVal;
        if (isPrimitive(parameterTypeName)) {
            retVal = state.getClassHierarchy().getClassFilePrimitiveOrVoid(toPrimitiveOrVoidCanonicalName(parameterTypeName));
        } else if (isArray(parameterTypeName) || isReference(parameterTypeName)) {
            retVal = state.getClassHierarchy().resolveClass(accessor, className(parameterTypeName), state.bypassStandardLoading());
        } else {
            throw new InvalidInputException("Wrong parameter type name " + parameterTypeName + ".");
        }
        return retVal;
    }
    
    /**
     * Finds a {@code java.lang.invoke.MethodType} for a method
     * descriptor.
     * 
     * @param state a {@link State}.
     * @param accessor a {@link ClassFile}, the accessor class.
     * @param descriptor a {@link String}, the method descriptor.
     * @return a {@link ReferenceConcrete} to an {@link Instance} of 
     *         {@code java.lang.invoke.MethodType} for {@code descriptor}.
     * @throws PleaseLoadClassException if the execution of this {@link Algorithm} must be interrupted 
     *         because a class referred in {@code resolved} must be loaded by a user-defined classloader.
     * @throws ClassFileNotFoundException if any class referred in {@code resolved} 
     *         does not exist.
     * @throws ClassFileIllFormedException if any class referred in {@code resolved} 
     *         is ill-formed.
     * @throws BadClassFileVersionException if any class referred in {@code resolved}
     *         has a version number that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException if the bytecode of any class referred in {@code resolved}
     *         has a name that is different from what expected (the corresponding name in 
     *         {@code resolved}).
     * @throws IncompatibleClassFileException if the superclass of any class referred in {@code resolved}
     *         is resolved to an interface type, or a superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if any class referred in {@code resolved} 
     *         is not accessible by {@code accessor}.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws InterruptException if the execution of this {@link Algorithm} must be interrupted.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    private ReferenceConcrete findMethodType(State state, ClassFile accessor, String descriptor) 
    throws PleaseLoadClassException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, HeapMemoryExhaustedException, InterruptException, ThreadStackEmptyException {
        //fast track: the MethodType already exists in the state's cache
        if (state.hasInstance_JAVA_METHODTYPE(descriptor)) {
            return state.referenceToInstance_JAVA_METHODTYPE(descriptor);
        }
        
        //in the case the state does not cache the MethodType
        //we upcall java.lang.invoke.MethodHandleNatives.findMethodHandleType,
        //see hotspot:/src/share/vm/classfile/systemDictionary.cpp 
        //lines 2419-2507, function SystemDictionary::find_method_handle_type;
        //then upcalls the internal (pseudo)method noclass_REGISTERMETHODTYPE
        //to store the returned method handle in the state's cache
        try {
            //prepares the parameters to upcalls: 
            
            //1-the return type
            final String returnTypeName = splitReturnValueDescriptor(descriptor);
            final ClassFile returnType = resolveTypeNameReturn(state, accessor, returnTypeName);
            state.ensureInstance_JAVA_CLASS(returnType);
            final ReferenceConcrete rtype = state.referenceToInstance_JAVA_CLASS(returnType);
            
            //2-the parameter types
            final String[] parameterTypeNames = splitParametersDescriptors(descriptor);
            final ClassFile cf_arrayOfJAVA_CLASS = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
            final ReferenceConcrete ptypes = state.createArray(null, state.getCalculator().valInt(parameterTypeNames.length), cf_arrayOfJAVA_CLASS);
            final Array ptypesArray = (Array) state.getObject(ptypes);
            int i = 0;
            for (String parameterTypeName : parameterTypeNames) {
                final ClassFile parameterType = resolveTypeNameParameter(state, accessor, parameterTypeName);
                state.ensureInstance_JAVA_CLASS(parameterType);
                ptypesArray.setFast(state.getCalculator().valInt(i), state.referenceToInstance_JAVA_CLASS(parameterType));
                ++i;
            }
            
            //3-the descriptor itself
            state.ensureStringLiteral(descriptor);
            final ReferenceConcrete descr = state.referenceToStringLiteral(descriptor);

            //upcalls
            //parameters for noclass_REGISTERMETHODTYPE
            state.pushOperand(descr);
            //parameters for JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE
            state.pushOperand(rtype);
            state.pushOperand(ptypes);
            final Snippet snippet = state.snippetFactoryWrap()
                .op_invokestatic(JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE)
                //let's populate the descriptor now
                .op_dup()
                .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                .op_pop() //we care only of the side effect
                //finally we register the method type
                .op_invokestatic(noclass_REGISTERMETHODTYPE)
                .op_return()
                .mk();
            state.pushSnippetFrameWrap(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve and reexecute this bytecode 
            exitFromAlgorithm();
        } catch (InvalidInputException | InvalidTypeException | FastArrayAccessNotAllowedException | 
                 InvalidOperandException | InvalidProgramCounterException e) {
            //this should never happen
            failExecution(e);
        }
        
        return null; //unreachable, to keep the compiler happy
    }
    
    /**
     * Links a method in the case this is a signature polymorphic nonintrinsic method. 
     * This is done by upcalling {@code java.lang.invoke.MethodHandleNatives.linkMethod} 
     * (see hotspot:/src/share/vm/prims/systemDictionary.cpp, lines 2377-2394). Finally, 
     * stores the link to the accessor invoker and the appendix in the {@link State}.
     * 
     * @param state a {@link State}.
     * @param accessor a {@link ClassFile}, the accessor class invoking {@code this.resolved}.
     * @param polymorphicMethodDescriptor a {@link String}, the descriptor of the signature-polymorphic method as declared
     *        in the member name.
     * @throws PleaseLoadClassException if the execution of this {@link Algorithm} must be interrupted 
     *         because a class referred in {@code resolved} must be loaded by a user-defined classloader.
     * @throws ClassFileNotFoundException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} is not found in the classpath.
     * @throws ClassFileIllFormedException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for any 
     *         class referred in {@code polymorphicMethodDescriptor} has a version number
     *         that is unsupported by the current version of JBSE.
     * @throws WrongClassNameException 
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
    private void linkMethod(State state, ClassFile accessor, String polymorphicMethodDescriptor) 
    throws PleaseLoadClassException, ClassFileNotFoundException, ClassFileIllFormedException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, HeapMemoryExhaustedException, 
    ThreadStackEmptyException, InterruptException, InvalidInputException, BadClassFileVersionException, WrongClassNameException {
        if (state.isMethodLinked(this.polymorphicMethodSignature)) {
            //already linked
            return;
        }
        final String polymorphicMethodName = this.polymorphicMethodSignature.getName();
        
        try {
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
            state.ensureInstance_JAVA_CLASS(cf_JAVA_METHODHANDLE);
            final ReferenceConcrete mhClassRef = state.referenceToInstance_JAVA_CLASS(cf_JAVA_METHODHANDLE);

            //2- the name of the resolved method 
            state.ensureStringLiteral(polymorphicMethodName);
            final ReferenceConcrete mhNameRef = state.referenceToStringLiteral(polymorphicMethodName);

            //3- a java.lang.invoke.MethodType for its descriptor
            final ReferenceConcrete mtRef = findMethodType(state, accessor, polymorphicMethodDescriptor);

            //4- an array with length 1 to host the returned appendix (if any)
            ClassFile cf_arrayOfJAVA_OBJECT = null; //to keep the compiler happy
            try {
                cf_arrayOfJAVA_OBJECT = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND);
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                    BadClassFileVersionException | WrongClassNameException | ClassFileNotAccessibleException e) {
                //this should never happen
                failExecution(e);
            }
            final ReferenceConcrete appendixBox = state.createArray(null, state.getCalculator().valInt(1), cf_arrayOfJAVA_OBJECT);

            //upcalls
            //parameters for the upcall to noclass_STORELINKEDMETHODANDAPPENDIX
            state.pushOperand(mhNameRef); //name of the method, either invoke or invokeExact
            state.pushOperand(mtRef); //java.lang.invoke.MethodType instance for the method's descriptor
            state.pushOperand(appendixBox); //appendix
            //parameters for the upcall to JAVA_METHODHANDLENATIVES_LINKMETHOD                    
            state.pushOperand(this.data.operand(1)); //accessor class
            state.pushOperand(state.getCalculator().valInt(REF_invokeVirtual)); //kind (MUST be REF_invokeVirtual)
            state.pushOperand(mhClassRef); //class where the method is defined (MUST be java.lang.invoke.MethodHandle)
            state.pushOperand(mhNameRef); //name of the method, either invoke or invokeExact
            state.pushOperand(mtRef); //java.lang.invoke.MethodType instance for the method's descriptor
            state.pushOperand(appendixBox); //appendix
            final Snippet snippet = state.snippetFactoryWrap()
                .op_invokestatic(JAVA_METHODHANDLENATIVES_LINKMETHOD)
                //the next call to getType ensures that the returned MemberName 
                //is normalized, so that its type field is a MethodType
                .op_dup()
                .op_invokevirtual(JAVA_MEMBERNAME_GETTYPE)
                .op_pop() //we care only of the side effect
                //stores the linked method and the appendix in the state
                .op_invokestatic(noclass_STORELINKEDMETHODANDAPPENDIX)
                .op_return()
                .mk();
            state.pushSnippetFrameWrap(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve, thus reexecute this bytecode 
            exitFromAlgorithm();
        } catch (InvalidTypeException | InvalidProgramCounterException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                //updates the MemberName: first, sets the clazz field...
                state.ensureInstance_JAVA_CLASS(this.resolvedClass);
                this.memberNameObject.setFieldValue(JAVA_MEMBERNAME_CLAZZ, state.referenceToInstance_JAVA_CLASS(this.resolvedClass));

                //...then sets the flags field
                final String resolvedName = this.resolvedSignature.getName();
                int flags;
                if (this.isMethod) {
                    //determines the flags based on the kind of invocation, 
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp line 176 
                    //method MethodHandles::init_method_MemberName; note
                    //that it is always the case that info.call_kind() == CallInfo::direct_call, see
                    //hotspot:/src/share/vm/interpreter/linkResolver.cpp line 88, method
                    //CallInfo::set_handle
                    flags = (short) (((short) this.resolvedClass.getMethodModifiers(this.polymorphicMethodSignature)) & JVM_RECOGNIZED_METHOD_MODIFIERS);
                    if (this.resolvedClass.isMethodStatic(this.polymorphicMethodSignature)) {
                        flags |= IS_METHOD      | (REF_invokeStatic  << REFERENCE_KIND_SHIFT);
                    } else if ("<init>".equals(resolvedName) || "<clinit>".equals(resolvedName)) {
                        flags |= IS_CONSTRUCTOR | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
                    } else {
                        flags |= IS_METHOD      | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
                    }
                    if (this.resolvedClass.isMethodCallerSensitive(this.polymorphicMethodSignature)) {
                        flags |= CALLER_SENSITIVE;
                    }
                } else {
                    //update the MemberName with field information,
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp line 276 
                    //method MethodHandles::init_field_MemberName
                    flags = (short) (((short) this.resolvedClass.getFieldModifiers(this.resolvedSignature)) & JVM_RECOGNIZED_FIELD_MODIFIERS);
                    flags |= IS_FIELD | ((this.resolvedClass.isFieldStatic(this.resolvedSignature) ? REF_getStatic : REF_getField) << REFERENCE_KIND_SHIFT);
                    if (this.isSetter) {
                        flags += ((REF_putField - REF_getField) << REFERENCE_KIND_SHIFT);
                    }
                }
                this.memberNameObject.setFieldValue(JAVA_MEMBERNAME_FLAGS, state.getCalculator().valInt(flags));
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (MethodNotFoundException | FieldNotFoundException e) {
                //this should never happen
                failExecution(e);
            }

            //pushes the reference to the MemberName (same as input)
            state.pushOperand(this.data.operand(0));
        };
    }
}
