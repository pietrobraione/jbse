package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import java.util.function.Supplier;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Abstract {@link Algorithm} implementing the effect of 
 * a method call that does not branch symbolic execution, 
 * i.e., produces one successor state.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Algo_INVOKEMETA_Nonbranching extends Algo_INVOKEMETA<
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private int pcOffset; //set by cooker

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            //sets the program counter offset for the return point
            this.pcOffset = (this.isInterface ? 
                INVOKEDYNAMICINTERFACE_OFFSET : 
                INVOKESPECIALSTATICVIRTUAL_OFFSET);
            try {
                cookMore(state);
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, CannotManageStateException, 
    InterruptException {
        //the default implementation does nothing
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //updates
            try { 
                update(state);
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    protected abstract void update(State state)
    throws ThreadStackEmptyException, ClasspathException,
    CannotManageStateException, DecisionException, 
    ContradictionException, FailureException, InterruptException;

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> this.pcOffset;
    }
}
