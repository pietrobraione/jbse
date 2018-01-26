package jbse.algo.meta;

import static jbse.algo.Util.continueWithBaseLevelImpl;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_STRING;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.Snippet;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of the many {@link java.lang.StringBuilder#append}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_STRINGBUILDER_APPEND extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, InvalidInputException, ClasspathException {
        try {
            final Primitive toAppend = (Primitive) this.data.operand(1);
            if (toAppend.isSymbolic()) {
                final String stringifiedSymbol = toAppend.toString();
                state.ensureStringLiteral(stringifiedSymbol);
                final ReferenceConcrete refStringifiedSymbol = state.referenceToStringLiteral(stringifiedSymbol);
                state.pushOperand(this.data.operand(0)); //this
                state.pushOperand(refStringifiedSymbol);
                final Snippet snippet = state.snippetFactory()
                    .op_invokevirtual(JAVA_STRINGBUILDER_APPEND_STRING)
                    .op_return()
                    .mk();
                state.pushSnippetFrameWrap(snippet, INVOKESPECIALSTATICVIRTUAL_OFFSET);
                exitFromAlgorithm();
            } else {
                continueWithBaseLevelImpl(state, this.isInterface, this.isSpecial, this.isStatic); //executes the original StringBuilder.append implementation
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (InvalidProgramCounterException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //never used
        };
    }
}
