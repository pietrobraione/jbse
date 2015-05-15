package jbse.algo.meta;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING_HASH;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.InterruptException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;

public final class Algo_JAVA_STRING_HASHCODE extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void update(State state) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference thisReference = (Reference) this.data.operand(0);
            final Objekt thisObjekt = state.getObject(thisReference);

            if (thisObjekt.isSymbolic()) {
                //gets the hashCode field in the string and returns it
                final Primitive hashCode = (Primitive) thisObjekt.getFieldValue(JAVA_STRING_HASH);
                //TODO ensure that strings that may not be equal have different hash codes
                state.pushOperand(hashCode);
            } else {
                continueWith(this.ctx.dispatcher.select(state.getInstruction())); //executes the original String.hashCode implementation
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
