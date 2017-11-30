package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_SYSTEM;
import static jbse.bc.Signatures.JAVA_SYSTEM_ERR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.mem.Klass;
import jbse.mem.State;

/**
 * Meta-level implementation of {@link java.lang.System#setErr0(java.io.PrintStream)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_SYSTEM_SETERR0 extends Algo_INVOKEMETA_Nonbranching {
    private Klass k; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    
    @Override
    protected void cookMore(State state) throws UndefinedResultException {
        this.k = state.getKlass(JAVA_SYSTEM);
        if (this.k == null) {
            throw new UndefinedResultException("Invoked java.lang.System.setErr0 before initialization of class java.lang.System.");
        }
    }

    @Override
    protected void update(State state) {
        this.k.setFieldValue(JAVA_SYSTEM_ERR, this.data.operand(0));
    }
}
