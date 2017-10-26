package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Objekt;
import jbse.mem.State;
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
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected Value valueToPut() {
        return this.data.operand(0);
    }

    @Override
    protected void checkMore(State state, String fieldClassName, ClassFile fieldClassFile)
    throws FieldNotFoundException, BadClassFileException,
    DecisionException, ClasspathException, InterruptException {
        //checks that the field is static or belongs to an interface
        if (!fieldClassFile.isInterface() && 
            !fieldClassFile.isFieldStatic(this.fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }

        //possibly creates and initializes the class 
        try {
            ensureClassCreatedAndInitialized(state, fieldClassName, this.ctx);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected Objekt destination(State state) throws InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        return state.getKlass(fieldClassName);
    }
}
