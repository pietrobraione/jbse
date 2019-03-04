package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_ISBUILTIN;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_LOADED;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_NAME;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link java.lang.ClassLoader.NativeLibrary#load(String, boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD extends Algo_INVOKEMETA_Nonbranching {
    private Instance thisInstance; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            //just sets some field of the NativeLibrary object, to simulate
            //the fact that the library is loaded
            final Reference thisReference = (Reference) this.data.operand(0);
            this.thisInstance = (Instance) state.getObject(thisReference);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Value name = this.data.operand(1);
            final Value isBuiltin = this.data.operand(2);
            this.thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_NAME, name);
            this.thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_ISBUILTIN, isBuiltin);
            this.thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_LOADED, this.ctx.getCalculator().valBoolean(true));
        };
    }
}
