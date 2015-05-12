package jbse.algo.meta;

import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import java.util.function.Supplier;

import jbse.algo.Algorithm;
import jbse.algo.BytecodeCooker;
import jbse.algo.BytecodeData_1ME;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Abstract {@link Algorithm} implementing the effect of 
 * a method call.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Algo_INVOKEMETA extends Algorithm<
BytecodeData_1ME,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private final boolean isInterface; //set by constructor
    
    public Algo_INVOKEMETA(boolean isInterface) {
        this.isInterface = isInterface;
    }

    private int pcOffset; //set by cooker

    @Override
    protected final Supplier<BytecodeData_1ME> bytecodeData() {
        return () -> BytecodeData_1ME.withInterfaceMethod(this.isInterface).get();
    }
    
    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            //sets the program counter offset for the return point
            this.pcOffset = (this.isInterface ? 
                            INVOKEDYNAMICINTERFACE_OFFSET : 
                            INVOKESPECIALSTATICVIRTUAL_OFFSET);
        };
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
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
    
    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> this.pcOffset;
    }
}
