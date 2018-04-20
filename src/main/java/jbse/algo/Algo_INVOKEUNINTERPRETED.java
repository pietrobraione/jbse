package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.function.Supplier;

import jbse.algo.exc.UninterpretedUnsupportedException;
import jbse.bc.Signature;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} implementing the effect of a method call that
 * produces as result a symbolic uninterpreted function application
 * on its arguments. Works only for methods that accept as parameters
 * (except possibly the {@code this} parameter) and produce as return
 * value only primitive values.
 * 
 * @author Pietro Braione
 */
public final class Algo_INVOKEUNINTERPRETED extends Algo_INVOKEMETA_Nonbranching {

    private final Signature methodSignatureImpl; //set by constructor
    private final String functionName; //set by constructor

    public Algo_INVOKEUNINTERPRETED(Signature methodSignatureImpl, String functionName) {
        this.methodSignatureImpl = methodSignatureImpl;
        this.functionName = functionName;
    }

    private Value[] args; //set by cooker
    private String returnType; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> {
            final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
            return (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
        };
    }

    @Override
    protected void cookMore(State state) throws UninterpretedUnsupportedException {
        //gets the return type
        this.returnType = splitReturnValueDescriptor(this.methodSignatureImpl.getDescriptor());

        //pops the args
        this.args = this.data.operands();
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //pushes the uninterpreted function term
        try {
            if (isPrimitive(this.returnType)) {
                state.pushOperand(state.getCalculator().applyFunctionPrimitive(this.returnType.charAt(0), state.getHistoryPoint(), this.functionName, this.args));
            } else {
                state.pushOperand(new ReferenceSymbolicApply(this.returnType, state.getHistoryPoint(), this.functionName, this.args));
            }
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }
    }
}