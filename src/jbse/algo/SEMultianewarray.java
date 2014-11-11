package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Offsets.MULTIANEWARRAY_OFFSET;

import jbse.Type;
import jbse.Util;
import jbse.bc.ClassHierarchy;
import jbse.exc.bc.ClassFileNotAccessibleException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.State;

/**
 * 
 * @author Pietro Braione
 *
 */
final class SEMultianewarray extends MultipleStateGeneratorNewarray implements Algorithm {
	
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		final ClassHierarchy hier = state.getClassHierarchy();
		final String currentClassName = state.getCurrentMethodSignature().getClassName();    
		
		//gets the signature of the array type
		final String arraySignature;
		try {
			arraySignature = hier.getClassFile(currentClassName).getClassSignature(index);
		} catch (InvalidIndexException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
		final String arraySignatureResolved;
    	if (Type.isPrimitive(memberType)) {
    		arraySignatureResolved = arraySignature;
    	} else {
    		try {
				arraySignatureResolved = hier.resolveClass(currentClassName, arraySignature);
    		} catch (ClassFileNotFoundException e) {
    			state.createThrowableAndThrowIt(NO_CLASS_DEFINITION_FOUND_ERROR);
    			return;
    		} catch (ClassFileNotAccessibleException e) {
    			state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
    			return;
    		}
    	}

		//checks the number of instantiation dimensions
		if (ndims <= 0) { //TODO check that no error arise because of incorrect byte concatenation interpreted as negative
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
    	this.arrayType = arraySignatureResolved;
    	this.generateStates();
	}
}