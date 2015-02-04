package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.NO_SUCH_FIELD_ERROR;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.ensureKlass;
import static jbse.algo.Util.throwVerifyError;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

/**
 * Command managing the getstatic bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class SEGetstatic extends MultipleStateGeneratorLFLoad implements Algorithm {
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException {
        //gets the index of the field signature in the current class 
    	//constant pool
        final int index;
        try {
        	final byte tmp1 = state.getInstruction(1);
        	final byte tmp2 = state.getInstruction(2);
	        index = Util.byteCat(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

		//gets the field signature from the current class constant pool
        final String currentClassName = state.getCurrentMethodSignature().getClassName();
        final ClassHierarchy hier = state.getClassHierarchy();
        final Signature fieldSignature;
        try {
			fieldSignature = hier.getClassFile(currentClassName).getFieldSignature(index);
		} catch (InvalidIndexException e) {
            throwVerifyError(state);
			return;
		} catch (ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}

		//performs field resolution
        final Signature fieldSignatureResolved;
		try {
	        fieldSignatureResolved = hier.resolveField(currentClassName, fieldSignature);
		} catch (ClassFileNotFoundException e) {
            createAndThrow(state, NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (FieldNotFoundException e) {
            createAndThrow(state, NO_SUCH_FIELD_ERROR);
			return;
		} catch (FieldNotAccessibleException e) {
            createAndThrow(state, ILLEGAL_ACCESS_ERROR);
			return;
		}

		//gets resolved field's class
		final String fieldClassName = fieldSignatureResolved.getClassName();    
		
		//checks the field
		try {
		    final ClassFile fieldClassFile = hier.getClassFile(fieldClassName);
			//checks that the field is static or belongs to an interface
			if ((!fieldClassFile.isInterface()) && (!fieldClassFile.isFieldStatic(fieldSignatureResolved))) {
	            createAndThrow(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
				return;
			}
		} catch (ClassFileNotFoundException | FieldNotFoundException e) {
			//this should never happen after field resolution
			throw new UnexpectedInternalException(e);
		}
		
		//performs class initialization
		final boolean mustExit;
		try {
		    mustExit = ensureKlass(state, fieldClassName, ctx.decisionProcedure);
		} catch (ClassFileNotFoundException e) {
		    throw new UnexpectedInternalException(e);
		}
		if (mustExit) {
		    return;
		    //now the execution continues with the class 
		    //initialization code; the current bytecode will 
		    //be reexecuted after that
		}
		
		//gets the field's value 
        final Value tmpValue = state.getKlass(fieldClassName).getFieldValue(fieldSignatureResolved);
        
        //generates all the next states
    	this.state = state;
    	this.ctx = ctx;
    	this.pcOffset = 3;
    	this.valToLoad = tmpValue;
    	generateStates();
    } 
}
