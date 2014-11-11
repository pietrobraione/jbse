package jbse.algo;

import static jbse.algo.Util.ensureKlass;
import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;

import jbse.Util;
import jbse.bc.ClassHierarchy;
import jbse.exc.bc.ClassFileNotAccessibleException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.InvalidIndexException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

class SENew implements Algorithm {
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, DecisionException, UnexpectedInternalException {
		final int index;
		try {
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			index = Util.byteCat(tmp1,tmp2);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		final String currentClassName = state.getCurrentMethodSignature().getClassName();
        
        //performs resolution
        final ClassHierarchy hier = state.getClassHierarchy();
		final String classSignature;
		try {
			classSignature = hier.getClassFile(currentClassName).getClassSignature(index);
		} catch (InvalidIndexException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		} catch (ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
        final String classSignatureResolved;
        try {
			classSignatureResolved = hier.resolveClass(currentClassName, classSignature);
		} catch (ClassFileNotFoundException e) {
			state.createThrowableAndThrowIt(NO_CLASS_DEFINITION_FOUND_ERROR);
			return;
		} catch (ClassFileNotAccessibleException e) {
			state.createThrowableAndThrowIt(ILLEGAL_ACCESS_ERROR);
			return;
		}

		//initializes the class in case it is not
		final boolean mustExit;
		try {
			mustExit = ensureKlass(state, currentClassName, ctx.decisionProcedure);
		} catch (ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		if (mustExit) {
			//TODO this should be impossible since the current class should by definition be already initialized! Perhaps should just throw UnexpectedInternalException instead of returning.
			return;
		}

        //creates the new object in the heap
        state.push(state.createInstance(classSignatureResolved));
        
		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
	} 
}