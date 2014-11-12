package jbse.meta.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;

import jbse.algo.Algorithm;
import jbse.bc.Signature;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Reference;
import jbse.mem.State;

public class SEInvokeAssertRepOk implements Algorithm {

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//pops the parameters and stores them in ctx
		//TODO store them elsewhere and eliminate the dependence of Run from ExecutionContext
		final Reference methodNameRef = (Reference) state.pop();
		ctx.repOkMethodName = valueString(state, methodNameRef);
		ctx.repOkTargetObjectReference = (Reference) state.pop();

        try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}


	private static final Signature SIG_STRING_ARRAY = new Signature("java/lang/String", "[C", "value");
	
    private final String valueString(State s, Reference ref) {
    	final Instance i = (Instance) s.getObject(ref);
    	if (i.getType().equals("java/lang/String")) {
    		final Reference valueRef = (Reference) i.getFieldValue(SIG_STRING_ARRAY);
    		final Array value = (Array) s.getObject(valueRef);
    		return value.valueString();
    	} else {
    		return null;
    	}
    }
}
