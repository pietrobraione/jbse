package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isAssignableFrom(Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_ISASSIGNABLEFROM extends Algo_INVOKEMETA_Nonbranching {
    private Simplex valToPush; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException, ClasspathException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and the name of the class it represents
            final Reference javaClassRefThis = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRefThis)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is null.");
            }
            final Instance_JAVA_CLASS javaClassThis = (Instance_JAVA_CLASS) state.getObject(javaClassRefThis);
            if (javaClassThis == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            final ClassFile classThis = javaClassThis.representedClass();

            //gets the reference to the class to be checked
            final Reference javaClassRefOther = (Reference) this.data.operand(1);
            if (state.isNull(javaClassRefOther)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance_JAVA_CLASS javaClassOther = (Instance_JAVA_CLASS) state.getObject(javaClassRefOther);
            if (javaClassOther == null) {
                //this should never happen
                failExecution("The class parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            final ClassFile classOther = javaClassOther.representedClass();

            //calculates the value to push
            this.valToPush = calc.valInt(classOther.isSubclass(classThis) ? 1 : 0);
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.valToPush);
        };
    }
}
