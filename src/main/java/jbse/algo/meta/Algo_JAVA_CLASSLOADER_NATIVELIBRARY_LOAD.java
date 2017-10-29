package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_ISBUILTIN;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_LOADED;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_NAME;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link java.lang.ClassLoader.NativeLibrary#load(String, boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected void cookMore(State state) throws InterruptException {
        try {
            //just sets some field of the NativeLibrary object, to simulate
            //the fact that the library is loaded
            final Reference thisReference = (Reference) this.data.operand(0);
            final Value name = this.data.operand(1);
            final Value isBuiltin = this.data.operand(2);
            final Instance thisInstance = (Instance) state.getObject(thisReference);
            thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_NAME, name);
            thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_ISBUILTIN, isBuiltin);
            thisInstance.setFieldValue(JAVA_CLASSLOADER_NATIVELIBRARY_LOADED, state.getCalculator().valBoolean(true));
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //nothing to do
    }
}
