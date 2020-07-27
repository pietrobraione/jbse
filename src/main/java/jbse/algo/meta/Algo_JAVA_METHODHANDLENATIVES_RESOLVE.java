package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.isSignaturePolymorphicMethodIntrinsic;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.algo.Util.getMemberNameFlagsMethod;
import static jbse.algo.Util.isConstructor;
import static jbse.algo.Util.isField;
import static jbse.algo.Util.IS_FIELD;
import static jbse.algo.Util.isInvokeInterface;
import static jbse.algo.Util.isMethod;
import static jbse.algo.Util.isSetter;
import static jbse.algo.Util.JVM_RECOGNIZED_FIELD_MODIFIERS;
import static jbse.algo.Util.linkMethod;
import static jbse.algo.Util.REFERENCE_KIND_SHIFT;
import static jbse.algo.Util.REF_getField;
import static jbse.algo.Util.REF_getStatic;
import static jbse.algo.Util.REF_putField;
import static jbse.algo.meta.Util.FAIL_JBSE;
import static jbse.algo.meta.Util.getInstance;
import static jbse.algo.meta.Util.INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION;
import static jbse.algo.meta.Util.OK;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FLAGS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.algo.meta.Util.ErrorAction;
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
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

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
    private ClassFile resolvedClass; //set by cookMore
    private Signature resolvedSignature; //set by cookMore
    private Signature polymorphicMethodSignature; //set by cookMore
    private boolean isMethod; //set by cookMore
    private boolean isSetter; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, InvalidInputException, 
    RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
    	
        final ErrorAction THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION = msg -> { throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION); exitFromAlgorithm(); };
        final ErrorAction THROW_JAVA_INTERNAL_ERROR             = msg -> { throwNew(state, calc, INTERNAL_ERROR); exitFromAlgorithm(); };
 
        try {
            //gets the first parameter (the MemberName)
            final Instance memberNameObject = getInstance(state, this.data.operand(0), "java.lang.invoke.MethodHandleNatives.resolve", "MemberName self", FAIL_JBSE, THROW_JAVA_INTERNAL_ERROR, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);

            //now we will get all the fields of the MemberName; in the case these fields
            //are null we throw IllegalArgumentException as Hotspot does, see 
            //hotspot:/src/share/vm/prims/methodHandles.cpp line 1127 (C++ method MHN_resolve_Mem, 
            //the native implementation of java.lang.invoke.MethodHandleNatives.resolve) and line
            //589 (C++ method MethodHandles::resolve_MemberName, invoked by the former, does the 
            //heavy lifting of resolution).
            
            //gets the container class of the MemberName
            final Instance_JAVA_CLASS memberNameContainerClassObject = 
                (Instance_JAVA_CLASS) getInstance(state, memberNameObject.getFieldValue(JAVA_MEMBERNAME_CLAZZ), "java.lang.invoke.MethodHandleNatives.resolve", "Class self.clazz", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final ClassFile memberNameContainerClass = memberNameContainerClassObject.representedClass();

            //gets the descriptor of the MemberName (field type)
            final Reference memberNameDescriptorReference = (Reference) memberNameObject.getFieldValue(JAVA_MEMBERNAME_TYPE);
            final Instance memberNameDescriptorObject = getInstance(state, memberNameDescriptorReference, "java.lang.invoke.MethodHandleNatives.resolve", "Object self.type", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
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

            //gets the name of the MemberName (field name)
            final Instance memberNameNameObject = getInstance(state, memberNameObject.getFieldValue(JAVA_MEMBERNAME_NAME), "java.lang.invoke.MethodHandleNatives.resolve", "String self.name", FAIL_JBSE /* TODO is it ok? */, THROW_JAVA_ILLEGAL_ARGUMENT_EXCEPTION, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final String memberNameName = valueString(state, memberNameNameObject);
            if (memberNameName == null) {
                //TODO who is to blame?
                failExecution("Unexpected null value while accessing to String self.name parameter to java.lang.invoke.MethodHandleNatives.resolve (nonconcrete string or missing field).");
            }

            //gets the flags of the MemberName (field flags)
            final int memberNameFlags = ((Integer) ((Simplex) memberNameObject.getFieldValue(JAVA_MEMBERNAME_FLAGS)).getActualValue()).intValue();

            //gets the second parameter (the Class of the member accessor)
            final Instance_JAVA_CLASS accessorClassInstance = (Instance_JAVA_CLASS) getInstance(state, this.data.operand(1), "java.lang.invoke.MethodHandleNatives.resolve", "Class caller", FAIL_JBSE, OK, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
            final ClassFile accessorClass = (accessorClassInstance == null ? memberNameContainerClass : accessorClassInstance.representedClass());
            
            //performs resolution based on memberNameFlags
            if (isMethod(memberNameFlags) || isConstructor(memberNameFlags)) {
                this.isMethod = true;
                
                //memberNameDescriptorObject is an Instance of java.lang.invoke.MethodType
                //or of java.lang.String
                
                //gets the descriptor
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
                if (memberNameDescriptor == null) {
                    //TODO who is to blame?
                    throwVerifyError(state, calc);
                    exitFromAlgorithm();
                }

                //builds the signature of the method to resolve
                final Signature methodToResolve = new Signature(memberNameContainerClass.getClassName(), memberNameDescriptor, memberNameName);

                //performs resolution
                final boolean isInterface = isInvokeInterface(memberNameFlags);
                this.resolvedClass = state.getClassHierarchy().resolveMethod(accessorClass, methodToResolve, isInterface, state.bypassStandardLoading(), memberNameContainerClass);
                
                final boolean methodIsSignaturePolymorphic = !isInterface && this.resolvedClass.hasOneSignaturePolymorphicMethodDeclaration(methodToResolve.getName());
                final boolean methodIsSignaturePolymorphicNonIntrinsic = methodIsSignaturePolymorphic && !isSignaturePolymorphicMethodIntrinsic(methodToResolve.getName());
                if (methodIsSignaturePolymorphicNonIntrinsic) {
                    //TODO is this block dead code???
                	
                    //links it, if it is the case
                    final Signature polymorphicMethodSignatureSpecialized = new Signature(this.resolvedClass.getClassName(), methodToResolve.getDescriptor(), methodToResolve.getName());
                    linkMethod(state, calc, (Reference) this.data.operand(1), accessorClass, polymorphicMethodSignatureSpecialized);
                
                    //if the method has an appendix throws an error, 
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp, 
                    //lines 687-692
                    /* TODO in the next calls of state.getAppendix(...) and state.getAdapter(...) we used as arguments
                     * the specialized version of the polymorphic method signature, i.e., with the descriptor specialized
                     * on the actual arguments. Is it ok or should we use the non-specialized signature, i.e., 
                     * new Signature(this.resolvedClass.getClassName(), SIGNATURE_POLYMORPHIC_DESCRIPTOR, methodToResolve.getName())?
                     */
                    if (state.getAppendix(polymorphicMethodSignatureSpecialized) != null) {
                        throwNew(state, calc, INTERNAL_ERROR);
                        exitFromAlgorithm();
                    }
                    
                    //returns the adapter instead of the resolved method
                    //TODO is it correct?
                    final Instance invoker = getInstance(state, state.getAdapter(polymorphicMethodSignatureSpecialized), "java.lang.invoke.MethodHandleNatives.resolve", "invoker for the (signature polymorphic) MemberName self", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
                    final String invokerName = valueString(state, (Reference) invoker.getFieldValue(JAVA_MEMBERNAME_NAME));
                    final Reference invokerMethodReference = (Reference) invoker.getFieldValue(JAVA_MEMBERNAME_TYPE);
                    final String invokerDescriptor = getDescriptorFromMethodType(state, invokerMethodReference);
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
                    memberNameType = "" + REFERENCE + ((Instance_JAVA_CLASS) memberNameDescriptorObject).representedClass().getClassName() + TYPEEND;
                } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType().getClassName())) {
                    //memberNameDescriptorObject is an Instance of java.lang.String:
                    //gets its String value and puts it in memberNameDescriptor
                    memberNameType = "" + REFERENCE + valueString(state, memberNameDescriptorObject) + TYPEEND;
                } else {
                    //memberNameDescriptorObject is neither a Class nor a String:
                    //just fails
                    throw new UndefinedResultException("The MemberName self parameter to java.lang.invoke.MethodHandleNatives.resolve represents a field access, but self.type is neither a Class nor a String.");
                }

                //builds the signature of the field to resolve
                final Signature fieldToResolve = new Signature(memberNameContainerClass.getClassName(), memberNameType, memberNameName);

                //performs resolution
                this.resolvedClass = state.getClassHierarchy().resolveField(accessorClass, fieldToResolve, state.bypassStandardLoading(), memberNameContainerClass);
                this.resolvedSignature = new Signature(this.resolvedClass.getClassName(), fieldToResolve.getDescriptor(), fieldToResolve.getName());
            } else { //the member name is a type declaration, or the flags field is ill-formed
                //see hotspot:/src/share/vm/prims/methodHandles.cpp lines 658-730
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
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
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            //TODO is it ok?
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
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
    private static String getDescriptorFromMethodType(State state, Reference methodTypeReference) 
    throws InvalidInputException, ThreadStackEmptyException, InterruptException, FrozenStateException {
    	//gets the MethodType
    	final Instance methodTypeInstance = (Instance) state.getObject(methodTypeReference);
    	if (methodTypeInstance == null) {
    		throw new InvalidInputException("The methodTypeReference parameter of getDescriptorFromMethodType method is a Null reference, or an unresolved symbolic reference, or an invalid concrete reference.");
    	}
    	
        //gets the methodDescriptor field
        final Reference memberNameDescriptorStringReference = (Reference) methodTypeInstance.getFieldValue(JAVA_METHODTYPE_METHODDESCRIPTOR);
        if (memberNameDescriptorStringReference == null) {
            //TODO missing field: who is to blame?
            failExecution("Unexpected null value while accessing to MethodType self.type.methodDescriptor parameter to java.lang.invoke.MethodHandleNatives.resolve (missing field).");
        }

        //the methodDescriptor field of a MethodType is a cache: 
        //If it is null, invoke java.lang.invoke.MethodType.toMethodDescriptorString()
        //to fill it, and then repeat this bytecode
        if (state.isNull(memberNameDescriptorStringReference)) {
            try {
                final Snippet snippet = state.snippetFactoryNoWrap()
                    .addArg(methodTypeReference)
                    .op_aload((byte) 0)
                    .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
                    .op_pop() //we cannot use the return value so we need to clean the stack
                    .op_return()
                    .mk();
                state.pushSnippetFrameNoWrap(snippet, 0, CLASSLOADER_BOOT, "java/lang/invoke"); //zero offset so that upon return from the snippet will repeat the invocation of java.lang.invoke.MethodHandleNatives.resolve and reexecute this algorithm 
                exitFromAlgorithm();
            } catch (InvalidProgramCounterException | InvalidInputException e) {
                //this should never happen
                failExecution(e);
            }
        }

        //now the methodDescriptor field is not null: gets  
        //its String value
        return valueString(state, memberNameDescriptorStringReference);
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            try {
                final Instance memberNameObject = getInstance(state, this.data.operand(0), "java.lang.invoke.MethodHandleNatives.resolve", "MemberName self", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
                
                //updates the MemberName: first, sets the clazz field...
                state.ensureInstance_JAVA_CLASS(calc, this.resolvedClass);
                memberNameObject.setFieldValue(JAVA_MEMBERNAME_CLAZZ, state.referenceToInstance_JAVA_CLASS(this.resolvedClass));

                //...then sets the flags field
                int flags;
                if (this.isMethod) {
                    //determines the flags based on the kind of invocation, 
                    //see hotspot:/src/share/vm/prims/methodHandles.cpp line 176 
                    //method MethodHandles::init_method_MemberName; note
                    //that it is always the case that info.call_kind() == CallInfo::direct_call, see
                    //hotspot:/src/share/vm/interpreter/linkResolver.cpp line 88, method
                    //CallInfo::set_handle
                	flags = getMemberNameFlagsMethod(this.resolvedClass, this.polymorphicMethodSignature);
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
                memberNameObject.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
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
