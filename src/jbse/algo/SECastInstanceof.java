package jbse.algo;

import static jbse.algo.Util.ILLEGAL_ACCESS_ERROR;
import static jbse.algo.Util.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Util.byteCat;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

abstract class SECastInstanceof implements Algorithm {
    public SECastInstanceof() { }

    @Override
    public final void exec(State state, ExecutionContext ctx)
    throws ThreadStackEmptyException, OperandStackEmptyException {
        final int index;
        try {
            final byte tmp1 = state.getInstruction(1);
            final byte tmp2 = state.getInstruction(2);
            index = byteCat(tmp1,tmp2);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            return;
        }

        //gets in the current class constant pool the name 
        //of the class
        final ClassHierarchy hier = state.getClassHierarchy();
        final String currentClassName = state.getCurrentMethodSignature().getClassName();    
        final String classSignature;
        try {
            classSignature = hier.getClassFile(currentClassName).getClassSignature(index);
        } catch (ClassFileNotFoundException | InvalidIndexException e) {
            throwVerifyError(state);
            return;
        }

        //performs resolution
        final String classSignatureResolved;
        try {
            classSignatureResolved = hier.resolveClass(currentClassName, classSignature);
        } catch (ClassFileNotFoundException e) {
            createAndThrow(state, NO_CLASS_DEFINITION_FOUND_ERROR);
            return;
        } catch (ClassFileNotAccessibleException e) {
            createAndThrow(state, ILLEGAL_ACCESS_ERROR);
            return;
        }

        //gets from the operand stack the reference to the 
        //object to be checked
        final Reference tmpValue;
        try {
            tmpValue = (Reference) state.top();
        } catch (OperandStackEmptyException e) {
            throwVerifyError(state);
            return;
        }
        
        //checks whether the object's class is a subclass 
        //of the class name from the constant pool
        final boolean isSubclass;
        if (state.isNull(tmpValue)) {
            isSubclass = true;  //the null value belongs to all classes
        } else {
            final Objekt objS = state.getObject(tmpValue);
            String classS = objS.getType();
            isSubclass = hier.isSubclass(classS, classSignatureResolved);
        }
        
        //completes the bytecode semantics
        final boolean exit = complete(state, isSubclass);
        if (exit) {
            return;
        }

        //increments the program counter
        try {
            state.incPC(3);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
        } 
    }
    
    protected abstract boolean complete(State state, boolean isSubclass) 
    throws ThreadStackEmptyException, OperandStackEmptyException;
}
