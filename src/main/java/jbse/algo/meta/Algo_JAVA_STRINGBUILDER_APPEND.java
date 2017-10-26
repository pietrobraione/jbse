package jbse.algo.meta;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_STRING;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.String#hashCode()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_STRINGBUILDER_APPEND extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete refStringifiedSymbol; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws ThreadStackEmptyException, InterruptException {
        try {
            final Primitive toAppend = (Primitive) this.data.operand(1);
            if (toAppend.isSymbolic()) {
                final String stringifiedSymbol = toAppend.toString();
                try {
                    ensureStringLiteral(state, this.ctx, stringifiedSymbol);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (ClassFileIllFormedException | DecisionException | 
                         ClasspathException e) {
                    //this should not happen
                    failExecution(e);
                }
                this.refStringifiedSymbol = state.referenceToStringLiteral(stringifiedSymbol);
            } else {
                continueWithBaseLevelImpl(state); //executes the original StringBuilder.append implementation
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException, InterruptException {
        //TODO is this the right order of the operands?
        state.pushOperand(this.data.operand(0)); //this
        state.pushOperand(this.refStringifiedSymbol);
        continueWithAnotherMethod(state, JAVA_STRINGBUILDER_APPEND_STRING, false, false, false);
    }
}
