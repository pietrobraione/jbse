package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_LAMBDAFORM;
import static jbse.bc.Signatures.JAVA_LAMBDAFORM_VMENTRY;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_FORM;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.parametersNumber;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.Arrays;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
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
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.invoke.MethodHandle#invokeBasic(Object[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_METHODHANDLE_INVOKEBASIC extends Algo_INVOKEMETA_Nonbranching {
	private ClassFile clazz; //set by cookMore
	private Signature adapterSignature; //set by cookMore
	
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> parametersNumber(this.data.signature().getDescriptor(), false);
	}

	@Override
	protected void cookMore(State state) 
	throws UndefinedResultException, SymbolicValueNotAllowedException, InvalidInputException, InterruptException, ClasspathException {
		try {
			//gets 'this'
			final Reference referenceThis = (Reference) this.data.operand(0);
			if (state.isNull(referenceThis)) {
                //this should never happen
                failExecution("Invoked method java.lang.invoke.MethodHandle.invokeBasic with null 'this' parameter.");
			}
			final Instance instanceThis = (Instance) state.getObject(referenceThis);
	        final ClassFile cf_JAVA_METHODHANDLE = state.getClassHierarchy().loadCreateClass(JAVA_METHODHANDLE);
	        if (cf_JAVA_METHODHANDLE == null) {
	            failExecution("Could not find class java.lang.invoke.MethodHandle.");
	        }
	        if (!instanceThis.getType().isSubclass(cf_JAVA_METHODHANDLE)) {
                //this should never happen
                failExecution("Invoked method java.lang.invoke.MethodHandle.invokeBasic with 'this' parameter that is not a subclass of java.lang.invoke.MethodHandle.");
	        }
	        
	        //gets the LambdaForm this.form
	        final Reference referenceForm = (Reference) instanceThis.getFieldValue(JAVA_METHODHANDLE_FORM);
			if (state.isNull(referenceForm)) {
				throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has null value for the field LambdaForm form.");
			}
			final Instance instanceForm = (Instance) state.getObject(referenceForm);
	        final ClassFile cf_JAVA_LAMBDAFORM = state.getClassHierarchy().loadCreateClass(JAVA_LAMBDAFORM);
	        if (cf_JAVA_LAMBDAFORM == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.invoke.LambdaForm.");
	        }
	        if (!instanceForm.getType().isSubclass(cf_JAVA_LAMBDAFORM)) {
	        	throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has the field form that is not a subclass of java.lang.invoke.LambdaForm.");
	        }

	        //gets the MemberName this.form.vmentry
	        final Reference referenceVmentry = (Reference) instanceForm.getFieldValue(JAVA_LAMBDAFORM_VMENTRY);
			if (state.isNull(referenceVmentry)) {
				throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has null value for the field Membername form.vmentry.");
			}
			final Instance instanceVmentry = (Instance) state.getObject(referenceVmentry);
	        final ClassFile cf_JAVA_MEMBERNAME = state.getClassHierarchy().loadCreateClass(JAVA_MEMBERNAME);
	        if (cf_JAVA_MEMBERNAME == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.invoke.MemberName.");
	        }
	        if (!cf_JAVA_MEMBERNAME.equals(instanceVmentry.getType())) {
	        	throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has the field form.vmentry that is not a java.lang.invoke.MemberName.");
	        }
	        
	        //from vmentry gets the name...
	        final Reference referenceName = (Reference) instanceVmentry.getFieldValue(JAVA_MEMBERNAME_NAME);
			if (state.isNull(referenceName)) {
				throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has null value for the field String form.vmentry.name.");
			}
			final Instance instanceName = (Instance) state.getObject(referenceName);
	        final ClassFile cf_JAVA_STRING = state.getClassHierarchy().loadCreateClass(JAVA_STRING);
	        if (cf_JAVA_STRING == null) {
	        	//this should never happen
	            failExecution("Could not find class java.lang.String.");
	        }
	        if (!cf_JAVA_STRING.equals(instanceName.getType())) {
	        	throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has the field form.vmentry.name that is not a java.lang.String.");
	        }
	        final String name = valueString(state, instanceName);
	        if (name == null) {
	        	throw new SymbolicValueNotAllowedException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has the field form.vmentry.name that is a symbolic String.");
	        }

	        //...and the clazz
	        final Reference referenceClazz = (Reference) instanceVmentry.getFieldValue(JAVA_MEMBERNAME_CLAZZ);
			if (state.isNull(referenceName)) {
				throw new UndefinedResultException("The 'this' parameter to java.lang.invoke.MethodHandle.invokeBasic has null value for the field ClassFile form.vmentry.clazz.");
			}
			final Instance_JAVA_CLASS instanceClazz = (Instance_JAVA_CLASS) state.getObject(referenceClazz);
	        this.clazz = instanceClazz.representedClass();

	        //builds the signature of the adapter
	        final String[] splitParametersDescriptorSignature = splitParametersDescriptors(this.data.signature().getDescriptor());
	        final String splitReturnValueDescriptorSignature = splitReturnValueDescriptor(this.data.signature().getDescriptor());
	        final String descriptor = "(" + 
	        		REFERENCE + JAVA_OBJECT + TYPEEND +  //the form to invoke
	        		String.join("", Arrays.stream(splitParametersDescriptorSignature).map(jbse.common.Type::simplifyType).toArray(String[]::new)) +
	        		")" + jbse.common.Type.simplifyType(splitReturnValueDescriptorSignature);
			this.adapterSignature = new Signature(this.clazz.getClassName(), descriptor, name);
		} catch (ClassCastException e) {
			throw new UndefinedResultException(e);
        } catch (ClassFileNotFoundException e) {
            //TODO should this exception wrap a ClassNotFoundException?
            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
        	//TODO should we wrap/throw a ClassNotFoundException?
        	throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); 
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            //TODO is it ok?
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
		} catch (ClassFileNotAccessibleException | RenameUnsupportedException e) {
			//this should never happen
			failExecution(e);
		}
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
            try {
                state.pushFrame(this.ctx.getCalculator(), this.clazz, this.adapterSignature, false, offsetInvoke(false), this.data.operands());
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
