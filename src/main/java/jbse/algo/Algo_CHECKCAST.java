package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;

import jbse.mem.HeapObjekt;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_IsSubclass;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_Null;
import jbse.val.Reference;

/**
 * {@link Algorithm} implementing the checkcast bytecode.
 *  
 * @author Pietro Braione
 */
final class Algo_CHECKCAST extends Algo_CASTINSTANCEOF {
    @Override
    protected StrategyUpdate<DecisionAlternative_CASTINSTANCEOF> updater() {
        return (state, alt) -> {
        	if (alt instanceof DecisionAlternative_CASTINSTANCEOF_IsSubclass || alt instanceof DecisionAlternative_CASTINSTANCEOF_Null) {
                //gets the operand
                final Reference referenceObj = (Reference) this.data.operand(0);

        		if (alt instanceof DecisionAlternative_CASTINSTANCEOF_IsSubclass && ((DecisionAlternative_CASTINSTANCEOF_IsSubclass) alt).refine()) {
                    final HeapObjekt obj = state.getObject(referenceObj);
                    obj.refine(this.ctx.getCalculator(), this.classCast, state);
        		}
        		
                try {
                    state.pushOperand(referenceObj);
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