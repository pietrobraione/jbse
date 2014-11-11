package jbse.meta.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;
import static jbse.mem.Util.JAVA_STRING_VALUE;
import jbse.algo.Algorithm;
import jbse.bc.Signature;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Reference;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.Simplex;
import jbse.mem.State;
import jbse.mem.Util;
import jbse.mem.Value;
import jbse.meta.SymbolicValueNotAllowedException;

public class SEInvokeIsResolved implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, UnexpectedInternalException {
		try {
			final Reference fieldNameRef = (Reference) state.pop();
			final Reference objRef = (Reference) state.pop();
			
			//gets the name of the field and converts it to a string
			final Instance fieldNameInstance = (Instance) (state.getObject(fieldNameRef));
			final Reference fieldNameArrayRef = (Reference) fieldNameInstance.getFieldValue(JAVA_STRING_VALUE);
			final Array fieldNameArray = (Array) (state.getObject(fieldNameArrayRef));
			final String fieldName = fieldNameArray.valueString();
			if (fieldName == null) {
				throw new SymbolicValueNotAllowedException(fieldName);
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
			final Simplex retVal;
			final Value fieldValue = objectInstance.getFieldValue(sig);
			if (Util.isSymbolicReference(fieldValue)) {
				final ReferenceSymbolic refToBeChecked = (ReferenceSymbolic) fieldValue;
				retVal = state.getCalculator().valInt(state.resolved(refToBeChecked) ? 1 : 0);
			} else {
				retVal = state.getCalculator().valInt(1);
			}
			state.push(retVal);//Load result on the operand stack in place of the fake parameter
		} catch (Exception e) { //if any implicit pre-condition is violated
			throw new UnexpectedInternalException(e);
		}

		try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}
}
