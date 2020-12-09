package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#metaThrowUnexpectedInternalException(String)} and
 * {@link jbse.base.JAVA_CONCURRENTMAP#metaThrowUnexpectedInternalException(String)}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_JAVA_XMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets the "message" parameter
            final Reference msgReference = (Reference) this.data.operand(0);
            if (state.isNull(msgReference)) {
                //this should never happen
                failExecution("The 'message' parameter to jbse.base.JAVA_[CONCURRENT]MAP.metaThrowUnexpectedInternalException method is null.");
            }
            final String msg = valueString(state, msgReference);
            if (msg == null) {
                throw new SymbolicValueNotAllowedException("The 'message' parameter to jbse.base.JAVA_[CONCURRENT]MAP.metaThrowUnexpectedInternalException method is not a simple String.");
            }
            
            //throws
            failExecution(msg);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	//nothing to do, this is unreachable
        };
    }
}
