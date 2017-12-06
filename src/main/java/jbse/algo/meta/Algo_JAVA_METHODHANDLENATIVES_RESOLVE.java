package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.algo.meta.Util.isSignaturePolymorphicMethodIntrinsic;
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
import static jbse.bc.Signatures.noclass_REGISTERMETHODTYPE;
import static jbse.bc.Signatures.noclass_STORELINKEDMETHODANDAPPENDIX;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
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
    //taken from java.lang.invoke.MethodHandleNatives.Constants
    private static final int  IS_METHOD            = 0x00010000; // method (not constructor)
    private static final int  IS_CONSTRUCTOR       = 0x00020000; // constructor
    private static final int  IS_FIELD             = 0x00040000; // field
    private static final int  REFERENCE_KIND_SHIFT = 24; // refKind
    private static final int  REFERENCE_KIND_MASK  = 0x0F000000 >> REFERENCE_KIND_SHIFT;
    private static final byte REF_invokeVirtual    = 5;
    private static final byte REF_invokeInterface  = 9;

    private Instance memberNameObject; //set by cookMore
    private Signature resolved; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @FunctionalInterface
    private interface ErrorAction {
        void doIt(String s) throws InterruptException, SymbolicValueNotAllowedException;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, UndefinedResultException, SymbolicValueNotAllowedException {
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
            final String memberNameContainerClassName = memberNameContainerClassObject.representedClass();

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
            final String accessorClassName = (accessorClassInstance == null ? memberNameContainerClassName : accessorClassInstance.representedClass());
            
            //performs resolution based on memberNameFlags
            if (isMethod(memberNameFlags) || isConstructor(memberNameFlags)) {
                //memberNameDescriptorObject is an Instance of java.lang.invoke.MethodType
                //or of java.lang.String
                
                //gets the descriptor
                final String memberNameDescriptor;
                if (JAVA_METHODTYPE.equals(memberNameDescriptorObject.getType())) {
                    memberNameDescriptor = getDescriptorFromMethodType(state, memberNameDescriptorObject);
                } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType())) {
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
                final Signature methodToResolve = new Signature(memberNameContainerClassName, memberNameDescriptor, memberNameName);

                //performs resolution
                this.resolved = state.getClassHierarchy().resolveMethod(accessorClassName, methodToResolve, isInvokeInterface(memberNameFlags));
                
                //if the method is signature polymorphic and not intrinsic
                //(i.e., invoke or invokeExact) and it is not yet linked, 
                //links it
                final boolean methodIsSignaturePolymorphicNonIntrinsic = 
                    state.getClassHierarchy().getClassFile(this.resolved.getClassName()).isMethodSignaturePolymorphic(this.resolved) &&
                    !isSignaturePolymorphicMethodIntrinsic(this.resolved);
                if (methodIsSignaturePolymorphicNonIntrinsic && !state.isMethodLinked(this.resolved)) {
                    linkMethod(state, accessorClassName);
                }
                
                //if the method is signature polymorphic and nonintrinsic, 
                //checks its linked adapter+appendix and returns it instead
                //of the resolved method
                if (methodIsSignaturePolymorphicNonIntrinsic) {
                    //if the method has an appendix throws an error, 
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp, 
                    //lines 687-692 
                    if (state.getAppendix(this.resolved) != null) {
                        throwNew(state, INTERNAL_ERROR);
                        exitFromAlgorithm();
                    }
                    
                    //gets the signature of the invoker and sets this.resolved with it
                    final Instance invoker = getInstance(state, state.getAdapter(this.resolved), "invoker for the (signature polymorphic) MemberName self", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
                    final String invokerName = valueString(state, (Reference) invoker.getFieldValue(JAVA_MEMBERNAME_NAME));
                    final Instance invokerMethodType = (Instance) state.getObject((Reference) invoker.getFieldValue(JAVA_MEMBERNAME_TYPE));
                    final String invokerDescriptor = getDescriptorFromMethodType(state, invokerMethodType);
                    this.resolved = new Signature(JAVA_METHODHANDLE, invokerDescriptor, invokerName);
                }
            } else if (isField(memberNameFlags)) {
                //memberNameDescriptorObject is an Instance of java.lang.Class
                //or of java.lang.String
                
                //gets the type of the MemberName as a string
                final String memberNameType;
                if (JAVA_CLASS.equals(memberNameDescriptorObject.getType())) {
                    //memberNameDescriptorObject is an Instance of java.lang.Class:
                    //gets the name of the represented class and puts it in memberNameType
                    memberNameType = ((Instance_JAVA_CLASS) memberNameDescriptorObject).representedClass();
                } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType())) {
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
                final Signature fieldToResolve = new Signature(memberNameContainerClassName, memberNameType, memberNameName);

                //performs resolution
                this.resolved = state.getClassHierarchy().resolveField(accessorClassName, fieldToResolve);
            } else { //the member name is a type declaration, or the flags field is ill-formed
                //see hotspot:/src/share/vm/prims/methodHandles.cpp lines 658-730
                throwNew(state, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            //TODO is it ok?
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static Instance getInstance(State state, Value ref, String paramName, 
                                        ErrorAction whenNoRef, ErrorAction whenNull, ErrorAction whenUnresolved)
    throws InterruptException, SymbolicValueNotAllowedException {
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
    
    //taken from java.lang.invoke.MemberName
    private static boolean isMethod(int flags) {
        return (flags & IS_METHOD) == IS_METHOD;
    }

    //taken from java.lang.invoke.MemberName
    private static boolean isConstructor(int flags) {
        return (flags & IS_CONSTRUCTOR) == IS_CONSTRUCTOR;
    }

    //taken from java.lang.invoke.MemberName
    private static boolean isField(int flags) {
        return (flags & IS_FIELD) == IS_FIELD;
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    private static boolean isInvokeInterface(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) == REF_invokeInterface;
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
     */
    private String getDescriptorFromMethodType(State state, Instance methodType) 
    throws ThreadStackEmptyException, InterruptException {
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
                final Snippet snippet = state.snippetFactory()
                    .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                    .op_pop() //we cannot use the return value so we need to clean the stack
                    .op_return()
                    .mk();
                state.pushSnippetFrame(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve, thus reexecute this bytecode 
                exitFromAlgorithm();
            } catch (BadClassFileException | InvalidProgramCounterException e) {
                //this should never happen
                failExecution(e);
            }
        }

        //the methodDescriptor field is not null: gets  
        //its String value and puts it in memberNameDescriptor
        return valueString(state, memberNameDescriptorStringReference);
    }
    
    /**
     * Finds a {@code java.lang.invoke.MethodType} for a method
     * descriptor.
     * 
     * @param state a {@link State}.
     * @param accessor a {@link String}, the name of the accessor class.
     * @param resolved a (resolved) {@link Signature}.
     * @return a {@link ReferenceConcrete} to an {@link Instance} of 
     *         {@code java.lang.invoke.MethodType} for 
     *         {@code resolved.}{@link Signature#getDescriptor() getDescriptor}{@code ()}.
     * @throws BadClassFileException if any class referred in {@code resolved} is
     *         ill-formed or does not exist.
     * @throws ClassFileNotAccessibleException if any class referred in {@code resolved} is
     *         not accessible by {@code accessor}.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws InterruptException if the execution of this {@link Algorithm} must be interrupted.
     */
    private ReferenceConcrete findMethodType(State state, String accessor, Signature resolved) 
    throws BadClassFileException, ClassFileNotAccessibleException, HeapMemoryExhaustedException, InterruptException {
        final String descriptor = resolved.getDescriptor();
        
        //fast track: the MethodType already exists in the state's cache
        if (state.hasInstance_JAVA_METHODTYPE(descriptor)) {
            return state.referenceToInstance_JAVA_METHODTYPE(descriptor);
        }
        
        //in the case the state does not cache the MethodType
        //we upcall java.lang.invoke.MethodHandleNatives.findMethodHandleType,
        //see hotspot:/src/share/vm/prims/systemDictionary.cpp 
        //lines 2419-2507, function SystemDictionary::find_method_handle_type;
        //then upcalls the internal (pseudo)method noclass_REGISTERMETHODTYPE
        //to store the returned method handle in the state's cache
        try {
            //prepares the parameters to upcalls: 
            
            //1-the return type
            final String returnType = splitReturnValueDescriptor(descriptor);
            _ensureInstance_JAVA_CLASS(state, accessor, returnType);
            final ReferenceConcrete rtype = _referenceToInstance_JAVA_CLASS(state, returnType);
            
            //2-the parameter types
            final String[] parameterTypes = splitParametersDescriptors(descriptor);
            for (String parameterType : parameterTypes) {
                _ensureInstance_JAVA_CLASS(state, accessor, parameterType);
            }
            final ReferenceConcrete ptypes = state.createArray(null, state.getCalculator().valInt(parameterTypes.length), "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
            final Array ptypeArray = (Array) state.getObject(ptypes);
            for (int i = 0; i < parameterTypes.length; ++i) {
                ptypeArray.setFast(state.getCalculator().valInt(i), _referenceToInstance_JAVA_CLASS(state, parameterTypes[i]));
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
            final Snippet snippet = state.snippetFactory()
                .op_invokestatic(JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE)
                //let's populate the descriptor now
                .op_dup()
                .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                .op_pop() //we care only of the side effect
                //finally we register the method type
                .op_invokestatic(noclass_REGISTERMETHODTYPE)
                .op_return()
                .mk();
            state.pushSnippetFrame(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve, thus reexecute this bytecode 
            exitFromAlgorithm();
        } catch (InvalidTypeException | FastArrayAccessNotAllowedException | 
                 InvalidOperandException | ThreadStackEmptyException | InvalidProgramCounterException e) {
            //this should never happen
            failExecution(e);
        }
        
        return null; //unreachable, to keep the compiler happy
    }
    
    private void _ensureInstance_JAVA_CLASS(State state, String accessor, String type) 
    throws HeapMemoryExhaustedException, BadClassFileException, ClassFileNotAccessibleException {
        if (isPrimitive(type)) {
            try {
                state.ensureInstance_JAVA_CLASS_primitive(type);
            } catch (ClassFileNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
        } else {
            ensureInstance_JAVA_CLASS(state, accessor, type, this.ctx);
        }
    }
    
    /**
     * Links {@code this.resolved} in the case this is a signature polymorphic nonintrinsic method. 
     * This is done by upcalling {@code java.lang.invoke.MethodHandleNatives.linkMethod} 
     * (see hotspot:/src/share/vm/prims/systemDictionary.cpp, lines 2377-2394). Finally, stores the link to
     * the accessor invoker and the appendix in the {@link State}.
     * 
     * @param state a {@link State}.
     * @param accessor a {@link String}, the name of the accessor class invoking {@code this.resolved}.
     * @throws BadClassFileException if the classfile for {@code java.lang.invoke.MethodHandle} or for any 
     *         class referred in {@code this.resolved.}{@link Signature#getDescriptor() getDescriptor}{@code ()} 
     *         is ill-formed or does not exist.
     * @throws ClassFileNotAccessibleException if the classfile for {@code java.lang.invoke.MethodHandle} or for any 
     *         class referred in {@code this.resolved.}{@link Signature#getDescriptor() getDescriptor}{@code ()} 
     *         is not accessible by {@code accessor}.
     * @throws HeapMemoryExhaustedException if {@code state}'s heap memory ends.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty (should never happen).
     * @throws InterruptException always at the end of the method, as the execution of this {@link Algorithm} 
     *         must be interrupted to perform the upcall.
     */
    private void linkMethod(State state, String accessor) 
    throws BadClassFileException, ClassFileNotAccessibleException, HeapMemoryExhaustedException, 
    ThreadStackEmptyException, InterruptException {
        try {
            //upcalls java.lang.invoke.MethodHandleNatives.linkMethod
            //and stores the link, see hotspot:/src/share/vm/prims/systemDictionary.cpp 
            //lines 2377-2394        

            //prepares the parameters for the upcall:

            //1- instance of java.lang.Class<java.lang.invoke.MethodHandle>
            ensureInstance_JAVA_CLASS(state, JAVA_METHODHANDLE, JAVA_METHODHANDLE, this.ctx); //this class is always accessible
            final ReferenceConcrete mhClassRef = state.referenceToInstance_JAVA_CLASS(JAVA_METHODHANDLE);

            //2- the name of the resolved method 
            state.ensureStringLiteral(this.resolved.getName()); //this is pleonastic but (I think) future-safe
            final ReferenceConcrete mhNameRef = state.referenceToStringLiteral(this.resolved.getName());

            //3- a java.lang.invoke.MethodType for its descriptor
            final ReferenceConcrete mtRef = findMethodType(state, accessor, this.resolved);

            //4- an array with length 1 to host the returned appendix (if any)
            final ReferenceConcrete appendixBox = state.createArray(null, state.getCalculator().valInt(1), "" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND);

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
            final Snippet snippet = state.snippetFactory()
            .op_invokestatic(JAVA_METHODHANDLENATIVES_LINKMETHOD)
            //ensures that the returned MemberName is normalized, so
            //that its type field is a MethodType
            .op_dup()
            .op_invokevirtual(JAVA_MEMBERNAME_GETTYPE)
            .op_pop() //we care only of the side effect
            //stores the linked method and the appendix in the state
            .op_invokestatic(noclass_STORELINKEDMETHODANDAPPENDIX)
            .op_return()
            .mk();
            state.pushSnippetFrame(snippet, 0); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve, thus reexecute this bytecode 
            exitFromAlgorithm();
        } catch (InvalidTypeException | InvalidProgramCounterException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private ReferenceConcrete _referenceToInstance_JAVA_CLASS(State state, String type) {
        if (isPrimitive(type)) {
            return state.referenceToInstance_JAVA_CLASS_primitive(type);
        } else {
            return state.referenceToInstance_JAVA_CLASS(type);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //TODO updates the MemberName
        
        state.pushOperand(this.data.operand(0));
    }
}
