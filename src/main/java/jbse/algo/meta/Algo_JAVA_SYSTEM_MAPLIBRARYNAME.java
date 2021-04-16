package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.System#mapLibraryName(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_SYSTEM_MAPLIBRARYNAME extends Algo_INVOKEMETA_Nonbranching {
    private Reference retVal; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws InterruptException, SymbolicValueNotAllowedException, 
    ClasspathException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            final Reference refString = (Reference) this.data.operand(0);
            if (state.isNull(refString)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
            }
            final String theString = valueString(state, refString);
            if (theString == null) {
                throw new SymbolicValueNotAllowedException("The parameter to java.lang.System.mapLibraryName was not a concrete string of characters.");
            }
            final String theResult = System.mapLibraryName(theString);
            try {
                state.ensureStringLiteral(calc, theResult);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
            this.retVal = state.referenceToStringLiteral(theResult);
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
    	return (state, alt) -> {
    		state.pushOperand(this.retVal);
    	};
    }
}
