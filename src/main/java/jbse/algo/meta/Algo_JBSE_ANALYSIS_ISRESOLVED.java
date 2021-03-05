package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.Signature;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.Util;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;

public final class Algo_JBSE_ANALYSIS_ISRESOLVED extends Algo_INVOKEMETA_Nonbranching {
    private Simplex retVal; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException {
        Reference fieldNameRef = null, objRef = null; //to keep the compiler happy
        try {
            objRef = (Reference) this.data.operand(0);
            fieldNameRef = (Reference) this.data.operand(1);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }

        //gets the name of the field and converts it to a string
        final String fieldName = valueString(state, fieldNameRef);
        if (fieldName == null) {
            throw new SymbolicValueNotAllowedException("the jbse.meta.Analysis.isResolved method needs a concrete String as name of the field to check");
        }

        //TODO improve error detection

        //gets, if not null, the instance
        Instance objectInstance = null; //to keep the compiler happy
        try {
            objectInstance = (Instance) (state.getObject(objRef));
            if (objectInstance == null) {
                throw new SymbolicValueNotAllowedException("null has no fields"); //TODO this is not really a limitation of JBSE, rather an invalid input
            }
        } catch (ClassCastException e) {
            throw new SymbolicValueNotAllowedException("the object is not an Instance and thus has no fields"); //TODO this is not really a limitation of JBSE, rather an invalid input
        }

        //looks for the signature of the field
        Signature sig = null;
        for (Signature s : objectInstance.getStoredFieldSignatures()) {
            if (s.getName().equals(fieldName)) {
                sig = s;
                break;
            }
        }
        if (sig == null) {
            throw new SymbolicValueNotAllowedException("the specified object does not contain the specified field");
        }

        //analyzes the field and calculates the return value
        final Value fieldValue = objectInstance.getFieldValue(sig);
        if (fieldValue != null && Util.isSymbolicReference(fieldValue)) {
            final ReferenceSymbolic refToBeChecked = (ReferenceSymbolic) fieldValue;
            this.retVal = this.ctx.getCalculator().valInt(Util.isResolved(state, refToBeChecked) ? 1 : 0);
        } else {
            this.retVal = this.ctx.getCalculator().valInt(1);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.retVal);
        };
    }
}
