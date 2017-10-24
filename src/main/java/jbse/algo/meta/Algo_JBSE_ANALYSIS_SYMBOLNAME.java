package jbse.algo.meta;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
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
    protected void cookMore(State state) throws InterruptException {
        final Value arg = this.data.operand(0);
        if (arg.isSymbolic()) {
            try {
                ensureStringLiteral(state, this.ctx, arg.toString());
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException | DecisionException | ClasspathException e) {
                failExecution(e); //TODO is it ok?
            }
            this.toPush = state.referenceToStringLiteral(arg.toString());
        } else {
            this.toPush = Null.getInstance();
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(this.toPush);
    }
}
