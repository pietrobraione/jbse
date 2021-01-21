package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.ensureClassInitialized;
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
import jbse.val.Value;

//TODO merge with Algo_GETSTATIC
/**
 * {@link Algorithm} managing the "set static field in class"
 * (putstatic) bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_PUTSTATIC extends Algo_PUTX {
    public Algo_PUTSTATIC() {
        super(true);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected Value valueToPut() {
        return this.data.operand(0);
    }

    @Override
    protected void checkMore(State state)
    throws FieldNotFoundException, DecisionException, ClasspathException, 
    InterruptException, ContradictionException {
        //checks that the field is static or belongs to an interface
        if (!this.fieldClassResolved.isInterface() && 
            !this.fieldClassResolved.isFieldStatic(this.data.signature())) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }

        //possibly creates and initializes the class 
        try {
            ensureClassInitialized(state, this.ctx, this.fieldClassResolved);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected Objekt destination(State state) 
    throws InterruptException, FrozenStateException {
        return state.getKlass(this.fieldClassResolved);
    }
}
