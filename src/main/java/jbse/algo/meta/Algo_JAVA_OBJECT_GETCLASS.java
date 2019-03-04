package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Object#getClass()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_OBJECT_GETCLASS extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile className; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, SymbolicValueNotAllowedException, 
    InterruptException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the "this" object and the name of its class
            final Reference thisRef = (Reference) this.data.operand(0);
            if (state.isNull(thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.getClass method is null.");
            }
            final Objekt thisObj = state.getObject(thisRef);
            if (thisObj == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.getClass method is symbolic and unresolved.");
            }
            this.className = thisObj.getType();
            state.ensureInstance_JAVA_CLASS(calc, this.className);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //gets the instance of the class of the "this" object
            final Reference classRef = state.referenceToInstance_JAVA_CLASS(this.className);
            state.pushOperand(classRef);
        };
    }
}
