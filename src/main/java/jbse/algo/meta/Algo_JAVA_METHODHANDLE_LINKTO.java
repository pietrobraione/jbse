package jbse.algo.meta;

import static jbse.algo.Util.checkOverridingMethodFits;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.lookupMethodImpl;
import static jbse.algo.Util.lookupMethodImplOverriding;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.parametersNumber;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.BaseUnsupportedException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.invoke.MethodHandle#linkToInterface(Object[])}, 
 * {@link java.lang.invoke.MethodHandle#linkToSpecial(Object[])}, {@link java.lang.invoke.MethodHandle#linkToStatic(Object[])} 
 * and {@link java.lang.invoke.MethodHandle#linkToVirtual(Object[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_METHODHANDLE_LINKTO extends Algo_INVOKEMETA_Nonbranching {
	private boolean isLinkInterface;       //set by setLinkFeatures
	private boolean isLinkSpecial;         //set by setLinkFeatures
	private boolean isLinkStatic;          //set by setLinkFeatures
    private ClassFile methodImplClass;     //set by cookMore
    private Signature methodImplSignature; //set by cookMore
    private boolean isMethodImplNative;    //set by cookMore
    private Value[] parameters;            //set by cookMore
    
    public void setLinkFeatures(boolean isLinkInterface, boolean isLinkSpecial, boolean isLinkStatic) {
    	this.isLinkInterface = isLinkInterface;
    	this.isLinkSpecial = isLinkSpecial;
    	this.isLinkStatic = isLinkStatic;
    }
    
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> parametersNumber(this.data.signature().getDescriptor(), true);
	}

	@Override
	protected void cookMore(State state) 
	throws UndefinedResultException, SymbolicValueNotAllowedException, 
	ThreadStackEmptyException, InterruptException, InvalidInputException, 
	RenameUnsupportedException, ClasspathException, BaseUnsupportedException, MetaUnsupportedException {
		final Calculator calc = this.ctx.getCalculator();
		try {
			//gets the trailing MemberName
			final Reference referenceMemberName = (Reference) this.data.operand(numOperands().get() - 1);
			if (state.isNull(referenceMemberName)) {
				throw new UndefinedResultException("Invoked method java.lang.invoke.MethodHandle.linkToVirtual with null trailing MemberName parameter.");
			}
			final Instance instanceMemberName = (Instance) state.getObject(referenceMemberName);
	        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().loadCreateClass(JAVA_MEMBERNAME);
	        if (cf_JAVA_MEMBERNAME == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.invoke.MemberName.");
	        }
	        if (!cf_JAVA_MEMBERNAME.equals(instanceMemberName.getType())) {
	        	throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual is not a java.lang.invoke.MemberName.");
	        }
	        
	        //from the trailing MemberName gets the name...
	        final Reference referenceName = (Reference) instanceMemberName.getFieldValue(JAVA_MEMBERNAME_NAME);
			if (state.isNull(referenceName)) {
				throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has null value for the field String name.");
			}
			final Instance instanceName = (Instance) state.getObject(referenceName);
	        final ClassFile cf_JAVA_STRING = state.getClassHierarchy().loadCreateClass(JAVA_STRING);
	        if (cf_JAVA_STRING == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.String.");
	        }
	        if (!cf_JAVA_STRING.equals(instanceName.getType())) {
	        	throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has the field name that is not a java.lang.String.");
	        }
	        final String name = valueString(state, instanceName);
	        if (name == null) {
	        	throw new SymbolicValueNotAllowedException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has the field name that is a symbolic String.");
	        }

	        //...the clazz...
	        final Reference referenceClazz = (Reference) instanceMemberName.getFieldValue(JAVA_MEMBERNAME_CLAZZ);
			if (state.isNull(referenceName)) {
				throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has null value for the field ClassFile clazz.");
			}
			final Instance_JAVA_CLASS instanceClazz = (Instance_JAVA_CLASS) state.getObject(referenceClazz);
	        final ClassFile clazz = instanceClazz.representedClass();
	        
	        //...and the type
			final Reference referenceType = (Reference) instanceMemberName.getFieldValue(JAVA_MEMBERNAME_TYPE);
			if (state.isNull(referenceMemberName)) {
                //this should never happen
                failExecution("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has null value for the field Object type.");
			}
			final Instance instanceType = (Instance) state.getObject(referenceType);
	        final ClassFile cf_JAVA_METHODTYPE = state.getClassHierarchy().loadCreateClass(JAVA_METHODTYPE);
	        if (cf_JAVA_METHODTYPE == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.invoke.MethodType.");
	        }
	        if (!cf_JAVA_METHODTYPE.equals(instanceType.getType())) {
	        	throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has the field type that is not a java.lang.invoke.MethodType.");
	        }
	        
	        //gets the descriptor from instanceType
	        final Reference referenceDescriptor = (Reference) instanceType.getFieldValue(JAVA_METHODTYPE_METHODDESCRIPTOR);
	        //the methodDescriptor field of a MethodType is a cache: 
	        //If it is null, invoke java.lang.invoke.MethodType.toMethodDescriptorString()
	        //to fill it, and then repeat this bytecode
			if (state.isNull(referenceDescriptor)) {
	            try {
	                final Snippet snippet = state.snippetFactoryNoWrap()
	    	            .addArg(referenceType)
	                    .op_aload((byte) 0)
	                    .op_invokevirtual(JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING)
	                    .op_pop() //we cannot use the return value so we need to clean the stack
	                    .op_return()
	                    .mk();
	                state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_MEMBERNAME); //zero offset so that upon return from the snippet will repeat the invocation of invokehandle and reexecute this algorithm 
	                exitFromAlgorithm();
	            } catch (InvalidProgramCounterException | InvalidInputException e) {
	                //this should never happen
	                failExecution(e);
	            }
			}
	        //now the methodDescriptor field is not null: gets  
	        //its String value
			final Instance instanceDescriptor = (Instance) state.getObject(referenceDescriptor);
	        if (!cf_JAVA_STRING.equals(instanceDescriptor.getType())) {
	        	throw new UndefinedResultException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has the field type.methodDescriptor that is not a java.lang.String.");
	        }
	        final String descriptor = valueString(state, instanceDescriptor);
	        if (descriptor == null) {
	        	throw new SymbolicValueNotAllowedException("The trailing MemberName parameter to java.lang.invoke.MethodHandle.linkToVirtual has the field type.methodDescriptor that is a symbolic String.");
	        }
	        
	        //builds the signature of the method to invoke
	        final Signature signatureToInvoke = new Signature(clazz.getClassName(), descriptor, name);

	        //performs method lookup
            final boolean isVirtualInterface = !this.isLinkStatic && !this.isLinkSpecial;
            final ClassFile receiverClass;
            if (isVirtualInterface) {
                final Reference thisRef = (Reference) this.data.operand(0);
                receiverClass = state.getObject(thisRef).getType();
            } else {
                receiverClass = null;
            }
            try {
				this.methodImplClass = 
				    lookupMethodImpl(state, 
				                     clazz, 
				                     signatureToInvoke,
				                     this.isLinkInterface, 
				                     this.isLinkSpecial, 
				                     this.isLinkStatic,
				                     receiverClass);
	            this.methodImplSignature = 
	                    new Signature(this.methodImplClass.getClassName(), descriptor, name);
	            this.isMethodImplNative = this.methodImplClass.isMethodNative(this.methodImplSignature);
            } catch (IncompatibleClassFileException | MethodNotAccessibleException | 
            		 MethodAbstractException | MethodNotFoundException e) {
            	throw new UndefinedResultException(e);
            }
            
            //in the case of varargs methods, if the number of operands is more than the 
            //number of parameters in the method signature, or the last operand is not a vararg
            //array type, decides to boxe the last parameters in a varargs array. 
            //Remember to exclude the trailing MemberName, that shall *not* be passed
            //as a parameter to the invoked method!!!
    		final boolean boxVarargs;
			ClassFile varargsArrayClass = null;
			final int formalParametersNumber = parametersNumber(this.methodImplSignature.getDescriptor(), this.isLinkStatic);
    		if (this.methodImplClass.isMethodVarargs(this.methodImplSignature)) {
				final String[] methodImplDescriptorSplit = splitParametersDescriptors(this.methodImplSignature.getDescriptor());
				final String varargsArrayType = methodImplDescriptorSplit[methodImplDescriptorSplit.length - 1];
				try {
					varargsArrayClass = state.getClassHierarchy().resolveClass(state.getCurrentClass(), varargsArrayType, state.bypassStandardLoading());
				} catch (PleaseLoadClassException e) {
		            invokeClassLoaderLoadClass(state, calc, e);
		            exitFromAlgorithm();
				}
    			if (numOperands().get() - 1 > formalParametersNumber) {
    				boxVarargs = true;
    			} else {
    				final Value lastArgument = this.data.operands()[numOperands().get() - 2];
    				if (lastArgument instanceof Reference) {
    					final Reference lastArgumentRef = (Reference) lastArgument;
    					final HeapObjekt lastArgumentObj = state.getObject(lastArgumentRef);
    					if (lastArgumentObj instanceof Array) {
    						final Array lastArgumentArray = (Array) lastArgumentObj;
    						if (state.getClassHierarchy().isAssignmentCompatible(lastArgumentArray.getType(), varargsArrayClass)) {
    							boxVarargs = false;
    						} else {
    							boxVarargs = true;
    						}
    					} else {
        					//the last argument is not a varargs array
        					boxVarargs = true;
    					}
    				} else {
    					//the last argument is not a varargs array
    					boxVarargs = true;
    				}
    			}
    		} else {
    			boxVarargs = false;
    		}

    		//builds the parameters array (again, remember to exclude the trailing operand, 
    		//that is not passed as a parameter)
    		if (boxVarargs) {	
    			this.parameters = new Value[formalParametersNumber];
    			System.arraycopy(this.data.operands(), 0, this.parameters, 0, this.parameters.length - 1);
    			final int arrayLength = numOperands().get() - formalParametersNumber;
    			final ReferenceConcrete referenceVarargsArray = state.createArray(calc, null, calc.valInt(arrayLength), varargsArrayClass);
    			final Array varargsArray = (Array) state.getObject(referenceVarargsArray);
    			final int start = formalParametersNumber - 1;
    			for (int i = 0; i < arrayLength; ++i) {
    				varargsArray.setFast(calc.valInt(i), this.data.operands()[start + i]);
    			}
    			this.parameters[formalParametersNumber - 1] = referenceVarargsArray;
    		} else {
    			this.parameters = new Value[numOperands().get() - 1];
    			System.arraycopy(this.data.operands(), 0, this.parameters, 0, this.parameters.length);
    		}
    		
            //looks for a base-level or meta-level overriding implementation, 
            //and in case considers it instead
            findOverridingImpl(state);
		} catch (ClassCastException e) {
			throw new UndefinedResultException(e);
        } catch (ClassFileNotFoundException e) {
            //TODO should this exception wrap (or throw) a ClassNotFoundException?
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
		} catch (BadClassFileVersionException e) {
            throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
		} catch (ClassFileNotAccessibleException e) {
            throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
		} catch (IncompatibleClassFileException e) {
            throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
		} catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
		} catch (ClassFileIllFormedException e) {
            //TODO is it ok?
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (MethodNotFoundException | FastArrayAccessNotAllowedException | 
        		InvalidTypeException | WrongClassNameException | InvalidNumberOfOperandsException e) {
           	//this should never happen
           	failExecution(e);
		}
	}
	
	private void findOverridingImpl(State state) throws BaseUnsupportedException, MetaUnsupportedException, InterruptException, 
    ClasspathException, ThreadStackEmptyException, InvalidInputException, InvalidNumberOfOperandsException {
        final Signature methodSignatureOverriding = lookupMethodImplOverriding(state, this.ctx, this.methodImplClass, this.methodImplSignature, this.isLinkInterface, this.isLinkSpecial, this.isLinkStatic, this.isMethodImplNative, true);
        if (methodSignatureOverriding == null) {
            return;
        }

        try {
            final ClassHierarchy hier = state.getClassHierarchy();
            final ClassFile classFileMethodOverriding = hier.getClassFileClassArray(CLASSLOADER_APP, methodSignatureOverriding.getClassName()); //if lookup of overriding method succeeded, the class is surely loaded
            checkOverridingMethodFits(state, this.methodImplClass, this.methodImplSignature, classFileMethodOverriding, methodSignatureOverriding);
            this.methodImplClass = classFileMethodOverriding;
            this.methodImplSignature = methodSignatureOverriding;
        } catch (MethodNotFoundException e) {
            throw new BaseUnsupportedException(e);
        }
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
			try {
	            //if the method is native, fails (should have an 
	            //overriding implementation)
                if (this.methodImplClass.isMethodNative(this.methodImplSignature)) {
                    throw new NotYetImplementedException("Method " + this.methodImplSignature + " is native and has no overriding implementation.");
                }
                
                //pushes the frame
				state.pushFrame(this.ctx.getCalculator(), this.methodImplClass, this.methodImplSignature, false, offsetInvoke(false), this.parameters);
            } catch (InvalidProgramCounterException | InvalidSlotException | InvalidTypeException e) {
                //TODO is it ok?
                throwVerifyError(state, this.ctx.getCalculator());
            } catch (NullMethodReceiverException | MethodNotFoundException | MethodCodeNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
		};
	}

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}
