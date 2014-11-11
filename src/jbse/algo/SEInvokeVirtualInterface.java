package jbse.algo;

import static jbse.algo.Util.ABSTRACT_METHOD_ERROR;
import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.NO_SUCH_METHOD_ERROR;
import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import static jbse.bc.Offsets.INVOKEINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;
import jbse.Util;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.bc.MethodAbstractException;
import jbse.exc.bc.MethodNotAccessibleException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.bc.NoMethodReceiverException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.FailureException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.mem.Value;


class SEInvokeVirtualInterface implements Algorithm {
	boolean isInterface;
	
	public void exec(State state, ExecutionContext ctx) 
	throws CannotManageStateException, DecisionException, 
	ThreadStackEmptyException, OperandStackEmptyException, 
	ContradictionException, FailureException, UnexpectedInternalException {
		//gets index operand from instruction word and 
		//calculates/stores the pointer to the next instruction
		final int index;
		try {
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			index = Util.byteCat(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
			state.createThrowableAndThrowIt(NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (IncompatibleClassFileException e) {
			state.createThrowableAndThrowIt(INCOMPATIBLE_CLASS_CHANGE_ERROR);
			return;
		} catch (MethodAbstractException e) {
			state.createThrowableAndThrowIt(ABSTRACT_METHOD_ERROR);
			return;
		} catch (MethodNotFoundException e) {
			state.createThrowableAndThrowIt(NO_SUCH_METHOD_ERROR);
			return;
		} catch (MethodNotAccessibleException e) {
			state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
			return;
		}

		//checks whether the method is not static
		try {
			final ClassFile classFileResolved = hier.getClassFile(methodSignatureResolved.getClassName());
			if (classFileResolved.isMethodStatic(methodSignatureResolved)) {
				state.createThrowableAndThrowIt(Util.INCOMPATIBLE_CLASS_CHANGE_ERROR);
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
			state.createThrowableAndThrowIt(NULL_POINTER_EXCEPTION);
		} catch (InvalidSlotException | InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		} catch (ClassFileNotFoundException | IncompatibleClassFileException | 
				MethodNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}		
	}
}