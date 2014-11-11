package jbse.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.Util.byteCatShort;
import static jbse.algo.Util.aliases;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Null;
import jbse.mem.Reference;
import jbse.mem.State;

/**
 * Command managing all the "branch if reference comparison" bytecodes, including 
 * comparison with null (if_acmp[eq/ne], ifnull, ifnonnull). 
 * 
 * @author Pietro Braione
 *
 */
class SEIfacmp implements Algorithm {
	boolean compareWithNull;
    boolean eq;

    public void exec(State state, ExecutionContext ctx) throws UnexpectedInternalException, ThreadStackEmptyException, OperandStackEmptyException {
        //determines branch target
        int index;
        try {
	        byte tmp1 = state.getInstruction(1);
	        byte tmp2 = state.getInstruction(2);
	        index = byteCatShort(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
			return;
		}
        
        //takes operands from current frame's operand stack;        
        final Reference val2 = (Reference) state.pop();
        final Reference val1;
        if (this.compareWithNull) {
        	val1 = Null.getInstance();
        } else {
        	val1 = (Reference) state.pop();
        }
        
        //computes branch condition by comparing val1 and
        //val2 (note that both are resolved as they come
		//from the operand stack)
        boolean doJump = aliases(state, val1, val2); //also true when both are null
        if (this.eq) {
            ; //do nothing
        } else {
            doJump = ! doJump;
        }

        //performs branch if it is the case
        try {
        	if (doJump) {
        		state.incPC(index);
        	} else {
        		state.incPC(3);
        	}
        } catch (InvalidProgramCounterException e) {
        	state.createThrowableAndThrowIt(VERIFY_ERROR);
        }
    }
}