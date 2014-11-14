package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.ClassFile;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.ConstantPoolString;
import jbse.val.Value;

final class SELdc implements Algorithm {
	boolean def;

	public SELdc() { }
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException  {
		int index;
		try {
			byte tmp1 = state.getInstruction(1);
			if (this.def) {
				index = Util.byteCat((byte) 0, tmp1);
			} else {
				byte tmp2 = state.getInstruction(2);
				index = Util.byteCat(tmp1, tmp2);
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

		//gets the current class and pushes the value stored 
		//at index
		final String currentClassName = state.getCurrentMethodSignature().getClassName();
		final ClassFile cf;
		try {
			cf = state.getClassHierarchy().getClassFile(currentClassName);
		} catch (ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		
		Value val;
		try {
			val = state.getCalculator().val_(cf.getValueFromConstantPool(index));
		} catch (InvalidIndexException e) {
            throwVerifyError(state);
			return;
		}
		if (val instanceof ConstantPoolString) {
			val = state.referenceToStringLiteral(val.toString());
		}

		//pushes the value on the operand stack
		state.push(val);
		
		try {
			if (this.def) {
				state.incPC(2);
			} else {
				state.incPC(3);
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	} 
}
