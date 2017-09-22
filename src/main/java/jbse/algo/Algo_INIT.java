package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.jvm.exc.InitializationException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.val.ReferenceSymbolic;

/**
 * {@link Algorithm} for the first execution step.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_INIT {
    public void exec(ExecutionContext ctx) 
    throws DecisionException, InitializationException, 
    InvalidClassFileFactoryClassException, ClasspathException {
        //TODO do checks and possibly raise exceptions
        State state = ctx.getInitialState();
        if (state == null) {
            //builds the initial state
            state = createInitialState(ctx);
        }

        //adds the initial state to the state tree
        ctx.stateTree.addInitialState(state);
    }

    private State createInitialState(ExecutionContext ctx) 
    throws InvalidClassFileFactoryClassException, InitializationException, 
    DecisionException, ClasspathException {
        final State state = new State(ctx.classpath, ctx.classFileFactoryClass, ctx.expansionBackdoor, ctx.calc);

        //adds a method frame for the initial method invocation
        try {
            //TODO resolve rootMethodSignature and lookup implementation
            //TODO instead of assuming that {ROOT}:this exists and create the frame, use lazy initialization also on {ROOT}:this, for homogeneity and to explore a wider range of alternatives  
            final ReferenceSymbolic rootThis = state.pushFrameSymbolic(ctx.rootMethodSignature);
            if (rootThis != null) {
                final String className = state.getObject(rootThis).getType();
                final DecisionAlternative_XLOAD_GETX_Expands rootExpansion = ctx.decisionProcedure.getRootDecisionAlternative(rootThis, className);
                ctx.triggerManager.loadTriggerFramesRoot(state, rootExpansion);
            }
        } catch (BadClassFileException | MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new InitializationException(e);
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }

        //creates and initializes the root class
        try {
            ensureClassCreatedAndInitialized(state, ctx.rootMethodSignature.getClassName(), ctx);
        } catch (InterruptException e) {
            //nothing to do: fall through
        } catch (InvalidInputException | BadClassFileException e) {
            //this should not happen after push frame
            throw new UnexpectedInternalException(e);
        }

        //saves a copy of the created state
        ctx.setInitialState(state);

        return state;
    }
}
