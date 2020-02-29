package jbse.mem;

import java.util.Collection;
import java.util.SortedMap;

import jbse.bc.ClassFileSnippetWrap;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.common.exc.InvalidInputException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * The activation {@link Frame} of a bytecode snippet that is a subcontext 
 * of another activation {@link Frame}, of which uses the local variables 
 * and the operand stack.
 */
public final class SnippetFrameWrap extends Frame implements Cloneable {
    /** The {@link Frame} context this {@link SnippetFrame} must execute. */ 
    private MethodFrame contextFrame; //not final only because it must be cloneable
    
    /**
     * Constructor.
     * 
     * @param snippet a {@link Snippet}.
     * @param contextFrame a {@link MethodFrame}, the activation context 
     *        this {@link SnippetFrameWrap} is a subcontext of. 
     * @throws InvalidInputException if {@code contextFrame.}{@link MethodFrame#getMethodClass() getCurrentClass()} 
     *         is a snippet classfile.
     */
    public SnippetFrameWrap(Snippet snippet, MethodFrame contextFrame) throws InvalidInputException {
        super(new ClassFileSnippetWrap(snippet, contextFrame.getMethodClass()), snippet.getBytecode());
        this.contextFrame = contextFrame;
    }
    
    /**
     * Returns the activation context this {@link SnippetFrameWrap}
     * is a subcontext of.
     * 
     * @return a {@link Frame}.
     */
    public Frame getContextFrame() {
        return this.contextFrame;
    }
    
    @Override
    public Collection<Value> operands() {
        return this.contextFrame.operands();
    }

    @Override
    public int getSourceRow() {
        return this.contextFrame.getSourceRow();
    }

    @Override
    public Signature getMethodSignature() {
        return this.contextFrame.getMethodSignature();
    }

    @Override
    public SortedMap<Integer, Variable> localVariables() {
        return this.contextFrame.localVariables();
    }

    @Override
    public String getLocalVariableDeclaredName(int slot) {
        return this.contextFrame.getLocalVariableDeclaredName(slot);
    }

    @Override
    public Value getLocalVariableValue(int slot) throws InvalidSlotException {
        return this.contextFrame.getLocalVariableValue(slot);
    }

    @Override
    public Value getLocalVariableValue(String name) {
        return this.contextFrame.getLocalVariableValue(name);
    }

    @Override
    public void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException {
        this.contextFrame.setLocalVariableValue(slot, currentPC, val);
    }

    @Override
    public void push(Value item) {
        this.contextFrame.push(item);
    }

    @Override
    public Value pop() throws InvalidNumberOfOperandsException {
        return this.contextFrame.pop();
    }

    @Override
    public void pop(int num) throws InvalidNumberOfOperandsException {
        this.contextFrame.pop(num);
    }

    @Override
    public Value top() throws InvalidNumberOfOperandsException {
        return this.contextFrame.top();
    }

    @Override
    public Value[] operands(int num) throws InvalidNumberOfOperandsException {
        return this.contextFrame.operands(num);
    }

    @Override
    public void clear() {
        this.contextFrame.clear();
    }

    @Override
    public void setArgs(Value... args) throws InvalidSlotException {
        this.contextFrame.setArgs(args);
    }
    
    @Override
    public SnippetFrameWrap clone() {
        final SnippetFrameWrap o = (SnippetFrameWrap) super.clone();
        o.contextFrame = this.contextFrame.clone();
        return o;
    }

    @Override
    public String toString(){
        String tmp = "[";
        tmp += "Method:" + getMethodSignature().toString() + ", ";
        tmp += "Snippet:" + getCode().toString() + ", ";
        tmp += "ProgramCounter:" + getProgramCounter() + ", ";
        tmp += "ReturnProgramCounter:" + (getReturnProgramCounter() == UNKNOWN_PC ? "UNKNOWN" : getReturnProgramCounter()) + ", ";
        tmp += "OperandStack:" + this.contextFrame.getOperandStack().toString() +", ";
        tmp += "Locals:" + this.contextFrame.getLocalVariableArea().toString() + "]";
        return tmp;
    }
}
