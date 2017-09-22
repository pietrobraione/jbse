package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;

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
    protected void check(State state, String currentClassName)
    throws FieldNotFoundException, BadClassFileException, 
    DecisionException, ClasspathException, InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        final ClassFile fieldClassFile = state.getClassHierarchy().getClassFile(fieldClassName);

        //checks that the field is static or belongs to an interface
        if (!fieldClassFile.isInterface() && 
            !fieldClassFile.isFieldStatic(this.fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }

        //checks that if the field is final is declared in the current class
        if (fieldClassFile.isFieldFinal(this.fieldSignatureResolved) &&
        !fieldClassName.equals(currentClassName)) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        }

        //TODO compare types of field and of the value to put in

        //possibly creates and initializes the class 
        try {
            ensureClassCreatedAndInitialized(state, fieldClassName, this.ctx);
        } catch (InvalidInputException | BadClassFileException e) {
            //this should never happen
            //TODO really?
            failExecution(e);
        }
    }

    @Override
    protected void put(State state) throws InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        state.getKlass(fieldClassName).setFieldValue(this.fieldSignatureResolved, this.data.operand(0));
    }
}
