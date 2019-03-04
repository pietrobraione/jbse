package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;

import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;

/**
 * {@link Algorithm} implementing the checkcast bytecode.
 *  
 * @author Pietro Braione
 */
final class Algo_CHECKCAST extends Algo_CASTINSTANCEOF {
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            if (this.isSubclass) { //TODO does the this.isSubclass check conform to the specification of the checkcast bytecode in the JVMS v8?
                try {
                    state.pushOperand(this.data.operand(0));
                } catch (ThreadStackEmptyException e) {
                    //this should never happen
                    failExecution(e);
                }
            } else {
                throwNew(state, this.ctx.getCalculator(), CLASS_CAST_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }
}