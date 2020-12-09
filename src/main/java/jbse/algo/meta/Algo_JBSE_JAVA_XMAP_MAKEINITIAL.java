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
import jbse.dec.exc.DecisionException;
import jbse.mem.HeapObjekt;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#makeInitial(Object)} and 
 * {@link jbse.base.JAVA_CONCURRENTMAP#makeInitial(Object)}.
 *  
 * @author Pietro Braione
 *
 */
//TODO this is really ugly! Possibly delete and do as with arrays, i.e., all objects are created symbolic/initial upon construction and never need to change from concrete to symbolic and to noninitial to initial.
public final class Algo_JBSE_JAVA_XMAP_MAKEINITIAL extends Algo_INVOKEMETA_Nonbranching {
	private Reference thisRef; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets the "this" object
            this.thisRef = (Reference) this.data.operand(0);
            if (state.isNull(this.thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to jbse.base.JAVA_[CONCURRENT]MAP.makeInitial method is null.");
            }
            final Objekt thisObj = state.getObject(this.thisRef);
            if (thisObj == null) {
                //this should never happen
                failExecution("The 'this' parameter to jbse.base.JAVA_[CONCURRENT]MAP.makeInitial method is symbolic and unresolved.");
            }            
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final HeapObjekt thisObj = state.getObject(this.thisRef);
        	thisObj.makeInitial();
        };
    }
}
