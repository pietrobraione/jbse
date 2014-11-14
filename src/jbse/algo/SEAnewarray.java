package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.ANEWARRAY_OFFSET;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;

/**
 * Class for completing the semantics of the anewarray (new array
 * of references) bytecode. It decides on the resolution
 * of a symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 *
 */
final class SEAnewarray extends MultipleStateGeneratorNewarray implements Algorithm {
    public void exec(State state, ExecutionContext ctx) 
    throws DecisionException, OperandStackEmptyException, ThreadStackEmptyException {
    	//gets the constant pool index
    	final int index;
    	try {
	        index = Util.byteCat(state.getInstruction(1), state.getInstruction(2));
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
			return;
		}
        final ClassHierarchy hier = state.getClassHierarchy();
		final String currentClassName = state.getCurrentMethodSignature().getClassName();
		
		//gets the signature of the array type
		final String arraySignature;
		try {
			arraySignature = "" + Type.ARRAYOF + Type.REFERENCE +
						hier.getClassFile(currentClassName).getClassSignature(index) +
						Type.TYPEEND;
		} catch (InvalidIndexException e) {
			throwVerifyError(state);
			return;
		} catch (ClassFileNotFoundException e) {
			//this must never happen
			throw new UnexpectedInternalException(e);
		}

		//resolves the array signature
        final String arraySignatureResolved;
		try {
			arraySignatureResolved = hier.resolveClass(currentClassName, arraySignature);
		} catch (ClassFileNotFoundException e) {
			createAndThrow(state, NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (ClassFileNotAccessibleException e) {
			createAndThrow(state, ILLEGAL_ACCESS_ERROR);
			return;
		}
		
        //pops the array's length from the operand stack
        final Primitive length = (Primitive) state.pop();
		//TODO length type check

		//generates the next states
    	this.state = state;
    	this.ctx = ctx;
    	this.pcOffset = ANEWARRAY_OFFSET;
    	this.dimensionsCounts = new Primitive[] { length };
        this.arrayType = arraySignatureResolved;
    	this.generateStates();
    } 
}