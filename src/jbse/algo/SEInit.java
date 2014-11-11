package jbse.algo;

import static jbse.algo.Util.ensureKlass;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.InitializationException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

/**
 * {@link Algorithm} for the first execution step.
 * 
 * @author Pietro Braione
 *
 */
public class SEInit {
	public void exec(ExecutionContext ctx) 
	throws DecisionException, InitializationException, UnexpectedInternalException, InvalidClassFileFactoryClassException {
		//TODO do checks and possibly raise exceptions
		State state = ctx.getInitialState();
		if (state == null) {
			//builds the initial state
			state = createInitialState(ctx);
		}
		
		//aligns the initial state with the context
		align(state, ctx);
	}
	
	private State createInitialState(ExecutionContext ctx) 
	throws InvalidClassFileFactoryClassException, InitializationException, 
	DecisionException, UnexpectedInternalException {
		final State state = new State(ctx.getClasspath(), ctx.classFileFactoryClass, ctx.expansionBackdoor, ctx.calc);

		//adds a method frame for the initial method invocation
		try {
			//TODO resolve rootMethodSignature
			//TODO instead of assuming that {ROOT}:this exists and create the frame, use lazy initialization also on {ROOT}:this, for homogeneity and to explore a wider range of alternatives  
			final ClassHierarchy hier = state.getClassHierarchy();
			final ClassFile rootMethodClassFile = hier.getClassFile(ctx.rootMethodSignature.getClassName());
			state.pushFrameSymbolic(ctx.rootMethodSignature, rootMethodClassFile.isStatic());
		} catch (ClassFileNotFoundException | MethodNotFoundException | 
				IncompatibleClassFileException | PleaseDoNativeException e) {
			throw new InitializationException(e);
		}

		//adds the root klass, and if this is not initialized
		//pushes all the <clinit> frames in its hierarchy
		try {
			ensureKlass(state, ctx.rootMethodSignature.getClassName(), ctx.decisionProcedure);
		} catch (ClassFileNotFoundException | ThreadStackEmptyException e) {
			//this should not happen after push frame
			throw new UnexpectedInternalException(e);
		}
		
		//sets the created state as the initial one (a safety copy)
		ctx.setInitialState(state.clone());
		
		return state;
	}
	
	private static void align(State state, ExecutionContext ctx) 
	throws DecisionException, UnexpectedInternalException {
		//synchronizes the decision procedure with the state
		ctx.decisionProcedure.setAssumptions(state.getPathCondition());
		state.resetLastPathConditionClauses();
		
		//adds the initial state to the state tree
		ctx.stateTree.addInitialState(state);
	}
}
