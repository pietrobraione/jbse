package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Reference;

/**
 * {@link Algorithm} managing the getfield bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_GETFIELD extends Algo_GETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void check(State state)
    throws ClasspathException, FieldNotFoundException, InterruptException {
        //checks that the field is not static
        if (this.fieldClassResolved.isFieldStatic(this.data.signature())) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
    }

    @Override
    protected Objekt source(State state) throws ClasspathException, InterruptException, FrozenStateException {
        try {
            final Reference myObjectRef = (Reference) this.data.operand(0);
            if (state.isNull(myObjectRef)) {
                throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            return state.getObject(myObjectRef); 
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        return null; //to keep the compiler happy
    }    
}