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
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of the many {@link java.lang.StringBuilder#append}.
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
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, InvalidInputException, ClasspathException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            final Primitive toAppend = (Primitive) this.data.operand(1);
            if (toAppend.isSymbolic()) {
                final String stringifiedSymbol = toAppend.toString();
                state.ensureStringLiteral(calc, stringifiedSymbol);
                this.refStringifiedSymbol = state.referenceToStringLiteral(stringifiedSymbol);
            } else {
                continueWithBaseLevelImpl(state, this.isInterface, this.isSpecial, this.isStatic); //executes the original StringBuilder.append implementation
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
        	try {
        		final Snippet snippet = state.snippetFactoryNoWrap()
        				.addArg(this.data.operand(0)) //this
        				.addArg(this.refStringifiedSymbol)
        				.op_aload((byte) 0) //this
        				.op_aload((byte) 1)
        				.op_invokevirtual(JAVA_STRINGBUILDER_APPEND_STRING)
        				.op_areturn()
        				.mk();
        		state.pushSnippetFrameNoWrap(snippet, INVOKESPECIALSTATICVIRTUAL_OFFSET);
            } catch (InvalidProgramCounterException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}
