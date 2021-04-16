package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isInstance(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_ISINSTANCE extends Algo_INVOKEMETA_Nonbranching {
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
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (clazz == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
            }
            final ClassFile representedClass = clazz.representedClass();

            //gets the reference to the object to be checked
            final Reference objRef = (Reference) this.data.operand(1);
            
            //determines which value to push on the operand stack
            if (clazz.representedClass().isPrimitiveOrVoid() || state.isNull(objRef)) {
                this.valToPush = calc.valInt(0);
            } else {
                //checks whether the object's class is a subclass 
                //of the class name from the constant pool
                final Objekt obj = state.getObject(objRef);
                final ClassFile objClass = obj.getType();
                this.valToPush = calc.valInt(objClass.isSubclass(representedClass) ? 1 : 0);
            }
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
