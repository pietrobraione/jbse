package jbse.algo.meta;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class Algo_JAVA_STRING_INTERN implements Algorithm {
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
    DecisionException, ClasspathException, InterruptException {
        try {
            final String valueString = valueString(state, (Reference) state.popOperand());
            if (valueString == null) {
                //TODO remove this limitation
                throw new SymbolicValueNotAllowedException("cannot intern a String with symbolic value");
            }
            if (state.hasStringLiteral(valueString)) {
                //nothing to do
            } else {
                ensureStringLiteral(state, valueString, ctx.decisionProcedure);
            }
            state.pushOperand(state.referenceToStringLiteral(valueString));
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            throw new InterruptException();
        }
        
        try {
            state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
        }
        throw new InterruptException();
    }
}
