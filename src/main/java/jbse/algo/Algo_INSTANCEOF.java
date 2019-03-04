package jbse.algo;

import jbse.tree.DecisionAlternative_NONE;

/**
 * {@link Algorithm} implementing the instanceof bytecode.
 *  
 * @author Pietro Braione
 */
final class Algo_INSTANCEOF extends Algo_CASTINSTANCEOF {
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            if (!this.isNull && this.isSubclass) { //TODO does the this.isSubclass check conform to the specification of the instanceof bytecode in the JVMS v8?
                state.pushOperand(this.ctx.getCalculator().valInt(1));
            } else { 
                state.pushOperand(this.ctx.getCalculator().valInt(0));
            }
        };
    }
}
