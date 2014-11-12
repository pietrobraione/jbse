package jbse.algo;

import jbse.Util;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.Simplex;
import jbse.mem.State;

class SEIinc implements Algorithm {
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		final boolean wide = state.nextWide();
		final int index;
		final int constant;
		final int offset;
		try {
			if (wide) {
				final byte tmp1, tmp2, tmp3, tmp4;
				tmp1 = state.getInstruction(1);
				tmp2 = state.getInstruction(2);
				tmp3 = state.getInstruction(3);
				tmp4 = state.getInstruction(4);
				index = Util.byteCat(tmp1, tmp2);
				constant = Util.byteCat(tmp3, tmp4);
				offset = 5;
			} else {
				index = state.getInstruction(1);
				constant = state.getInstruction(2);
				offset = 3;
			}
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		try {
			final Primitive tmpVal = (Primitive) state.getLocalVariableValue(index);
			final Simplex constantSimplex = state.getCalculator().valInt(constant);
			state.setLocalVariable(index, tmpVal.add(constantSimplex));
		} catch (InvalidSlotException | InvalidOperandException | InvalidTypeException e) {
			//TODO InvalidOperandException and InvalidTypeException can also be caused by internal errors. Distinguish!
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		try {
			state.incPC(offset);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
