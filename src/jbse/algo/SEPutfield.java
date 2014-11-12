package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.NO_SUCH_FIELD_ERROR;
import static jbse.algo.Util.NULL_POINTER_EXCEPTION;

import jbse.Util;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.FieldNotAccessibleException;
import jbse.exc.bc.FieldNotFoundException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Instance;
import jbse.mem.Reference;
import jbse.mem.State;
import jbse.mem.Value;

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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}

		//gets the field signature from the current class constant pool
        final String currentClassName = state.getCurrentMethodSignature().getClassName();
        final ClassHierarchy hier = state.getClassHierarchy();
        final Signature fieldSignature;
        try {
			fieldSignature = hier.getClassFile(currentClassName).getFieldSignature(index);
		} catch (InvalidIndexException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
			state.createThrowableAndThrowIt(NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (FieldNotFoundException e) {
			state.createThrowableAndThrowIt(NO_SUCH_FIELD_ERROR);
			return;
		} catch (FieldNotAccessibleException e) {
			state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
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
				state.createThrowableAndThrowIt(INCOMPATIBLE_CLASS_CHANGE_ERROR);
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
				state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
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
        	state.createThrowableAndThrowIt(NULL_POINTER_EXCEPTION);
	    	return;
        }
		final Instance myObject = (Instance) state.getObject(ref);
		myObject.setFieldValue(fieldSignatureResolved, valueToPut);

		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
	} 
}
