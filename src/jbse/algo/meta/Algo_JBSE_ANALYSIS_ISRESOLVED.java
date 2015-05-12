package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.Signature;
import jbse.mem.Instance;
import jbse.mem.Util;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;

public final class Algo_JBSE_ANALYSIS_ISRESOLVED extends Algo_INVOKEMETA {
    public Algo_JBSE_ANALYSIS_ISRESOLVED() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
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
                    throw new SymbolicValueNotAllowedException("The method needs a nonnull object as the object where to search the field.");
                }
            } catch (ClassCastException e) {
                throw new SymbolicValueNotAllowedException("The method needs an Instance as the object where to search the field.");
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
        };
    }
}
