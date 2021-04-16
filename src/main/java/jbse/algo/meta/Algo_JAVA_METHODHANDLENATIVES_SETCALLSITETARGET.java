package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.meta.Util.getInstance;
import static jbse.algo.meta.Util.INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CALLSITE_TARGET;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.Util.ErrorAction;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Meta-level abstract implementation of {@link java.lang.invoke.MethodHandleNatives#setCallSiteTargetNormal(java.lang.invoke.CallSite, java.lang.invoke.MethodHandle)}
 * and {@link java.lang.invoke.MethodHandleNatives#setCallSiteTargetVolatile(java.lang.invoke.CallSite, java.lang.invoke.MethodHandle)}
 * 
 * @author Pietro Braione
 */
abstract class Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGET extends Algo_INVOKEMETA_Nonbranching {
	private final String methodName; //set by constructor
	private Instance callSite; //set by cookMore
	private Reference methodHandle; //set by cookMore
	
	public Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGET(String methodName) {
		this.methodName = methodName;
	}
	
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 2;
	}

	@Override
	protected void cookMore(State state) 
	throws SymbolicValueNotAllowedException, FrozenStateException, InterruptException, ClasspathException {
		final ErrorAction THROW_JAVA_INTERNAL_ERROR = msg -> { throwNew(state, this.ctx.getCalculator(), INTERNAL_ERROR); exitFromAlgorithm(); };

		//gets the first parameter (the CallSite)
		this.callSite = getInstance(state, this.data.operand(0), this.methodName, "CallSite site", THROW_JAVA_INTERNAL_ERROR, THROW_JAVA_INTERNAL_ERROR, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		
		//gets the second parameter (the reference to the MethodHandle)
		this.methodHandle = (Reference) this.data.operand(1);
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
			this.callSite.setFieldValue(JAVA_CALLSITE_TARGET, this.methodHandle);
		};
	}
}
