package jbse.algo;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Array;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Value;

/**
 * Algorithm for the completion of the INVOKEDYNAMIC bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_INVOKEDYNAMIC_Completion extends Algorithm<
BytecodeData_1CS,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
	private ClassFile adapterClass; //set by Algo_INVOKEDYNAMIC
	private Signature adapterSignature; //set by Algo_INVOKEDYNAMIC
	private boolean hasAppendix; //set by Algo_INVOKEDYNAMIC
	private Array appendixArray; //set by Algo_INVOKEDYNAMIC
	private Value[] parameters; //set by bytecodeCooker

	public void setAdapterClass(ClassFile adapterClass) {
		this.adapterClass = adapterClass;
	}

	public void setAdapterSignature(Signature adapterSignature) {
		this.adapterSignature = adapterSignature;
	}

	public void setHasAppendix(boolean hasAppendix) {
		this.hasAppendix = hasAppendix;
	}

	public void setAppendixArray(Array appendixArray) {
		this.appendixArray = appendixArray;
	}

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> (splitParametersDescriptors(this.adapterSignature.getDescriptor()).length - (this.hasAppendix ? 1 : 0));
    }

    @Override
    protected Supplier<BytecodeData_1CS> bytecodeData() {
        return BytecodeData_1CS::get;
    }

	@Override
	protected BytecodeCooker bytecodeCooker() {
		return (state) -> {
        	final Calculator calc = this.ctx.getCalculator();
        	
	        //builds the parameters
	        this.parameters = new Value[this.data.operands().length + (this.hasAppendix ? 1 : 0)];
	        System.arraycopy(this.data.operands(), 0, this.parameters, 0, this.data.operands().length);
	        if (this.hasAppendix) {
	        	try {
					this.parameters[this.data.operands().length] = ((Array.AccessOutcomeInValue) this.appendixArray.getFast(calc, calc.valInt(0))).getValue();
				} catch (FastArrayAccessNotAllowedException | ClassCastException e) {
		        	//this should never happen
		        	failExecution(e);
				}
	        }
		};
	}

	@Override
	protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
		return DecisionAlternative_NONE.class;
	}

	@Override
	protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
	}

	@Override
	protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	try {
				state.pushFrame(this.ctx.getCalculator(), this.adapterClass, this.adapterSignature, false, offsetInvoke(true), this.parameters);
            } catch (InvalidProgramCounterException | InvalidSlotException e) {
                //TODO is it ok?
                throwVerifyError(state, this.ctx.getCalculator());
            } catch (NullMethodReceiverException | MethodNotFoundException | MethodCodeNotFoundException e) {
                //this should never happen
                failExecution(e);
			}
        };
	}

	@Override
	protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
	}

	@Override
	protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
	}
}
