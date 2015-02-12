package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;

import jbse.algo.exc.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

public abstract class Algo_GETX extends MultipleStateGenerator_XLOAD_GETX implements Algorithm {
    @Override
    public final void exec(State state, ExecutionContext ctx)
    throws DecisionException, ContradictionException, 
    ClasspathException, ThreadStackEmptyException, 
    OperandStackEmptyException {
        //gets the index of the field signature in the current class 
        //constant pool
        final int index;
        try {
            final byte tmp1 = state.getInstruction(1);
            final byte tmp2 = state.getInstruction(2);
            index = Util.byteCat(tmp1, tmp2);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            return;
        }
        
        //gets the field signature from the current class constant pool
        final String currentClassName = state.getCurrentMethodSignature().getClassName();    
        final ClassHierarchy hier = state.getClassHierarchy();
        final Signature fieldSignature;
        try {
            fieldSignature = hier.getClassFile(currentClassName).getFieldSignature(index);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            return;
        } catch (BadClassFileException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        
        //performs field resolution
        final Signature fieldSignatureResolved;
        try {
            fieldSignatureResolved = hier.resolveField(currentClassName, fieldSignature);
        } catch (ClassFileNotFoundException e) {
            throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
            return;
        } catch (FieldNotFoundException e) {
            throwNew(state, NO_SUCH_FIELD_ERROR);
            return;
        } catch (FieldNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            return;
        } catch (BadClassFileException e) {
            throwVerifyError(state);
            return;
        }

        //gets resolved field's class

        //checks the field
        try {
            final String fieldClassName = fieldSignatureResolved.getClassName();        
            final ClassFile fieldClassFile = hier.getClassFile(fieldClassName);
            if (!fieldOk(fieldSignatureResolved, fieldClassFile)) {
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                return;
            }
        } catch (BadClassFileException | FieldNotFoundException e) {
            //this should never happen after field resolution
            throw new UnexpectedInternalException(e);
        }
        
        //generates all the next states
        try {
            this.state = state;
            this.ctx = ctx;
            this.pcOffset = 3;
            this.valToLoad = fieldValue(fieldSignatureResolved);
            generateStates();
        } catch (InterruptException e) {
            //nothing to do
        }
    }
    
    protected abstract boolean fieldOk(Signature fieldSignatureResolved, ClassFile fieldClassFile)
    throws FieldNotFoundException;
    
    protected abstract Value fieldValue(Signature fieldSignatureResolved)
    throws DecisionException, OperandStackEmptyException, 
    ThreadStackEmptyException, ClasspathException, InterruptException;
}
