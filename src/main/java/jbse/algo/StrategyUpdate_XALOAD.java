package jbse.algo;

import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.tree.VisitorDecisionAlternative_XALOAD;
import jbse.val.exc.InvalidOperandException;

/**
 * {@link StrategyUpdate} for the *aload (load from array) bytecodes 
 * ([a/b/c/d/f/i/l/s]aload). It reimplements {@link StrategyUpdate#update} 
 * to redispatch towards abstract methods specializing refinement on 
 * the possible {@link DecisionAlternative_XALOAD}s.
 * This class exists only to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StrategyUpdate_XALOAD implements StrategyUpdate<DecisionAlternative_XALOAD> {
    abstract public void updateUnresolved(State state, DecisionAlternative_XALOAD_Unresolved alt) 
    throws DecisionException, InterruptException, MissingTriggerParameterException, 
    ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
    FrozenStateException, InvalidOperandException, InvalidInputException;

    abstract public void updateResolved(State state, DecisionAlternative_XALOAD_Resolved alt) 
    throws DecisionException, InterruptException, MissingTriggerParameterException, 
    ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
    FrozenStateException, InvalidOperandException, InvalidInputException;

    abstract public void updateOut(State state, DecisionAlternative_XALOAD_Out alt) 
    throws InterruptException, ClasspathException;

    @Override
    public final void update(final State state, DecisionAlternative_XALOAD alt)
    throws DecisionException, InterruptException, MissingTriggerParameterException, 
    ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
    InvalidInputException, InvalidOperandException {
        //a visitor redispatching to the methods which specialize this.update
        final VisitorDecisionAlternative_XALOAD visitorUpdate = 
            new VisitorDecisionAlternative_XALOAD() {
                @Override
                public void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException, 
                ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
                InvalidOperandException, InvalidInputException {
                    StrategyUpdate_XALOAD.this.updateUnresolved(state, alt);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException, 
                ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
                InvalidOperandException, InvalidInputException {
                    StrategyUpdate_XALOAD.this.updateUnresolved(state, alt);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException, 
                ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
                InvalidOperandException, InvalidInputException {
                    StrategyUpdate_XALOAD.this.updateUnresolved(state, alt);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException, 
                ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
                InvalidOperandException, InvalidInputException {
                    StrategyUpdate_XALOAD.this.updateResolved(state, alt);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out alt) 
                throws InterruptException, ClasspathException {
                    StrategyUpdate_XALOAD.this.updateOut(state, alt);
                }
            };

        try {
            alt.accept(visitorUpdate);
        } catch (DecisionException | InterruptException | MissingTriggerParameterException | 
                 ClasspathException | NotYetImplementedException | 
                 ThreadStackEmptyException | InvalidOperandException | 
                 InvalidInputException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedInternalException(e);
        }
    }
}
