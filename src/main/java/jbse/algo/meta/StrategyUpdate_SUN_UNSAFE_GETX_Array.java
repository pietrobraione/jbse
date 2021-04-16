package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;

import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.tree.VisitorDecisionAlternative_XALOAD;
import jbse.val.exc.InvalidOperandException;

/**
 * Strategy for updating a state during the execution of 
 * {@link sun.misc.Unsafe.getIntVolatile(Object, long)} when the object 
 * parameter is an array.
 * 
 * @author Pietro Braione
 */
//TODO refactor together with StrategyUpdate_SUN_UNSAFE_GETOBJECTVOLATILE_Array
abstract class StrategyUpdate_SUN_UNSAFE_GETX_Array implements StrategyUpdate<DecisionAlternative_XALOAD> {
    abstract public void updateResolved(State state, DecisionAlternative_XALOAD_Resolved alt) 
    throws DecisionException, InterruptException, MissingTriggerParameterException,
    ClasspathException, NotYetImplementedException, InvalidInputException, InvalidOperandException;

    abstract public void updateOut(State state, DecisionAlternative_XALOAD_Out alt) 
    throws CannotManageStateException;

    @Override
    public final void update(final State state, DecisionAlternative_XALOAD alt)
    throws DecisionException, InterruptException, MissingTriggerParameterException, 
    ClasspathException, NotYetImplementedException, InvalidInputException, InvalidOperandException {
        //a visitor redispatching to the methods which specialize this.update
        final VisitorDecisionAlternative_XALOAD visitorUpdate = 
            new VisitorDecisionAlternative_XALOAD() {
                @Override
                public void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved alt) 
                throws DecisionException, InterruptException, MissingTriggerParameterException, ClasspathException, 
                NotYetImplementedException, InvalidInputException, InvalidOperandException {
                    StrategyUpdate_SUN_UNSAFE_GETX_Array.this.updateResolved(state, alt);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out alt) 
                throws CannotManageStateException {
                    StrategyUpdate_SUN_UNSAFE_GETX_Array.this.updateOut(state, alt);
                }
            };

        try {
            alt.accept(visitorUpdate);
        } catch (DecisionException | InterruptException | 
                 MissingTriggerParameterException | ClasspathException | 
                 NotYetImplementedException | InvalidInputException | 
                 InvalidOperandException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            failExecution(e);
        }
    }
}
