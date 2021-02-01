package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTIONCOMPLETE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
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
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#onKeyResolution0(Object)}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0 extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
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
            		.op_invokestatic(JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTIONCOMPLETE)
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
