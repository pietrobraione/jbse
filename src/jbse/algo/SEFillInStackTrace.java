package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;

import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;
import jbse.val.Reference;

public class SEFillInStackTrace implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//TODO replace this dummy implementation
		final Reference thisRef = (Reference) state.pop(); //pops "this"
		final Instance exc = (Instance) state.getObject(thisRef);
		exc.setFieldValue(JAVA_THROWABLE_STACKTRACE, Null.getInstance());
		state.push(thisRef);
		
        try {
			state.incPC(INVOKEVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
