package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XLOADSTORE_WIDE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_OPCODE_OFFSET;

import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

/**
 * Command managing all the *load* (load from local variable) bytecodes 
 * (aload[_0/1/2/3], dload[_0/1/2/3], fload[_0/1/2/3], iload[_0/1/2/3], 
 * lload[_0/1/2/3]). It decides over the value loaded 
 * to the operand stack in the case (bytecodes aload[_0/1/2/3]) this 
 * value is a symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */

final class SELoad extends MultipleStateGeneratorLFLoad implements Algorithm {
    boolean def;
    int index;

    public SELoad() { }
    
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException,  UnexpectedInternalException {
    	boolean wide = state.nextWide();

        //gets the index of the local variable and 
        //calculates the program counter offset for
        //the next instruction
        final int ofst;
        try {
            if (this.def) {
            	//index already initialized by constructor
            	ofst = XLOADSTORE_OPCODE_OFFSET;
            } else if (wide) {
            	byte tmp1 = state.getInstruction(1);
            	byte tmp2 = state.getInstruction(2);
            	this.index = Util.byteCat(tmp1, tmp2);
            	ofst = XLOADSTORE_WIDE_OFFSET;
            } else {
            	this.index = state.getInstruction(1);
            	ofst = XLOADSTORE_IMMEDIATE_OFFSET;
            }
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

        //gets the value from the index
        final Value tmpValue;
		try {
			tmpValue = state.getLocalVariableValue(this.index);
		} catch (InvalidSlotException e) {
            throwVerifyError(state);
			return;
		}

        //generates all the next states
       	this.state = state;
       	this.ctx = ctx;
       	this.pcOffset = ofst;
   		this.valToLoad = tmpValue;
   		generateStates();
   } 
}