package jbse.algo.nativ;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotInvokeNativeException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class SEIntern implements Algorithm {
    public SEIntern() {
        //nothing to do
    }

    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, CannotInvokeNativeException, DecisionException, ClasspathException {
        try {
            final Objekt string = (Objekt) state.getObject((Reference) state.pop());
            if (string.isSymbolic()) {
                throw new CannotInvokeNativeException("cannot invoke java.lang.String.intern() on a symbolic String instance");
            }
            final Array arrayOfChars = (Array) state.getObject((Reference) string.getFieldValue(JAVA_STRING_VALUE));
            final String stringValue = arrayOfChars.valueString();
            if (state.hasStringLiteral(stringValue)) {
                //nothing to do
            } else {
                ensureStringLiteral(state, stringValue, ctx.decisionProcedure);
            }
            state.push(state.referenceToStringLiteral(stringValue));
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
