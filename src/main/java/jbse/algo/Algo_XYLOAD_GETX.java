package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.INT;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;

import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Loads;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract {@link Algorithm} for *load*, *aload, get* bytecodes 
 * ([a/d/f/i/l]load[_0/1/2/3], [a/b/c/d/f/i/l/s]aload,  get[field/static]).
 * It manages refinement in the case the {@link Value} 
 * is a {@link ReferenceSymbolic} ("lazy initialization"), and/or it comes 
 * from an {@link Array}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_XYLOAD_GETX<
D extends BytecodeData, 
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> extends Algorithm<D, R, DE, RE, UP> {

    //set by subclasses (decider method)
    protected boolean someRefNotExpanded;
    protected String nonExpandedRefTypes;
    protected String nonExpandedRefOrigins;

    @Override
    protected final void cleanup() {
        this.someRefNotExpanded = false;
        this.nonExpandedRefTypes = "";
        this.nonExpandedRefOrigins = "";
        super.cleanup();
    }

    protected final void refineRefExpands(State state, DecisionAlternative_XYLOAD_GETX_Expands drc) 
    throws ContradictionException, InvalidTypeException {
        final ReferenceSymbolic referenceToExpand = drc.getValueToLoad();
        final String classNameOfTargetObject = drc.getClassNameOfTargetObject();
        state.assumeExpands(referenceToExpand, classNameOfTargetObject);
        //in the case the expansion object is an array, we assume it 
        //to have nonnegative length
        if (isArray(classNameOfTargetObject)) {
            try {
                final Array targetObject = (Array) state.getObject(referenceToExpand);
                final Primitive lengthPositive = targetObject.getLength().ge(state.getCalculator().valInt(0));
                state.assume(this.ctx.decisionProcedure.simplify(lengthPositive));
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                failExecution(e);
            }
        }
    }

    protected final void refineRefAliases(State state, DecisionAlternative_XYLOAD_GETX_Aliases altAliases)
    throws ContradictionException {
        final ReferenceSymbolic referenceToResolve = altAliases.getValueToLoad();
        final long aliasPosition = altAliases.getAliasPosition();
        final Objekt object = state.getObjectInitial(new ReferenceConcrete(aliasPosition));
        state.assumeAliases(referenceToResolve, aliasPosition, object);
    }

    protected final void refineRefNull(State state, DecisionAlternative_XYLOAD_GETX_Null altNull)
    throws ContradictionException {
        final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
        state.assumeNull(referenceToResolve);
    }

    protected final void update(State state, DecisionAlternative_XYLOAD_GETX_Loads altLoads) 
    throws DecisionException, InterruptException {
        //possibly materializes the value
        final Value val = altLoads.getValueToLoad();
        final Value valMaterialized = possiblyMaterialize(state, val);
        final char valMaterializedType = valMaterialized.getType();

        //pushes the value
        try {
            final Value valToPush;
            if (isPrimitive(valMaterializedType) && !isPrimitiveOpStack(valMaterializedType)) {
                valToPush = ((Primitive) valMaterialized).to(INT);
            } else {
                valToPush = valMaterialized;
            }
            state.pushOperand(valToPush);
        } catch (ClassCastException | InvalidTypeException | 
                 ThreadStackEmptyException e) {
            //this should not happen
            failExecution(e);
        }

        //manages triggers
        try {
            final boolean someTriggerFrameLoaded = 
                this.ctx.triggerManager.loadTriggerFrames(state, altLoads, this.programCounterUpdate.get());
            if (someTriggerFrameLoaded) {
                exitFromAlgorithm();
            }
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            //this should not happen
            failExecution(e);
        }
    }

    /** 
     * Materializes an immaterial {@link Value}.
     * 
     * @param s a {@link State}
     * @param val the {@link Value} to be materialized.
     * @return a materialized {@link Value}.
     * @throws DecisionException if 
     */
    protected abstract Value possiblyMaterialize(State s, Value val) 
    throws DecisionException;

    @Override
    public final boolean someReferenceNotExpanded() { 
        return this.someRefNotExpanded; 
    }

    @Override
    public final String nonExpandedReferencesTypes() { 
        return this.nonExpandedRefTypes; 
    }

    @Override
    public final String nonExpandedReferencesOrigins() { 
        return this.nonExpandedRefOrigins; 
    }
}
