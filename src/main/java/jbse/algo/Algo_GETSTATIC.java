package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;

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
    protected void check(State state, String currentClassName)
    throws FieldNotFoundException, BadClassFileException, InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        final ClassFile fieldClassFile = state.getClassHierarchy().getClassFile(fieldClassName);

        //checks that the field is static or belongs to an interface
        if (!fieldClassFile.isInterface() && 
            !fieldClassFile.isFieldStatic(this.fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected void get(State state)
    throws DecisionException, ClasspathException, InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        
        //possibly creates and initializes the class of the field
        try {
            ensureClassCreatedAndInitialized(state, fieldClassName, this.ctx);
        } catch (InvalidInputException | BadClassFileException e) {
            //this should never happen
            //TODO really?
            failExecution(e);
        }
        
        //gets the field's value 
        this.valToLoad = state.getKlass(fieldClassName).getFieldValue(this.fieldSignatureResolved);
    }
}
