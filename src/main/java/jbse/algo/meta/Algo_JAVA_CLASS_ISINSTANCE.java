package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.isPrimitiveBinaryClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.ClassHierarchy;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isInstance(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_ISINSTANCE extends Algo_INVOKEMETA_Nonbranching {
    private String representedClass; //set by cookMore
    private Reference tmpValue; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException {
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and the name of the class it represents
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS javaClassObject = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (javaClassObject == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            this.representedClass = javaClassObject.representedClass();
            
            //gets the reference to the object to be checked
            this.tmpValue = (Reference) this.data.operand(1);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //determines which value to push on the operand stack
        final Simplex valToPush;
        if (isPrimitiveBinaryClassName(this.representedClass) || state.isNull(this.tmpValue)) {
            valToPush = state.getCalculator().valInt(0);
        } else {
            //checks whether the object's class is a subclass 
            //of the class name from the constant pool
            final ClassHierarchy hier = state.getClassHierarchy();
            final Objekt obj = state.getObject(this.tmpValue);
            final String objClass = obj.getType();
            valToPush = state.getCalculator().valInt(hier.isSubclass(objClass, this.representedClass) ? 1 : 0);
        }

        //pushes the value
        state.pushOperand(valToPush);
    }
}
