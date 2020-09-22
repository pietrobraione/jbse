package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INSTANTIATION_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#allocateInstance(Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_ALLOCATEINSTANCE extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile classFile; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) 
    throws FrozenStateException, UndefinedResultException, ClasspathException, InterruptException {
        try {
            final Reference refParam = (Reference) this.data.operand(1);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refParam);
            this.classFile = clazz.representedClass();
            if (this.classFile.isPrimitiveOrVoid()) {
                //this is the behavior of Hotspost
                throw new UndefinedResultException("Invoked method sun.misc.Unsafe.allocateInstance with a primitive or void class parameter.");
            } else if (this.classFile.isArray()) {
                //this is also the behavior of Hotspost
                throwNew(state, this.ctx.getCalculator(), INSTANTIATION_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException | NullPointerException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
                final ReferenceConcrete ref = state.createInstance(calc, this.classFile);
                state.pushOperand(ref);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            }
        };
    }
}
