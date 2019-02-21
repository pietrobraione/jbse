package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFile;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Action} for the first execution step. It loads
 * the frames for the root method, and possibly for the
 * trigger frames that the assumption of the root object
 * may cause to fire, and of the  root class static 
 * initializer if present, and jumps at the first
 * bytecode.
 * 
 * @author Pietro Braione
 */
public final class Action_INIT implements Action {
    /**
     * Constructor.
     */
    public Action_INIT() { }
    
    @Override
    public void exec(State state, ExecutionContext ctx)
    throws DecisionException, ContradictionException, ThreadStackEmptyException, ClasspathException,
    CannotManageStateException, FailureException, ContinuationException {
        try {
            //pushes a frame for the root method (and possibly triggers)
            invokeRootMethod(state, ctx);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
        }
    }
    
    private void invokeRootMethod(State state, ExecutionContext ctx) 
    throws ClasspathException, CannotAssumeSymbolicObjectException , MissingTriggerParameterException, NotYetImplementedException, HeapMemoryExhaustedException {
        try {
            //TODO resolve rootMethodSignature and lookup implementation
            //TODO instead of assuming that {ROOT}:this exists and create the frame, use lazy initialization also on {ROOT}:this, for homogeneity and to explore a wider range of alternatives
            final ClassFile rootClass = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_APP, ctx.rootMethodSignature.getClassName());
            final ReferenceSymbolic rootThis = state.pushFrameSymbolic(rootClass, ctx.rootMethodSignature);
            if (rootThis != null) {
                //must assume {ROOT}:this expands to nonnull object (were it null the root frame would not exist!)
                state.assumeExpands(rootThis, rootClass);
                final ClassFile rootThisClass = state.getObject(rootThis).getType();
                final DecisionAlternative_XLOAD_GETX_Expands rootExpansion = ctx.decisionProcedure.getRootDecisionAlternative(rootThis, rootThisClass);
                ctx.triggerManager.loadTriggerFramesRoot(state, rootExpansion);
            }
        } catch (MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new ClasspathException(e);
        } catch (ThreadStackEmptyException | ContradictionException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }
}
