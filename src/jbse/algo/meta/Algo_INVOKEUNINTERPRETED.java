package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.mem.Util.toPrimitive;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.Arrays;
import java.util.function.Supplier;

import jbse.algo.Algorithm;
import jbse.algo.BytecodeCooker;
import jbse.algo.BytecodeData_1ME;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.UninterpretedUnsupportedException;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

final class Algo_INVOKEUNINTERPRETED extends Algorithm<
BytecodeData_1ME,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private final Signature methodSignatureImpl; //set by constructor
    private final String functionName; //set by constructor
    
    public Algo_INVOKEUNINTERPRETED(Signature methodSignatureImpl, String functionName) {
        this.methodSignatureImpl = methodSignatureImpl;
        this.functionName = functionName;
    }
    
    private boolean isInterface; //set by cooker
    private boolean isStatic; //set by cooker
    private int pcOffset; //set by cooker
    private int nParams; //set by cooker
    private Primitive[] argsPrimitive; //set by cooker
    private char returnType; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> this.nParams;
    }
    
    @Override
    protected Supplier<BytecodeData_1ME> bytecodeData() {
        return () -> BytecodeData_1ME.withInterfaceMethod(this.isInterface).get();
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                this.isInterface = (state.getInstruction() == OP_INVOKEINTERFACE);
                this.isStatic = (state.getInstruction() == OP_INVOKESTATIC);
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
            
            //sets the program counter offset for the return point
            this.pcOffset = (this.isInterface ? 
                            INVOKEDYNAMICINTERFACE_OFFSET : 
                            INVOKESPECIALSTATICVIRTUAL_OFFSET);
            
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
            //pushes the uninterpreted function term
            try {
                state.pushOperand(state.getCalculator().applyFunction(this.returnType, this.functionName, this.argsPrimitive));
            } catch (InvalidOperandException | InvalidTypeException | 
                     ThreadStackEmptyException e) {
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
        return () -> this.pcOffset;
    }
}