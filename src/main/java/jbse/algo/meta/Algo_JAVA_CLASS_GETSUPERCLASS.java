package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Class#getSuperclass()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETSUPERCLASS extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete refSuper; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject((Reference) this.data.operand(0));
            if (clazz == null) {
                //this should never happen
                failExecution("Violated invariant (unexpected heap access with symbolic unresolved reference).");
            }
            final ClassFile cf = clazz.representedClass();
            final ClassFile superClass = cf.getSuperclass();
            if (superClass == null) {
                this.refSuper = Null.getInstance();
            } else {
                state.ensureInstance_JAVA_CLASS(calc, superClass);
                this.refSuper = state.referenceToInstance_JAVA_CLASS(superClass);
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.refSuper);
        };
    }
}
