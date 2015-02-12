package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;

import jbse.algo.exc.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

/**
 * Command managing the getstatic bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_GETSTATIC extends Algo_GETX {
    @Override
    protected boolean fieldOk(Signature fieldSignatureResolved, ClassFile fieldClassFile)
    throws FieldNotFoundException {
        return (fieldClassFile.isInterface() || fieldClassFile.isFieldStatic(fieldSignatureResolved));
    }
    
    @Override
    protected Value fieldValue(Signature fieldSignatureResolved)
    throws DecisionException, ThreadStackEmptyException, 
    ClasspathException, InterruptException {
        final String fieldClassName = fieldSignatureResolved.getClassName();
        
        //possibly creates and initializes the class of the field
        try {
            ensureClassCreatedAndInitialized(state, fieldClassName, ctx.decisionProcedure);
        } catch (BadClassFileException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        //gets the field's value 
        return state.getKlass(fieldClassName).getFieldValue(fieldSignatureResolved);
    }
}
