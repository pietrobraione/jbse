package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.Signature;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.Util;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;

public class Algo_JBSE_ANALYSIS_ISRESOLVED implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, SymbolicValueNotAllowedException {
	    final Reference fieldNameRef, objRef;
		try {
		    fieldNameRef = (Reference) state.popOperand();
		    objRef = (Reference) state.popOperand();
        } catch (OperandStackEmptyException e) {
            throwVerifyError(state);
            return;
        }
			
		//gets the name of the field and converts it to a string
		final String fieldName = valueString(state, fieldNameRef);
		if (fieldName == null) {
		    throw new SymbolicValueNotAllowedException("The name of the field to check must be a concrete String");
		}

		//gets, if not null, the instance
		final Instance objectInstance = (Instance) (state.getObject(objRef)); 
		Signature sig = null;
		for (Signature s : objectInstance.getFieldSignatures()) {
		    if (s.getName().equals(fieldName)) {
		        sig = s;
		        break;
		    }
		}
		
		//analyzes the field and calculates the return value
		final Simplex retVal;
		final Value fieldValue = objectInstance.getFieldValue(sig);
		if (fieldValue != null && Util.isSymbolicReference(fieldValue)) {
		    final ReferenceSymbolic refToBeChecked = (ReferenceSymbolic) fieldValue;
		    retVal = state.getCalculator().valInt(state.resolved(refToBeChecked) ? 1 : 0);
		} else {
		    retVal = state.getCalculator().valInt(1);
		}
		state.pushOperand(retVal);

		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
