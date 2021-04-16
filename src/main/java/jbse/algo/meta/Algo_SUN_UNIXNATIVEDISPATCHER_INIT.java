package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.bc.Signatures.SUN_UNIXNATIVEDISPATCHER;
import static jbse.common.Type.binaryClassName;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link sun.nio.fs.UnixNativeDispatcher#init()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNIXNATIVEDISPATCHER_INIT extends Algo_INVOKEMETA_Nonbranching {
	private Simplex capabilities; //set by cookMore
	
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 0;
	}
	
	@Override
	protected void cookMore(State state) {
		//we do not metacircularly invoke the method, 
		//rather we ensure that sun.nio.fs.UnixNativeDispatcher
		//is loaded and initialized and peek the return 
		//value from it
		try {
			final Class<?> class_SUN_UNIXNATIVEDISPATCHER = Class.forName(binaryClassName(SUN_UNIXNATIVEDISPATCHER));
			final Field capabilitiesField = class_SUN_UNIXNATIVEDISPATCHER.getDeclaredField("capabilities");
			capabilitiesField.setAccessible(true);
			final int capabilitiesInt = capabilitiesField.getInt(null);
			this.capabilities = this.ctx.getCalculator().valInt(capabilitiesInt);
		} catch (ClassNotFoundException e) {
			//this may happen with a badly misconfigured JBSE, which 
			//is using a UNIX JRE but is running on a non-UNIX platform, 
			//breaking the metacircularity hypothesis
            failExecution(e);
		} catch (NoSuchFieldException e) {
			//this may happen if the version of the JRE used by JBSE
			//is very different from the one we are currently assuming,
			//i.e., Java 8. AFAIK all Java 8 JREs should have this field
            failExecution(e);
		} catch (SecurityException | IllegalAccessException e) {
			//this should never happen
            //TODO I'm not quite sure that SecurityException can never be raised
            failExecution(e);
		}
	}
	
	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	state.pushOperand(this.capabilities);
        };
	}
}
