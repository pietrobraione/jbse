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
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getComponentType()}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JAVA_CLASS_GETCOMPONENTTYPE extends Algo_INVOKEMETA_Nonbranching {
    private Reference componentClassRef;  //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException {
        try {           
            //gets the canonical name of the primitive type and converts it to a string
            final Reference classRef = (Reference) this.data.operand(0);
            if (state.isNull(classRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getComponentType method is null.");
            }
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            final ClassFile classFile = clazz.representedClass();
            if (classFile.isArray()) {
                final ClassFile componentType = classFile.getMemberClass();
                state.ensureInstance_JAVA_CLASS(this.ctx.getCalculator(), componentType);
                this.componentClassRef = state.referenceToInstance_JAVA_CLASS(componentType);
            } else {
                //not an array
                this.componentClassRef = Null.getInstance();
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.componentClassRef);
        };
    }
}
