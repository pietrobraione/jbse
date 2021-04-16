package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicMemberMapKey;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#onKeyResolution0(Object)}, 
 * {@link jbse.base.JAVA_CONCURRENTMAP#onKeyResolution0(Object)} and
 * {@link jbse.base.JAVA_LINKEDMAP#onKeyResolution0(Object)}.
 *  
 * @author Pietro Braione
 */
abstract class Algo_JBSE_JAVA_XMAP_ONKEYRESOLUTION0 extends Algo_INVOKEMETA_Nonbranching {
	private final Signature onKeyResolutionComplete; //set by constructor
	
	public Algo_JBSE_JAVA_XMAP_ONKEYRESOLUTION0(Signature onKeyResolutionComplete) {
		this.onKeyResolutionComplete = onKeyResolutionComplete;
	}
	
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected final void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets the key and the map references
            final ReferenceSymbolicMemberMapKey keyRef = (ReferenceSymbolicMemberMapKey) this.data.operand(0);
            final ReferenceSymbolic mapRef = keyRef.getContainer();
            
            //upcalls the base implementation
            final Snippet snippet = state.snippetFactoryNoWrap()
            		//parameters
            		.addArg(mapRef)
            		.addArg(keyRef)
            		//pushes everything
            		.op_aload((byte) 0)
            		.op_aload((byte) 1)
            		//invokes
            		.op_invokestatic(this.onKeyResolutionComplete)
            		.op_return()
            		.mk();
            
            try {
            	state.pushSnippetFrameNoWrap(snippet, programCounterUpdate().get());
            } catch (InvalidProgramCounterException e) {
            	//this should never happen
            	failExecution(e);
            }
            exitFromAlgorithm();
        } catch (ClassCastException e) {
        	//this should never happen
        	failExecution(e);
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return null; //never used
    }
}
