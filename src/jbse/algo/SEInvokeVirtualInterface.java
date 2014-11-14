package jbse.algo;

import static jbse.algo.Util.ABSTRACT_METHOD_ERROR;
import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.NO_SUCH_METHOD_ERROR;
import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;

import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.PleaseDoNativeException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NoMethodReceiverException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class SEInvokeVirtualInterface implements Algorithm {
	boolean isInterface;
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws CannotManageStateException, DecisionException, 
	ThreadStackEmptyException, OperandStackEmptyException, 
	ContradictionException, FailureException {
		//gets index operand from instruction word and 
		//calculates/stores the pointer to the next instruction
		final int index;
		try {
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			index = Util.byteCat(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
		
		//gets the signature of the method to be invoked and
		//calculates the program counter offset
		final String currentClassName = state.getCurrentMethodSignature().getClassName();
		final ClassHierarchy hier = state.getClassHierarchy();
		final int pcOffset;
		final Signature methodSignature;
		try {
			if (isInterface) {
				methodSignature = hier.getClassFile(currentClassName).getInterfaceMethodSignature(index);
				pcOffset = INVOKEINTERFACE_OFFSET;
			} else {
				methodSignature = hier.getClassFile(currentClassName).getMethodSignature(index);
				pcOffset = INVOKEVIRTUAL_OFFSET;
			}
		} catch (InvalidIndexException e) {
            throwVerifyError(state);
			return;
		} catch (ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		
		//performs method resolution
		final Signature methodSignatureResolved;
		try {
			methodSignatureResolved = hier.resolveMethod(currentClassName, methodSignature, isInterface);
		} catch (ClassFileNotFoundException e) {
            createAndThrow(state, NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (IncompatibleClassFileException e) {
            createAndThrow(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
			return;
		} catch (MethodAbstractException e) {
            createAndThrow(state, ABSTRACT_METHOD_ERROR);
			return;
		} catch (MethodNotFoundException e) {
            createAndThrow(state, NO_SUCH_METHOD_ERROR);
			return;
		} catch (MethodNotAccessibleException e) {
            createAndThrow(state, ILLEGAL_ACCESS_ERROR);
			return;
		}

		//checks whether the method is not static
		try {
			final ClassFile classFileResolved = hier.getClassFile(methodSignatureResolved.getClassName());
			if (classFileResolved.isMethodStatic(methodSignatureResolved)) {
	            createAndThrow(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
				return;
			}
		} catch (ClassFileNotFoundException | MethodNotFoundException e) {
			//this should never happen after resolution
			throw new UnexpectedInternalException(e);
		}

		//determines whether a meta invocation is required, 
		//and in the case delegates the responsibility to invoke it
		try {
			if (ctx.dispatcherMeta.isMeta(hier, methodSignatureResolved)) {
				final Algorithm algo = ctx.dispatcherMeta.select(methodSignatureResolved);
				algo.exec(state, ctx);
				return;
			}
		} catch (ClassFileNotFoundException | MethodNotFoundException e) {
			//this should never happen after resolution
			throw new UnexpectedInternalException();
		}
		
		//TODO initialize the class???

		//creates the method frame and sets the parameters on the operand stack
		final Value[] args = state.popMethodCallArgs(methodSignature, false);
		try {
			state.pushFrame(methodSignatureResolved, false, false, false, pcOffset, args);
		} catch (PleaseDoNativeException e) {
			ctx.nativeInvoker.doInvokeNative(state, methodSignature, args, pcOffset);
		} catch (NoMethodReceiverException e) {
            createAndThrow(state, NULL_POINTER_EXCEPTION);
		} catch (InvalidSlotException | InvalidProgramCounterException e) {
            throwVerifyError(state);
		} catch (ClassFileNotFoundException | IncompatibleClassFileException | 
				MethodNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}		
	}
}