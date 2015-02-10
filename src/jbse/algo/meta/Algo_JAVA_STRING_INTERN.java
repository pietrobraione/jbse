package jbse.algo.meta;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotInvokeNativeException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class Algo_JAVA_STRING_INTERN implements Algorithm {
    public Algo_JAVA_STRING_INTERN() {
        //nothing to do
    }

    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, CannotInvokeNativeException, DecisionException, ClasspathException {
        try {
            final String valueString = valueString(state, (Reference) state.pop());
            if (valueString == null) {
                throw new CannotInvokeNativeException("cannot intern a String with symbolic value");
            }
            if (state.hasStringLiteral(valueString)) {
                //nothing to do
            } else {
                ensureStringLiteral(state, valueString, ctx.decisionProcedure);
            }
            state.push(state.referenceToStringLiteral(valueString));
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
        }
        
        try {
            state.incPC(INVOKEVIRTUAL_OFFSET);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
        }
    }
}
