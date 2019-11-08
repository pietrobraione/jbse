package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Class#setSigners(Object[])}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JAVA_CLASS_SETSIGNERS extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException {
        try {           
            //gets the 'this' object
            final Reference classRef = (Reference) this.data.operand(0);
            if (state.isNull(classRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.setSigners method is null.");
            }
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            
            //gets the array of signers
            final Reference signersRef = (Reference) this.data.operand(1);
            if (signersRef.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The signers parameter to java.lang.System.setSigners was not a concrete reference.");
            }
            
            //sets the signers of the class
            clazz.setSigners((ReferenceConcrete) signersRef);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> { };
    }
}
