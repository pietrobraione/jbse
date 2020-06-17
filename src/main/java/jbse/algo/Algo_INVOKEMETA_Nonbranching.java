package jbse.algo;

import static jbse.bc.Offsets.offsetInvoke;

import java.util.function.Supplier;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

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

    protected int pcOffset; //set by cooker

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            //sets the program counter offset for the return point
            this.pcOffset = offsetInvoke(this.isInterface);
            cookMore(state);
        };
    }

    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, ClasspathException, 
    CannotManageStateException, InterruptException, InvalidInputException, 
    ContradictionException, InvalidTypeException, InvalidOperandException, 
    RenameUnsupportedException {
        //the default implementation does nothing
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.pcOffset;
    }
}
