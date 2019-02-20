package jbse.algo;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
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
 * Strategy for refining a state for the *aload bytecodes
 * ([a/b/c/d/f/i/l/s]aload). It reimplements 
 * {@link StrategyRefine#refine} to redispatch 
 * towards abstract methods specializing refinement on 
 * the possible {@link DecisionAlternative_XALOAD}s.
 * This class exists only to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StrategyRefine_XALOAD implements StrategyRefine<DecisionAlternative_XALOAD> {
    abstract public void refineRefExpands(State s, DecisionAlternative_XALOAD_Expands dac) 
    throws DecisionException, ContradictionException, InvalidTypeException, InvalidInputException, 
    InterruptException, SymbolicValueNotAllowedException, ClasspathException;

    abstract public void refineRefAliases(State s, DecisionAlternative_XALOAD_Aliases dai) 
    throws DecisionException, ContradictionException, InvalidInputException, InterruptException, 
    ClasspathException;

    abstract public void refineRefNull(State s, DecisionAlternative_XALOAD_Null dan) 
    throws DecisionException, ContradictionException, InvalidInputException;

    abstract public void refineResolved(State s, DecisionAlternative_XALOAD_Resolved dav) 
    throws DecisionException, InvalidInputException;

    abstract public void refineOut(State s, DecisionAlternative_XALOAD_Out dao) 
    throws InvalidInputException;

    @Override
    public final void refine(final State s, DecisionAlternative_XALOAD r)
    throws DecisionException, ContradictionException, InvalidTypeException, InvalidInputException, 
    InterruptException, SymbolicValueNotAllowedException, ClasspathException {
        //a visitor redispatching to the methods which specialize this.refine
        final VisitorDecisionAlternative_XALOAD visitorRefine = 
            new VisitorDecisionAlternative_XALOAD() {
                @Override
                public void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands dac)
                throws DecisionException, ContradictionException, InvalidTypeException, InvalidInputException, 
                InterruptException, SymbolicValueNotAllowedException, ClasspathException {
                    StrategyRefine_XALOAD.this.refineRefExpands(s, dac);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases dai)
                throws DecisionException, ContradictionException, InvalidInputException, InterruptException, 
                ClasspathException {
                    StrategyRefine_XALOAD.this.refineRefAliases(s, dai);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null dan)
                throws DecisionException, ContradictionException, InvalidInputException {
                    StrategyRefine_XALOAD.this.refineRefNull(s, dan);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved dav)
                throws DecisionException, InvalidInputException {
                    StrategyRefine_XALOAD.this.refineResolved(s, dav);
                }
    
                @Override
                public void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out dao) 
                throws InvalidInputException {
                    StrategyRefine_XALOAD.this.refineOut(s, dao);
                }
            };

        //redispatches and manages exceptions
        try {
            r.accept(visitorRefine);
        } catch (DecisionException | ContradictionException | 
                 InvalidTypeException | InvalidInputException | InterruptException | 
                 SymbolicValueNotAllowedException | ClasspathException | 
                 RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
}
