package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.common.Type.isPrimitiveBinaryClassName;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassHierarchy;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

public class Algo_JAVA_CLASS_ISINSTANCE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
	InterruptException {
		try {
            //gets from the operand stack the reference to the 
            //object to be checked
            final Reference tmpValue;
            try {
                tmpValue = (Reference) state.popOperand();
            } catch (OperandStackEmptyException e) {
                throwVerifyError(state);
                return;
            }
            
			//gets the 'this' java.lang.Class instance from the heap 
            //and the name of the class it represents
            final Reference javaClassRef = (Reference) state.popOperand();
            final Instance_JAVA_CLASS javaClassObject = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            final String representedClass = javaClassObject.representedClass();

            //determines which value to push on the operand stack
            final Simplex valToPush;
            if (isPrimitiveBinaryClassName(representedClass) || state.isNull(tmpValue)) {
                valToPush = state.getCalculator().valInt(0);
            } else {
                //checks whether the object's class is a subclass 
                //of the class name from the constant pool
                final ClassHierarchy hier = state.getClassHierarchy();
                final Objekt obj = state.getObject(tmpValue);
                final String objClass = obj.getType();
                valToPush = state.getCalculator().valInt(hier.isSubclass(objClass, representedClass) ? 1 : 0);
            }
            state.pushOperand(valToPush);
		} catch (OperandStackEmptyException | ClassCastException e) {
		    throwVerifyError(state);
            throw new InterruptException();
		}

        //increments the program counter
		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
        throw new InterruptException();
	}
}
