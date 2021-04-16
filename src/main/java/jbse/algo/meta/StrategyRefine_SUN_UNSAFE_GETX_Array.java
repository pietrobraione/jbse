package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;

import jbse.algo.StrategyRefine;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.tree.VisitorDecisionAlternative_XALOAD;
import jbse.val.exc.InvalidTypeException;

/**
 * Strategy for refining a state during the execution of 
 * {@link sun.misc.Unsafe.getIntVolatile(Object, long)} when the object 
 * parameter is an array.
 * 
 * @author Pietro Braione
 */
abstract class StrategyRefine_SUN_UNSAFE_GETX_Array implements StrategyRefine<DecisionAlternative_XALOAD>{
    abstract public void refineResolved(State s, DecisionAlternative_XALOAD_Resolved dav) 
    throws DecisionException, InvalidInputException, ContradictionException;

    abstract public void refineOut(State s, DecisionAlternative_XALOAD_Out dao) 
    throws InvalidInputException, ContradictionException;

    public final void refine(final State s, DecisionAlternative_XALOAD r)
    throws DecisionException, ContradictionException, InvalidTypeException, InvalidInputException {
        //a visitor redispatching to the methods which specialize this.refine
        final VisitorDecisionAlternative_XALOAD visitorRefine = 
            new VisitorDecisionAlternative_XALOAD() {
                @Override
                public void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands dac)
                throws DecisionException, ContradictionException, InvalidTypeException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases dai)
                throws DecisionException, ContradictionException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null dan)
                throws DecisionException, ContradictionException {
                    //this should never happen
                    failExecution("Unexpected decision on symbolic reference during sun.misc.Unsafe.getIntVolatile invocation");
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved dav)
                throws DecisionException, ContradictionException, InvalidInputException {
                    StrategyRefine_SUN_UNSAFE_GETX_Array.this.refineResolved(s, dav);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out dao) 
                throws ContradictionException, InvalidInputException {
                    StrategyRefine_SUN_UNSAFE_GETX_Array.this.refineOut(s, dao);
                }
            };

        //redispatches and manages exceptions
        try {
            r.accept(visitorRefine);
        } catch (DecisionException | ContradictionException | 
                 InvalidTypeException | InvalidInputException | 
                 RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            failExecution(e);
        }
    }
}
