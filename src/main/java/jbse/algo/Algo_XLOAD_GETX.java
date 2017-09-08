package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Resolved;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract {@link Algorithm} for completing the semantics of the 
 * *load*, *load and get* bytecodes ([a/d/f/i/l]load[_0/1/2/3], 
 * [a/d/f/i/l]load, get[field/static]). 
 * It decides over the value loaded to the operand stack in the cases 
 * (aload[_0/1/2/3], aload, get[field/static]) 
 * it is an uninitialized symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
abstract class Algo_XLOAD_GETX<D extends BytecodeData> extends Algo_XYLOAD_GETX<
D, 
DecisionAlternative_XLOAD_GETX,
StrategyDecide<DecisionAlternative_XLOAD_GETX>, 
StrategyRefine_XLOAD_GETX,
StrategyUpdate<DecisionAlternative_XLOAD_GETX>> {

    //set by subclasses
    protected Value valToLoad;

    @Override
    protected final Class<DecisionAlternative_XLOAD_GETX> classDecisionAlternative() {
        return DecisionAlternative_XLOAD_GETX.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_XLOAD_GETX> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.resolve_XLOAD_GETX(state, this.valToLoad, result);
            this.someRefNotExpanded = o.noReferenceExpansion();
            if (this.someRefNotExpanded) {
                try {
                    final ReferenceSymbolic refToLoad = (ReferenceSymbolic) this.valToLoad;
                    this.nonExpandedRefTypes = refToLoad.getStaticType();
                    this.nonExpandedRefOrigins = refToLoad.getOrigin().toString();
                } catch (ClassCastException e) {
                    throw new UnexpectedInternalException(e);
                }
            }
            return o;
        };
    }

    @Override
    protected final StrategyRefine_XLOAD_GETX refiner() {
        return new StrategyRefine_XLOAD_GETX() {
            @Override
            public void refineRefExpands(State s, DecisionAlternative_XLOAD_GETX_Expands alt) 
            throws ContradictionException, InvalidTypeException {
                Algo_XLOAD_GETX.this.refineRefExpands(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineRefAliases(State s, DecisionAlternative_XLOAD_GETX_Aliases alt)
            throws ContradictionException {
                Algo_XLOAD_GETX.this.refineRefAliases(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineRefNull(State s, DecisionAlternative_XLOAD_GETX_Null alt)
            throws ContradictionException {
                Algo_XLOAD_GETX.this.refineRefNull(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineResolved(State s, DecisionAlternative_XLOAD_GETX_Resolved alt) {
                //nothing to do, the value is concrete or has been already refined
            }
        };
    }

    @Override
    protected final Value possiblyMaterialize(State s, Value val) {
        //nothing to do, val does not come from an array
        return val;
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_XLOAD_GETX> updater() {
        return this::update;
    }

    @Override
    protected final void onInvalidInputException(State state, InvalidInputException e) {
        //bad value to load (triggered by call to resolve_XLOAD_GETX done by decider())
        throwVerifyError(state);
    }

    @Override
    protected final void onBadClassFileException(State state, BadClassFileException e) {
        //bad value to load (triggered by call to resolve_XLOAD_GETX done by decider())
        throwVerifyError(state);
    }
}
