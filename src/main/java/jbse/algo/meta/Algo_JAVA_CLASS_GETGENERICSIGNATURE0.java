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
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getGenericSignature0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETGENERICSIGNATURE0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference refGenericSignature; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException, ClasspathException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and the classfile of the class it represents
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getGenericSignature0 method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (clazz == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getGenericSignature0 method is symbolic and unresolved.");
            }
            final String genericSignature = clazz.representedClass().getGenericSignatureType();
            if (genericSignature == null) {
                this.refGenericSignature = Null.getInstance();
            } else {
                state.ensureStringLiteral(calc, genericSignature);
                this.refGenericSignature = state.referenceToStringLiteral(genericSignature);
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
            state.pushOperand(this.refGenericSignature);
        };
    }
}
