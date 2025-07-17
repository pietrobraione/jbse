package jbse.algo;

import jbse.tree.DecisionAlternative_CASTINSTANCEOF;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_IsSubclass;

/**
 * {@link Algorithm} implementing the instanceof bytecode.
 *  
 * @author Pietro Braione
 */
final class Algo_INSTANCEOF extends Algo_CASTINSTANCEOF {
    protected StrategyUpdate<DecisionAlternative_CASTINSTANCEOF> updater() {
        return (state, alt) -> {
            if (alt instanceof DecisionAlternative_CASTINSTANCEOF_IsSubclass) {
                state.pushOperand(this.ctx.getCalculator().valInt(1));
            } else { 
                state.pushOperand(this.ctx.getCalculator().valInt(0));
            }
        };
    }
}
