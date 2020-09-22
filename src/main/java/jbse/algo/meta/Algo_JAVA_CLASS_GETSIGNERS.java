package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Class#setSigners()}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JAVA_CLASS_GETSIGNERS extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete signers;  //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException {
        try {           
            //gets the 'this' object
            final Reference classRef = (Reference) this.data.operand(0);
            if (state.isNull(classRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getSigners method is null.");
            }
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            this.signers = clazz.getSigners();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.signers == null ? Null.getInstance() : this.signers);
        };
    }
}
