package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.ClassHierarchy;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isAssignableFrom(Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_ISASSIGNABLEFROM extends Algo_INVOKEMETA_Nonbranching {
    private String classThis; //set by cookMore
    private String classOther; //set by cookMore
    
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
            final Reference javaClassRefThis = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRefThis)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS javaClassThis = (Instance_JAVA_CLASS) state.getObject(javaClassRefThis);
            if (javaClassThis == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            this.classThis = javaClassThis.representedClass();
            
            //gets the reference to the class to be checked
            final Reference javaClassRefOther = (Reference) this.data.operand(1);
            if (state.isNull(javaClassRefOther)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS javaClassOther = (Instance_JAVA_CLASS) state.getObject(javaClassRefThis);
            if (javaClassOther == null) {
                //this should never happen
                failExecution("The class parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            this.classOther = javaClassOther.representedClass();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        final ClassHierarchy hier = state.getClassHierarchy();
        final Simplex valToPush = state.getCalculator().valInt(hier.isSubclass(this.classOther, this.classThis) ? 1 : 0);
        state.pushOperand(valToPush);
    }
}
