package jbse.algo.nativ;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.bc.Signature;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Instance;
import jbse.mem.Null;
import jbse.mem.Reference;
import jbse.mem.State;

public class SEFillInStackTrace implements Algorithm {
	private static final Signature JAVA_LANG_THROWABLE_STACKTRACE = 
			new Signature("java/lang/Throwable", "[Ljava/lang/StackTraceElement;", "stackTrace");
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//TODO replace this dummy implementation
		final Reference thisRef = (Reference) state.pop(); //pops "this"
		final Instance exc = (Instance) state.getObject(thisRef);
		exc.setFieldValue(JAVA_LANG_THROWABLE_STACKTRACE, Null.getInstance());
		state.push(thisRef);
		
        try {
			state.incPC(INVOKEVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}
}
