package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Offsets.ANEWARRAY_OFFSET;
import jbse.Type;
import jbse.Util;
import jbse.bc.ClassHierarchy;
import jbse.exc.bc.ClassFileNotAccessibleException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.State;

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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
			state.createThrowableAndThrowIt(NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (ClassFileNotAccessibleException e) {
			state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
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