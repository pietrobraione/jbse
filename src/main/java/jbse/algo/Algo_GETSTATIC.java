package jbse.algo;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;

/**
 * {@link Algorithm} managing the getstatic bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_GETSTATIC extends Algo_GETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void check(State state)
    throws ClasspathException, FieldNotFoundException, InterruptException {
        //checks that the field is static or belongs to an interface
        if (!this.fieldClassResolved.isInterface() && 
            !this.fieldClassResolved.isFieldStatic(this.data.signature())) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
    }

    @Override
    protected Objekt source(State state)
    throws ClasspathException, DecisionException, InterruptException, 
    ContradictionException, FrozenStateException {
        //possibly initializes the class of the field
        try {
            ensureClassInitialized(state, this.ctx, this.fieldClassResolved);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidInputException e) {
            //this should never happen
            //TODO really?
            failExecution(e);
        }

        return state.getKlass(this.fieldClassResolved);
    }
}
