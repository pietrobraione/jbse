package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;

class SEPutfield implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//gets the index of the field signature in the current class 
    	//constant pool
		final int index;
		try {
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			index = Util.byteCat(tmp1,tmp2);
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
            createAndThrowObject(state, NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (FieldNotFoundException e) {
            createAndThrowObject(state,NO_SUCH_FIELD_ERROR);
			return;
		} catch (FieldNotAccessibleException e) {
            createAndThrowObject(state, ILLEGAL_ACCESS_ERROR);
			return;
		}

		//gets resolved field's data
		final String fieldClassName = fieldSignatureResolved.getClassName();        
		final ClassFile fieldClassFile;
		try {
			fieldClassFile = hier.getClassFile(fieldClassName);
		} catch (ClassFileNotFoundException e) {
			//this should never happen after field resolution
			throw new UnexpectedInternalException(e);
		}

		//checks that the field is not static
		try {
			if (fieldClassFile.isFieldStatic(fieldSignatureResolved)) {
	            createAndThrowObject(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
				return;
			}
		} catch (FieldNotFoundException e) {
			//this should never happen after field resolution
			throw new UnexpectedInternalException(e);
		}

        //checks that if the field is final is declared in the current class
        try {
			if (fieldClassFile.isFieldFinal(fieldSignatureResolved) &&
				!fieldClassName.equals(currentClassName)) {
	            createAndThrowObject(state, ILLEGAL_ACCESS_ERROR);
				return;
			}
		} catch (FieldNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}

        //sets the field's value
		final Value valueToPut = state.pop();
		final Reference ref = (Reference) state.pop();
        if (state.isNull(ref)) {
            createAndThrowObject(state, NULL_POINTER_EXCEPTION);
	    	return;
        }
		final Instance myObject = (Instance) state.getObject(ref);
		myObject.setFieldValue(fieldSignatureResolved, valueToPut);

		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
	} 
}
