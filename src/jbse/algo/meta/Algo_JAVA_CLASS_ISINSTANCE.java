package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.isPrimitiveBinaryClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.InterruptException;
import jbse.bc.ClassHierarchy;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

public final class Algo_JAVA_CLASS_ISINSTANCE extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void update(State state) 
    throws ThreadStackEmptyException, InterruptException {
        //gets the 'this' java.lang.Class instance from the heap 
        //and the name of the class it represents
        Reference javaClassRef = null; //to keep the compiler happy;
        try {
            javaClassRef = (Reference) this.data.operand(0);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
        final Instance_JAVA_CLASS javaClassObject = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
        final String representedClass = javaClassObject.representedClass();

        //gets the reference to the object to be checked
        Reference tmpValue = null; //to keep the compiler happy
        try {
            tmpValue = (Reference) this.data.operand(1);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

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

        //pushes the value
        state.pushOperand(valToPush);
    }
}
