package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.Util.valueString;
import static jbse.algo.meta.Util.FAIL_JBSE;
import static jbse.algo.meta.Util.getInstance;
import static jbse.algo.meta.Util.INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION;
import static jbse.bc.Modifiers.IS_FIELD;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.common.Type.LONG;
import static jbse.common.Type.toInternalName;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FLAGS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_STRING;

import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.Util.ErrorAction;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level abstract implementation of {@link java.lang.invoke.MethodHandleNatives#objectFieldOffset(java.lang.invoke.MemberName)}
 * and {@link java.lang.invoke.MethodHandleNatives#staticFieldOffset(java.lang.invoke.MemberName)}
 * 
 * @author Pietro Braione
 */
abstract class Algo_JAVA_METHODHANDLENATIVES_ABSTRACTFIELDOFFSET extends Algo_INVOKEMETA_Nonbranching {
	private final String methodName;
	private final boolean mustBeStatic;
	private Simplex ofst; //set by cookMore
	
	protected Algo_JAVA_METHODHANDLENATIVES_ABSTRACTFIELDOFFSET(String methodName, boolean mustBeStatic) {
		this.methodName = methodName;
		this.mustBeStatic = mustBeStatic;
	}
	
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 1;
	}

	@Override
	protected void cookMore(State state) 
	throws InterruptException, SymbolicValueNotAllowedException, ClasspathException, 
	InvalidInputException, NoSuchElementException, InvalidTypeException, UndefinedResultException {
		final ErrorAction THROW_JAVA_INTERNAL_ERROR = msg -> { throwNew(state, this.ctx.getCalculator(), INTERNAL_ERROR); exitFromAlgorithm(); };

		//checks the first parameter (the MemberName)
		final Instance memberNameObject = getInstance(state, this.data.operand(0), this.methodName, "MemberName self", FAIL_JBSE, THROW_JAVA_INTERNAL_ERROR, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		
		//gets the container class of the member
		final Reference clazzReference = (Reference) memberNameObject.getFieldValue(JAVA_MEMBERNAME_CLAZZ);
		if (state.isNull(clazzReference)) {
			//not resolved
			throwNew(state, this.ctx.getCalculator(), INTERNAL_ERROR);
			exitFromAlgorithm();
		}
		final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(clazzReference);
		final ClassFile containerClass = clazz.representedClass(); 

		//checks if the member is an instance field
		final int fieldFlags = ((Integer) ((Simplex) memberNameObject.getFieldValue(JAVA_MEMBERNAME_FLAGS)).getActualValue()).intValue();
		if ((fieldFlags & IS_FIELD) == 0 || (this.mustBeStatic ? ((fieldFlags & Modifier.STATIC) == 0) : ((fieldFlags & Modifier.STATIC) != 0))) {
			//not an instance field
			throwNew(state, this.ctx.getCalculator(), INTERNAL_ERROR);
			exitFromAlgorithm();
		}
		
        //gets the type of the MemberName as a string
		final Instance memberNameDescriptorObject = (Instance) state.getObject((Reference) memberNameObject.getFieldValue(JAVA_MEMBERNAME_TYPE));
        final String memberNameType;
        if (JAVA_CLASS.equals(memberNameDescriptorObject.getType().getClassName())) {
            //memberNameDescriptorObject is an Instance of java.lang.Class:
            //gets the name of the represented class and puts it in memberNameType
            memberNameType = toInternalName(((Instance_JAVA_CLASS) memberNameDescriptorObject).representedClass().getClassName());
        } else if (JAVA_STRING.equals(memberNameDescriptorObject.getType().getClassName())) {
            //memberNameDescriptorObject is an Instance of java.lang.String:
            //gets its String value and puts it in memberNameDescriptor
            memberNameType = toInternalName(valueString(state, memberNameDescriptorObject)); //TODO shall we replace . with / in class names???
        } else {
            //memberNameDescriptorObject is neither a Class nor a String:
            //just fails
            throw new UndefinedResultException("The MemberName self parameter to java.lang.invoke.MethodHandleNatives.xxxFieldOffset represents a field access, but self.type is neither a Class nor a String.");
        }

		//gets the offset
		final Signature fieldSignature = 
				new Signature(containerClass.getClassName(), memberNameType, 
				              valueString(state, (Reference) memberNameObject.getFieldValue(JAVA_MEMBERNAME_NAME)));
		final int _ofst = containerClass.getFieldOffset(fieldSignature);
		this.ofst = (Simplex) this.ctx.getCalculator().pushInt(_ofst).to(LONG).pop();
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
			state.pushOperand(this.ofst);
		};
	}
}
