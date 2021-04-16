package jbse.algo;

import static jbse.algo.UtilControlFlow.continueWith;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.UtilJavaLangInvokeObjects.getDescriptorFromMemberName;
import static jbse.algo.UtilLinking.ensureMethodLinked;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.isSignaturePolymorphicMethodIntrinsic;
import static jbse.algo.Util.isSignaturePolymorphicMethodStatic;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.parametersNumber;

import java.util.function.Supplier;

import jbse.algo.BytecodeData_1KME.Kind;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.Algo_JAVA_METHODHANDLE_INVOKEBASIC;
import jbse.algo.meta.Algo_JAVA_METHODHANDLE_LINKTO;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;

/**
 * Algorithm for the invokehandle internal bytecode.
 * 
 * @author Pietro Braione
 *
 */
final class Algo_INVOKEHANDLE extends Algorithm<
BytecodeData_1KME,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
	private final Algo_JAVA_METHODHANDLE_INVOKEBASIC algo_JAVA_METHODHANDLE_INVOKEBASIC; //set by constructor
	private final Algo_JAVA_METHODHANDLE_LINKTO algo_JAVA_METHODHANDLE_LINKTO; //set by constructor
	private ClassFile clazz; //set by bytecodeCooker
	private Signature adapterSignature; //set by bytecodeCooker
	private Value[] parameters; //set by bytecodeCooker
	
	public Algo_INVOKEHANDLE() { 
		this.algo_JAVA_METHODHANDLE_INVOKEBASIC = new Algo_JAVA_METHODHANDLE_INVOKEBASIC();
		this.algo_JAVA_METHODHANDLE_LINKTO = new Algo_JAVA_METHODHANDLE_LINKTO();
		this.algo_JAVA_METHODHANDLE_INVOKEBASIC.setFeatures(false, false, false, true, JAVA_METHODHANDLE_INVOKEBASIC);
	}

    @Override
    protected final Supplier<BytecodeData_1KME> bytecodeData() {
        return () -> BytecodeData_1KME.withMethod(Kind.BOTH).get();
    }
    
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> {
            return parametersNumber(this.data.signature().getDescriptor(), isSignaturePolymorphicMethodStatic(this.data.signature().getName()));
        };
    }
    
	@Override
	protected BytecodeCooker bytecodeCooker() {
		return (state) -> {
			if (isSignaturePolymorphicMethodIntrinsic(this.data.signature().getName())) {
				switch (this.data.signature().getName()) {
				case "invokeBasic":
					continueWith(this.algo_JAVA_METHODHANDLE_INVOKEBASIC);
				case "linkToInterface":
					this.algo_JAVA_METHODHANDLE_LINKTO.setFeatures(false, false, true, true, JAVA_METHODHANDLE_LINKTOINTERFACE);
					this.algo_JAVA_METHODHANDLE_LINKTO.setLinkFeatures(true, false, false);
					continueWith(this.algo_JAVA_METHODHANDLE_LINKTO);
				case "linkToSpecial":
					this.algo_JAVA_METHODHANDLE_LINKTO.setFeatures(false, false, true, true, JAVA_METHODHANDLE_LINKTOSPECIAL);
					this.algo_JAVA_METHODHANDLE_LINKTO.setLinkFeatures(false, true, false);
					continueWith(this.algo_JAVA_METHODHANDLE_LINKTO);
				case "linkToStatic":
					this.algo_JAVA_METHODHANDLE_LINKTO.setFeatures(false, false, true, true, JAVA_METHODHANDLE_LINKTOSTATIC);
					this.algo_JAVA_METHODHANDLE_LINKTO.setLinkFeatures(false, false, true);
					continueWith(this.algo_JAVA_METHODHANDLE_LINKTO);
				case "linkToVirtual":
					this.algo_JAVA_METHODHANDLE_LINKTO.setFeatures(false, false, true, true, JAVA_METHODHANDLE_LINKTOVIRTUAL);
					this.algo_JAVA_METHODHANDLE_LINKTO.setLinkFeatures(false, false, false);
					continueWith(this.algo_JAVA_METHODHANDLE_LINKTO);
				default:
					//this should never happen
					failExecution("Unrecognized signature-polymorphic intrinsic method (unreachable code).");
				}
			} else {
            	final Calculator calc = this.ctx.getCalculator();
            	
            	//possibly links the method
	            try {
	            	final ClassFile currentClass = state.getCurrentClass();
	            	state.ensureInstance_JAVA_CLASS(calc, currentClass);
	            	ensureMethodLinked(state, calc, state.referenceToInstance_JAVA_CLASS(currentClass), currentClass, this.data.signature());
				} catch (PleaseLoadClassException e) {
		            invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
		            exitFromAlgorithm();
				} catch (ClassFileNotFoundException e) {
		            //TODO should this exception wrap a ClassNotFoundException?
		            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
		            exitFromAlgorithm();
				} catch (BadClassFileVersionException e) {
		            throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
		            exitFromAlgorithm();
				} catch (WrongClassNameException e) {
		            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
		            exitFromAlgorithm();
		        } catch (IncompatibleClassFileException e) {
		            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
		            exitFromAlgorithm();
				} catch (ClassFileNotAccessibleException e) {
		            throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
		            exitFromAlgorithm();
				} catch (HeapMemoryExhaustedException e) {
		            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
		            exitFromAlgorithm();
		        } catch (ClassFileIllFormedException e) {
		            //TODO is it ok?
		            throwVerifyError(state, this.ctx.getCalculator());
		            exitFromAlgorithm();
				}
	            
	        	//gets the adapter
	        	final ReferenceConcrete adapterReference = state.getMethodAdapter(this.data.signature());
				if (state.isNull(adapterReference)) {
					//this should never happen
					failExecution("Null reference stored in state as adapter of a signature-polymorphic non-intrinsic method.");
				}
				final Instance adapterInstance = (Instance) state.getObject(adapterReference);
		        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
		        if (cf_JAVA_MEMBERNAME == null) {
		        	//this should never happen
		            failExecution("Could not find class java.lang.invoke.MemberName.");
		        }
		        if (!cf_JAVA_MEMBERNAME.equals(adapterInstance.getType())) {
					//this should never happen
		        	failExecution("Reference to an instance not of class MemberName stored in state as adapter of a signature-polymorphic non-intrinsic method.");
		        }

		        //from the adapter gets the name...
		        final Reference referenceName = (Reference) adapterInstance.getFieldValue(JAVA_MEMBERNAME_NAME);
				if (state.isNull(referenceName)) {
					//this should never happen
					failExecution("An adapter of a signature-polymorphic non-intrinsic method has null value for the field String name.");
				}
				final Instance instanceName = (Instance) state.getObject(referenceName);
		        final ClassFile cf_JAVA_STRING = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING); //surely loaded
		        if (cf_JAVA_STRING == null) {
		        	//this should never happen
		            failExecution("Could not find class java.lang.String.");
		        }
		        if (!cf_JAVA_STRING.equals(instanceName.getType())) {
					//this should never happen
		        	failExecution("An adapter of a signature-polymorphic non-intrinsic method has the field name that is not a java.lang.String.");
		        }
		        final String name = valueString(state, instanceName);
		        if (name == null) {
		        	throw new SymbolicValueNotAllowedException("An adapter of a signature-polymorphic non-intrinsic method has the field name that is a symbolic String.");
		        }

		        //...and the clazz
		        final Reference referenceClazz = (Reference) adapterInstance.getFieldValue(JAVA_MEMBERNAME_CLAZZ);
				if (state.isNull(referenceName)) {
					throw new UndefinedResultException("An adapter of a signature-polymorphic non-intrinsic method has null value for the field ClassFile clazz.");
				}
				final Instance_JAVA_CLASS instanceClazz = (Instance_JAVA_CLASS) state.getObject(referenceClazz);
		        this.clazz = instanceClazz.representedClass();
		        
		        //builds the signature of the adapter method
		        final String descriptor = getDescriptorFromMemberName(state, adapterReference);
		        this.adapterSignature = new Signature(this.clazz.getClassName(), descriptor, name);
		        
	        	//gets the appendix
		        final Reference appendixReference = state.getMethodAppendix(this.data.signature());
		        if (!(state.getObject(appendixReference) instanceof Array)) {
		        	//this should never happen
		        	failExecution("Reference to an appendix of a signature-polymorphic non-intrinsic method stored in a state does not refer to an array.");
		        }
		        final Array appendixArray = (Array) state.getObject(appendixReference);
		        if (!appendixArray.hasSimpleRep()) {
		        	//this should never happen
		        	failExecution("An appendix of a signature-polymorphic non-intrinsic method stored in a state is not a simple array.");
		        }
		        final int appendixArrayLength = ((Integer) ((Simplex) appendixArray.getLength()).getActualValue()).intValue();
		        final boolean hasAppendix = (appendixArrayLength > 0);
		        
		        //builds the parameters
		        this.parameters = new Value[this.data.operands().length + (hasAppendix ? 1 : 0)];
		        System.arraycopy(this.data.operands(), 0, this.parameters, 0, this.data.operands().length);
		        if (hasAppendix) {
		        	try {
						this.parameters[this.data.operands().length] = ((Array.AccessOutcomeInValue) appendixArray.getFast(calc, calc.valInt(0))).getValue();
					} catch (FastArrayAccessNotAllowedException | ClassCastException e) {
			        	//this should never happen
			        	failExecution(e);
					}
		        }
			}
		};
	}

	@Override
	protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
		return DecisionAlternative_NONE.class;
	}

	@Override
	protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
	}

	@Override
	protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                state.pushFrame(this.ctx.getCalculator(), this.clazz, this.adapterSignature, false, offsetInvoke(false), this.parameters);
            } catch (InvalidProgramCounterException | InvalidSlotException e) {
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
