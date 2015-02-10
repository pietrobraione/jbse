package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

final class Algo_IINC implements Algorithm {
    
    @Override
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
            throwVerifyError(state);
			return;
		}
		
		try {
			final Primitive tmpVal = (Primitive) state.getLocalVariableValue(index);
			final Simplex constantSimplex = state.getCalculator().valInt(constant);
			state.setLocalVariable(index, tmpVal.add(constantSimplex));
		} catch (InvalidSlotException | InvalidOperandException | InvalidTypeException e) {
			//TODO InvalidOperandException and InvalidTypeException can also be caused by internal errors. Distinguish!
            throwVerifyError(state);
			return;
		}
		
		try {
			state.incPC(offset);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
