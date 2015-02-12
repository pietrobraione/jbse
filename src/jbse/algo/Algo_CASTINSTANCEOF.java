package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.common.Util.byteCat;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

abstract class Algo_CASTINSTANCEOF implements Algorithm {
    public Algo_CASTINSTANCEOF() { }

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
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            return;
        } catch (BadClassFileException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }

        //performs resolution
        try {
            hier.resolveClass(currentClassName, classSignature);
        } catch (ClassFileNotFoundException e) {
            throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
            return;
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            return;
        } catch (BadClassFileException e) {
            throwVerifyError(state);
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
            isSubclass = hier.isSubclass(classS, classSignature);
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
