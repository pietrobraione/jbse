package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.MULTIANEWARRAY_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;

/**
 * 
 * @author Pietro Braione
 *
 */
final class Algo_MULTIANEWARRAY extends MultipleStateGenerator_XNEWARRAY implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws DecisionException, ContradictionException, 
	ThreadStackEmptyException, OperandStackEmptyException, 
	UnexpectedInternalException  {
		//gets the number of dimensions and the constant pool index
		final int ndims;
		final int index;
		try {
			byte tmp1 = state.getInstruction(1);
			byte tmp2 = state.getInstruction(2); 
			index = Util.byteCat(tmp1, tmp2);
			ndims = state.getInstruction(3);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
		final ClassHierarchy hier = state.getClassHierarchy();
		final String currentClassName = state.getCurrentMethodSignature().getClassName();    
		
		//gets the signature of the array type
		final String arraySignature;
		try {
			arraySignature = hier.getClassFile(currentClassName).getClassSignature(index);
		} catch (InvalidIndexException e) {
            throwVerifyError(state);
			return;
		} catch (ClassFileNotFoundException e) {
			//this must never happen
			throw new UnexpectedInternalException(e);
		}

		//gets the type of the (innermost layer's) array members
		String memberType = Type.getArrayMemberType(arraySignature);
		while (Type.isArray(memberType)) {
			memberType = Type.getArrayMemberType(memberType);
		}

		//(possibly) resolves the array signature
    	if (Type.isReference(memberType)) {
    		try {
				hier.resolveClass(currentClassName, arraySignature);
    		} catch (ClassFileNotFoundException e) {
                createAndThrowObject(state, NO_CLASS_DEFINITION_FOUND_ERROR);
    			return;
    		} catch (ClassFileNotAccessibleException e) {
                createAndThrowObject(state, ILLEGAL_ACCESS_ERROR);
    			return;
    		}
    	}

		//checks the number of instantiation dimensions
		if (ndims <= 0) { //TODO check that no error arise because of incorrect byte concatenation interpreted as negative
            throwVerifyError(state);
			return;
		}

		//generates the next states
    	this.state = state;
    	this.ctx = ctx;
    	this.pcOffset = MULTIANEWARRAY_OFFSET;
		this.dimensionsCounts = new Primitive[ndims];
		for (int i = ndims - 1; i >= 0; --i) {
			this.dimensionsCounts[i] = (Primitive) state.pop();
			//TODO length type check
		}
    	this.arrayType = arraySignature;
    	this.generateStates();
	}
}