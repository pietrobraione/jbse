package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.mem.Util.toPrimitive;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.Arrays;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.exc.UninterpretedUnsupportedException;
import jbse.bc.Signature;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

final class Algo_INVOKEUNINTERPRETED extends Algo_INVOKEMETA {
    
    private final Signature methodSignatureImpl; //set by constructor
    private final String functionName; //set by constructor
    
    public Algo_INVOKEUNINTERPRETED(Signature methodSignatureImpl, String functionName) {
        this.methodSignatureImpl = methodSignatureImpl;
        this.functionName = functionName;
    }
    
    private int nParams; //set by cooker
    private Primitive[] argsPrimitive; //set by cooker
    private char returnType; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> this.nParams;
    }
    
    @Override
    protected void cookMore(State state) throws UninterpretedUnsupportedException {
        //calculates the number of parameters
        final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
        this.nParams = (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);

        final char returnType = splitReturnValueDescriptor(this.methodSignatureImpl.getDescriptor()).charAt(0);
        if (!isPrimitive(returnType)) {
            throw new UninterpretedUnsupportedException("The method " + this.methodSignatureImpl + " does not return a primitive value."); 
        }

        //pops the args and checks that they are all primitive
        try {
            final Value[] args = this.data.operands();
            this.argsPrimitive = toPrimitive(this.isStatic ? args : Arrays.copyOfRange(args, 1, args.length));
        } catch (InvalidTypeException e) {
            throw new UninterpretedUnsupportedException("The method " + this.methodSignatureImpl + " has a nonprimitive argument other than 'this'."); 
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //pushes the uninterpreted function term
        try {
            state.pushOperand(state.getCalculator().applyFunction(this.returnType, this.functionName, this.argsPrimitive));
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }
    }
}