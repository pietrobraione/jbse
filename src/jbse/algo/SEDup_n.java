package jbse.algo;

import jbse.Type;
import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.mem.Value;

class SEDup_n implements Algorithm {
	/** {@code true} for dup, {@code false} for dup2. */
	boolean cat_1;

	/** {@code true} for X1, {@code false} for X2. */
	boolean x_1;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		boolean error = false;
		
		final Value tmp1 = state.pop();
		final Value tmp2 = state.pop();
		Value tmp3 = null;
		Value tmp4 = null;
		if (x_1) {
			if (cat_1) {
				//dup_x1: the two operands must be of category 1
				if (Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup_x1 semantics
					state.push(tmp1);
					state.push(tmp2);
					state.push(tmp1);
				} else {
					//dup_x1: incorrect operands
					error = true;
				}
			} else {
				if (Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup2_x1: we need a third operand
					tmp3 = state.pop();
					if (Type.isCat_1(tmp3.getType())) {
						//dup2_x1 form 1 semantics
						state.push(tmp2);
						state.push(tmp1);
						state.push(tmp3);
						state.push(tmp2);
						state.push(tmp1);
					} else {
						//dup2_x1: incorrect operand 3
						error = true;
					}
				} else if (!Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup2_x1 form 2 semantics
					state.push(tmp1);
					state.push(tmp2);
					state.push(tmp1);
				} else {
					//dup2_x1: incorrect operand 2
					error = true;
				}
			}
		} else {
			if (cat_1) {
				//dup_x2
				if (Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup_x2: we need a third operand
					tmp3 = state.pop();
					if (Type.isCat_1(tmp3.getType())) {
						//dup_x2 form 1 semantics
						state.push(tmp1);
						state.push(tmp3);
						state.push(tmp2);
						state.push(tmp1);						
					} else {
						//dup_x2: incorrect operand 3
						error = true;
					}
				} else if (Type.isCat_1(tmp1.getType()) && !Type.isCat_1(tmp2.getType())) {
					//dup_x2 form 2 semantics
					state.push(tmp1);
					state.push(tmp2);
					state.push(tmp1);
				} else {
					//dup_x2: incorrect operand 1
					error = true;
				}
			} else {
				//dup2_x2
				if (Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup2_x2: we need a third operand
					tmp3 = state.pop();
					if (Type.isCat_1(tmp3.getType())) {
						//dup2_x2: we need a fourth operand
						tmp4 = state.pop();
						if (Type.isCat_1(tmp4.getType())) {
							//dup2_x2 form 1 semantics
							state.push(tmp2);
							state.push(tmp1);
							state.push(tmp4);
							state.push(tmp3);
							state.push(tmp2);
							state.push(tmp1);
						} else {
							//dup2_x2: incorrect operand 4
							error = true;
						}
					} else {
						//dup2_x2 form 3 semantics
						state.push(tmp2);
						state.push(tmp1);
						state.push(tmp3);
						state.push(tmp2);
						state.push(tmp1);
					}
				} else if (!Type.isCat_1(tmp1.getType()) && Type.isCat_1(tmp2.getType())) {
					//dup2_x2: we need a third operand
					tmp3 = state.pop();
					if (Type.isCat_1(tmp1.getType())) {
						//dup2_x2 form 2 semantics
						state.push(tmp1);
						state.push(tmp3);
						state.push(tmp2);
						state.push(tmp1);
					} else {
						//dup2_x2: incorrect operand 3
						error = true;
					}
				} else if (!Type.isCat_1(tmp1.getType()) && !Type.isCat_1(tmp2.getType())) {
					//dup2_x2 form 4 semantics					
					state.push(tmp1);
					state.push(tmp2);
					state.push(tmp1);
				} else {
					//dup2_x2: incorrect operand 2
					error = true;
				}
			}
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			error = true;
		}

		//common updates
		if (error) {
	    	state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
