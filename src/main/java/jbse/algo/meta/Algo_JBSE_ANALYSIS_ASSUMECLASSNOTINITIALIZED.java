package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

public final class Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED extends Algo_INVOKEMETA_Nonbranching {
    private Instance_JAVA_CLASS clazz; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ClasspathException, InterruptException, FrozenStateException {
        Reference javaClassRef = null; //to keep the compiler happy
        try {
            javaClassRef = (Reference) this.data.operand(0);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        if (state.isNull(javaClassRef)) {
            throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
            exitFromAlgorithm();
        }

        //gets the class
        try {
            this.clazz = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (this.clazz == null) {
                //this should never happen
                failExecution("An unresolved symbolic reference from the operand stack was detected during invocation of method jbse.meta.Analysis.assumeClassNotInitialized.");
            }
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.assumeClassNotInitialized(this.clazz.representedClass());
        };
    }
}
