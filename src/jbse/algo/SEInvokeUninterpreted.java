package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIAL_OFFSET;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESPECIAL;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;

import java.util.Arrays;

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

final class SEInvokeUninterpreted implements Algorithm {
	Signature methodSignatureResolved;
	String functionName;
	
	public SEInvokeUninterpreted() { }
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws CannotManageStateException, ThreadStackEmptyException, OperandStackEmptyException, 
	DecisionException, ContradictionException {		
		//gets index operand from instruction word and 
		//calculates/stores the pointer to the next instruction
		final int opcode = state.getInstruction();
		final int offset = (
				opcode == OP_INVOKEINTERFACE ? INVOKEINTERFACE_OFFSET :
				opcode == OP_INVOKESPECIAL ? INVOKESPECIAL_OFFSET :
				opcode == OP_INVOKESTATIC ? INVOKESTATIC_OFFSET :
				INVOKEVIRTUAL_OFFSET); //opcode == OP_INVOKEVIRTUAL

		//pops the args and pushes the uninterpreted function symbol 
		final Value[] args = state.popMethodCallArgs(this.methodSignatureResolved, false);
		final char returnType = Type.splitReturnValueDescriptor(this.methodSignatureResolved.getDescriptor()).charAt(0);
		if (Type.isPrimitive(returnType)) {
			final Primitive[] argsPrimitive;
			try {
				argsPrimitive = jbse.mem.Util.toPrimitive(opcode == OP_INVOKESTATIC ? args : Arrays.copyOfRange(args, 1, args.length));
			} catch (InvalidTypeException e) {
				throw new UninterpretedUnsupportedException("The method " + this.methodSignatureResolved + " has a nonprimitive argument other than 'this'."); 
			}
			try {
				state.push(state.getCalculator().applyFunction(returnType, this.functionName, argsPrimitive));
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