package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.continueWithBaseLevelImpl;
import static jbse.common.Type.parametersNumber;

import java.util.function.Supplier;

import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * {@link Algo_INVOKEMETA} implementing the effect of 
 * a method call that pushes a function application term 
 * if its parameters are symbolic. More precisely:
 * <ul>
 * <li>If the method's parameters are all concrete and the 
 *     method has bytecode, then continues with the execution 
 *     of the method's bytecode;
 * <li>Otherwise, it continues with {@link Algo_INVOKEMETA_Metacircular}. 
 * </ul>
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_INVOKEX_Abstract and subclasses and with Algo_XYLOAD_GETX and subclasses
public final class Algo_INVOKEMETA_Uninterpreted extends Algo_INVOKEMETA_Nonbranching {
    private final Algo_INVOKEMETA_Metacircular algo_INVOKEMETA_Metacircular = new Algo_INVOKEMETA_Metacircular();

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> {
            return parametersNumber(this.data.signature().getDescriptor(), this.isStatic);
        };
    }

    @Override
    protected void cookMore(State state) throws InterruptException, FrozenStateException {
    	//if this algorithm is overriding a native method, the only
    	//possible alternative is trying to execute it metacircularly
    	if (this.isOverriddenMethodNative) {
    		this.algo_INVOKEMETA_Metacircular.setFeatures(this.isInterface, this.isSpecial, this.isStatic, this.isOverriddenMethodNative, this.methodSignatureImplementation);
    		continueWith(this.algo_INVOKEMETA_Metacircular);
    	}            

    	//calculates whether the parameters are all simple
    	//(i.e., concrete if they are numeric, containing
    	//concrete values in their slots if they are objects)
    	final Value[] args = this.data.operands();
    	boolean allSimple = true;
    	for (int i = 0; i < args.length; ++i) {
    		if (args[i] instanceof Primitive && args[i].isSymbolic()) {
    			allSimple = false;
    			break;
    		} else if (args[i] instanceof Reference) {
    			if (!isSimple(state, (Reference) args[i])) {
    				allSimple = false;
    				break;
    			}
    		}
    	}

    	//if all parameters are concrete, executes the overridden method,
    	//otherwise falls back to a metacircular invocation
    	if (allSimple) {
    		continueWithBaseLevelImpl(state, this.isInterface, this.isSpecial, this.isStatic);
    	} else {
    		this.algo_INVOKEMETA_Metacircular.setFeatures(this.isInterface, this.isSpecial, this.isStatic, this.isOverriddenMethodNative, this.methodSignatureImplementation);
    		continueWith(this.algo_INVOKEMETA_Metacircular);
    	}
    }
    
    private static boolean isSimple(State state, Reference objRef) throws FrozenStateException {
		if (state.isNull(objRef)) {
			return true;
		}
		final Objekt obj = state.getObject(objRef);
		if (obj instanceof Array) {
			return ((Array) obj).isSimple();
		} else {
			for (Variable var : obj.fields().values()) {
				final Value val = var.getValue();
				if (val instanceof Primitive && val.isSymbolic()) {
					return false;
				} else if (val instanceof Reference && !isSimple(state, (Reference) val)) {
					return false;
				}
			}
			return true;
		}
    }

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return null; //will never be invoked
	}
}
