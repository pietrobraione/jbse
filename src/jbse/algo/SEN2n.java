package jbse.algo;

import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.mem.Value;

/**
 * Command implementing all the *2* bytecodes (i2[b/s/l/f/d/c], l2[i/f/d], f2[i/l/d], d2[i/l/f]).
 * 
 * @author Pietro Braione
 *
 */
class SEN2n implements Algorithm {
	char type;
	char castType;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
		//pops the value on the operand stack
		final Value value = state.pop();
		
		//casts the value
		final Value castValue;
		if (value instanceof Primitive) {
			try {
				castValue = ((Primitive) value).to(castType);
			} catch (InvalidTypeException e) {
				state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				return;
			}
		} else {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		//pushes the obtained value
		state.push(castValue);

		//increments the program counter
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	} 
}