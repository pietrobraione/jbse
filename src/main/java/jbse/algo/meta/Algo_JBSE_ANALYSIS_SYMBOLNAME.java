package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.ReferenceConcrete;
import jbse.val.Value;

/**
 * An {@link Algorithm} implementing {@link jbse.meta.Analysis#symbolName(boolean)},
 * {@link jbse.meta.Analysis#symbolName(byte)},
 * {@link jbse.meta.Analysis#symbolName(char)},
 * {@link jbse.meta.Analysis#symbolName(double)},
 * {@link jbse.meta.Analysis#symbolName(float)},
 * {@link jbse.meta.Analysis#symbolName(int)},
 * {@link jbse.meta.Analysis#symbolName(long)}, and
 * {@link jbse.meta.Analysis#symbolName(short)}.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_ANALYSIS_SYMBOLNAME extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete toPush;
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, InvalidInputException {
        final Value arg = this.data.operand(0);
        if (arg.isSymbolic()) {
            try {
                state.ensureStringLiteral(this.ctx.getCalculator(), arg.toString());
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
            this.toPush = state.referenceToStringLiteral(arg.toString());
        } else {
            this.toPush = Null.getInstance();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.toPush);
        };
    }
}
