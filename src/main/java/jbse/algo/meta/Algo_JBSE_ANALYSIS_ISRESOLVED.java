package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.Signature;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.Util;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;

public final class Algo_JBSE_ANALYSIS_ISRESOLVED extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void update(State state) 
    throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
    InterruptException {
        Reference fieldNameRef = null, objRef = null; //to keep the compiler happy
        try {
            objRef = (Reference) this.data.operand(0);
            fieldNameRef = (Reference) this.data.operand(1);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //gets the name of the field and converts it to a string
        final String fieldName = valueString(state, fieldNameRef);
        if (fieldName == null) {
            throw new SymbolicValueNotAllowedException("The method needs a concrete String as name of the field to check.");
        }

        //TODO improve error detection

        //gets, if not null, the instance
        Instance objectInstance = null; //to keep the compiler happy
        try {
            objectInstance = (Instance) (state.getObject(objRef));
            if (objectInstance == null) {
                throw new SymbolicValueNotAllowedException("Null has no fields."); //TODO this is not really a limitation of JBSE, rather an invalid input
            }
        } catch (ClassCastException e) {
            throw new SymbolicValueNotAllowedException("The object is not an Instance and thus has no fields."); //TODO this is not really a limitation of JBSE, rather an invalid input
        }

        //looks for the signature of the field
        Signature sig = null;
        for (Signature s : objectInstance.getFieldSignatures()) {
            if (s.getName().equals(fieldName)) {
                sig = s;
                break;
            }
        }
        if (sig == null) {
            throw new SymbolicValueNotAllowedException("The specified object does not contain the specified field.");
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

        //pushes it
        state.pushOperand(retVal);
    }
}
