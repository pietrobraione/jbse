package jbse.algo;

import static jbse.bc.Offsets.INVOKEINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIAL_OFFSET;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESPECIAL;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;

import java.util.Arrays;

import jbse.Type;
import jbse.Util;
import jbse.bc.Signature;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.algo.UninterpretedUnsupportedException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.mem.Value;


class SEInvokeUninterpreted implements Algorithm {
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
		final Value[] args = state.popMethodCallArgs(methodSignatureResolved, false);
		final char returnType = Type.splitReturnValueDescriptor(methodSignatureResolved.getDescriptor()).charAt(0);
		if (Type.isPrimitive(returnType)) {
			final Primitive[] argsPrimitive;
			try {
				argsPrimitive = jbse.mem.Util.toPrimitive(opcode == OP_INVOKESTATIC ? args : Arrays.copyOfRange(args, 1, args.length));
			} catch (InvalidTypeException e) {
				throw new UninterpretedUnsupportedException("The method " + methodSignatureResolved + " has a nonprimitive argument other than 'this'."); 
			}
			try {
				state.push(state.getCalculator().applyFunction(returnType, this.functionName, argsPrimitive));
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			throw new UninterpretedUnsupportedException("The method " + methodSignatureResolved + " does not return a primitive value."); 
		}
		try {
			state.incPC(offset);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}