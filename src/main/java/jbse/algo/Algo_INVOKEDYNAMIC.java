package jbse.algo;

import static jbse.algo.UtilControlFlow.continueWith;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.UtilJavaLangInvokeObjects.getDescriptorFromMemberName;
import static jbse.algo.UtilLinking.ensureCallSiteLinked;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * Algorithm for the INVOKEDYNAMIC bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_INVOKEDYNAMIC extends Algorithm<
BytecodeData_1CS,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
	private final Algo_INVOKEDYNAMIC_Completion algo_INVOKEDYNAMIC_Completion;
	
	public Algo_INVOKEDYNAMIC() {
		this.algo_INVOKEDYNAMIC_Completion  = new Algo_INVOKEDYNAMIC_Completion();
	}

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0; //not used
    }

    @Override
    protected Supplier<BytecodeData_1CS> bytecodeData() {
        return BytecodeData_1CS::get;
    }

	@Override
	protected BytecodeCooker bytecodeCooker() {
		return (state) -> {
        	final Calculator calc = this.ctx.getCalculator();
        	
        	//gets info about the current dynamic call site
        	final ClassFile caller = state.getCurrentClass();
        	final String callerDescriptor = state.getCurrentMethodSignature().getDescriptor();
        	final String callerName = state.getCurrentMethodSignature().getName();
        	final int callerProgramCounter = state.getCurrentProgramCounter();
        	
        	//possibly links the call site
        	try {
				ensureCallSiteLinked(state, calc, caller, callerDescriptor, callerName, callerProgramCounter, this.data.callSiteSpecifier());
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
        	final ReferenceConcrete adapterReference = state.getCallSiteAdapter(caller, callerDescriptor, callerName, callerProgramCounter);
			if (state.isNull(adapterReference)) {
				//this should never happen
				failExecution("Null reference stored in state as adapter of a call site.");
			}
			final Instance adapterInstance = (Instance) state.getObject(adapterReference);
	        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
	        if (cf_JAVA_MEMBERNAME == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.invoke.MemberName.");
	        }
	        if (!cf_JAVA_MEMBERNAME.equals(adapterInstance.getType())) {
				//this should never happen
	        	failExecution("Reference to an instance not of class MemberName stored in state as adapter of a call site.");
	        }

	        //from the adapter gets the name...
	        final Reference referenceName = (Reference) adapterInstance.getFieldValue(JAVA_MEMBERNAME_NAME);
			if (state.isNull(referenceName)) {
				//this should never happen
				failExecution("An adapter of a call site has null value for the field String name.");
			}
			final Instance instanceName = (Instance) state.getObject(referenceName);
	        final ClassFile cf_JAVA_STRING = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING); //surely loaded
	        if (cf_JAVA_STRING == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.String.");
	        }
	        if (!cf_JAVA_STRING.equals(instanceName.getType())) {
				//this should never happen
	        	failExecution("An adapter of a call site has the field name that is not a java.lang.String.");
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
	        final ClassFile adapterClass = instanceClazz.representedClass();
	        
	        //builds the signature of the adapter method
	        final String descriptor = getDescriptorFromMemberName(state, adapterReference);
	        final Signature adapterSignature = new Signature(adapterClass.getClassName(), descriptor, name);
	        
        	//gets the appendix
	        final Reference appendixReference = state.getCallSiteAppendix(caller, callerDescriptor, callerName, callerProgramCounter);
	        if (!(state.getObject(appendixReference) instanceof Array)) {
	        	//this should never happen
	        	failExecution("Reference to an appendix of a call site stored in a state does not refer to an array.");
	        }
	        final Array appendixArray = (Array) state.getObject(appendixReference);
	        if (!appendixArray.hasSimpleRep()) {
	        	//this should never happen
	        	failExecution("An appendix of a call site stored in a state is not a simple array.");
	        }
	        final int appendixArrayLength = ((Integer) ((Simplex) appendixArray.getLength()).getActualValue()).intValue();
	        final boolean hasAppendix = (appendixArrayLength > 0);
	        
	        //continues
	        this.algo_INVOKEDYNAMIC_Completion.setAdapterClass(adapterClass);
	        this.algo_INVOKEDYNAMIC_Completion.setAdapterSignature(adapterSignature);
	        this.algo_INVOKEDYNAMIC_Completion.setHasAppendix(hasAppendix);
	        this.algo_INVOKEDYNAMIC_Completion.setAppendixArray(appendixArray);
	        continueWith(this.algo_INVOKEDYNAMIC_Completion);
		};
	}

	@Override
	protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
		return null; //unreachable
	}

	@Override
	protected StrategyDecide<DecisionAlternative_NONE> decider() {
		return null; //unreachable
	}

	@Override
	protected StrategyRefine<DecisionAlternative_NONE> refiner() {
		return null; //unreachable
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return null; //unreachable
	}

	@Override
	protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
		return null; //unreachable
	}

	@Override
	protected Supplier<Integer> programCounterUpdate() {
		return null; //unreachable
	}
}
