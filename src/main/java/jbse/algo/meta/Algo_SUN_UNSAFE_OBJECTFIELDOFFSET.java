package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.common.Type.LONG;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

public final class Algo_SUN_UNSAFE_OBJECTFIELDOFFSET extends Algo_INVOKEMETA_Nonbranching {
    private Simplex ofst; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            final Reference fldRef = (Reference) this.data.operand(1);
            final Instance fldInstance = (Instance) state.getObject(fldRef);
            final Simplex ofst = (Simplex) fldInstance.getFieldValue(JAVA_FIELD_SLOT); //we return the slot number of the field as its offset
            if (ofst == null) {
                //field not found, possibly wrong type
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            this.ofst = (Simplex) ofst.to(LONG);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }
        //TODO check that operands are concrete and kill trace if they are not
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.ofst);
        };
    }
}
