package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.String#intern()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_STRING_INTERN extends Algo_INVOKEMETA_Nonbranching {
    private String valueString; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws DecisionException, ClasspathException, 
    SymbolicValueNotAllowedException, InterruptException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            this.valueString = valueString(state, (Reference) this.data.operand(0));
            if (this.valueString == null) {
                //TODO remove this limitation
                throw new SymbolicValueNotAllowedException("Cannot intern a String object that is not simple.");
            }
            if (state.hasStringLiteral(this.valueString)) {
                //nothing to do
            } else {
                state.ensureStringLiteral(calc, this.valueString);
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
            state.pushOperand(state.referenceToStringLiteral(this.valueString));
        };
    }
}
