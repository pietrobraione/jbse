package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public final class Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void update(State state) 
    throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
    InterruptException {
        Reference classNameRef = null; //to keep the compiler happy
        try {
            classNameRef = (Reference) this.data.operand(0);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //gets the name of the class and converts it to a string
        final String className = valueString(state, classNameRef);
        if (className == null) {
            throw new SymbolicValueNotAllowedException("The method needs a concrete String as name of the class to check.");
        }

        //pushes it
        try {
            state.assumeClassNotInitialized(className);
        } catch (BadClassFileException e) {
            throw new SymbolicValueNotAllowedException("The class " + className + " does not exist in the classpath.");
        }
    }
}
