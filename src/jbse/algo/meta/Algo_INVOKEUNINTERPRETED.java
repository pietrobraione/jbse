package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;
import static jbse.mem.Util.toPrimitive;

import java.util.Arrays;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.UninterpretedUnsupportedException;
import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

final class Algo_INVOKEUNINTERPRETED implements Algorithm {
	Signature methodSignatureResolved;
	String functionName;
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws CannotManageStateException, ThreadStackEmptyException, 
	DecisionException, ContradictionException {		
		//gets index operand from instruction word and 
		//calculates/stores the pointer to the next instruction
		final int opcode = state.getInstruction();
		final int offset = (
				opcode == OP_INVOKEINTERFACE ? INVOKEDYNAMICINTERFACE_OFFSET : 
				INVOKESPECIALSTATICVIRTUAL_OFFSET); //TODO invokedynamic

		final char returnType = Type.splitReturnValueDescriptor(this.methodSignatureResolved.getDescriptor()).charAt(0);
		if (Type.isPrimitive(returnType)) {
	        //pops the args and checks that they are all primitive
			final Primitive[] argsPrimitive;
			try {
	            final Value[] args = state.popMethodCallArgs(this.methodSignatureResolved, false);
				argsPrimitive = toPrimitive(opcode == OP_INVOKESTATIC ? args : Arrays.copyOfRange(args, 1, args.length));
            } catch (OperandStackEmptyException e) {
                throwVerifyError(state);
                return;
			} catch (InvalidTypeException e) {
				throw new UninterpretedUnsupportedException("The method " + this.methodSignatureResolved + " has a nonprimitive argument other than 'this'."); 
			}

			//pushes the uninterpreted function term
			try {
				state.pushOperand(state.getCalculator().applyFunction(returnType, this.functionName, argsPrimitive));
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			throw new UninterpretedUnsupportedException("The method " + this.methodSignatureResolved + " does not return a primitive value."); 
		}
		
		try {
			state.incPC(offset);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}