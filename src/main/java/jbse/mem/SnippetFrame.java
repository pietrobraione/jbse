package jbse.mem;

import java.util.Collection;
import java.util.SortedMap;

import jbse.bc.Signature;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * Class representing the activation {@link Frame} of a bytecode snippet.
 * It is a subcontext of another activation {@link Frame}, of which takes
 * the local variables and the operand stack.
 */
public final class SnippetFrame extends Frame implements Cloneable {
    /** The {@link Frame} context this {@link SnippetFrame} must execute. */ 
    private Frame contextFrame; //not final only because it must be cloneable
    
    /**
     * Constructor.
     * 
     * @param contextFrame a {@link Frame}, the activation context 
     *        this {@link SnippetFrame} is a subcontext of. 
     * @param bytecode a {@code byte[]}, the bytecode to be executed.
     */
    public SnippetFrame(Frame contextFrame, byte[] bytecode) {
        super(bytecode);
        this.contextFrame = contextFrame;
    }
    
    /**
     * Returns the activation context this {@link SnippetFrame}
     * is a subcontext of.
     * 
     * @return a {@link Frame}.
     */
    public Frame getContextFrame() {
        return this.contextFrame;
    }
    
    @Override
    public Collection<Value> values() {
        return this.contextFrame.values();
    }

    @Override
    public int getSourceRow() {
        return this.contextFrame.getSourceRow();
    }

    @Override
    public Signature getCurrentMethodSignature() {
        return this.contextFrame.getCurrentMethodSignature();
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
    public SnippetFrame clone() {
        final SnippetFrame o = (SnippetFrame) super.clone();
        o.contextFrame = this.contextFrame.clone();
        return o;
    }

    @Override
    public String toString(){
        String tmp = "[";
        tmp += "Snippet:" + getCode().toString() + ", ";
        tmp += "ProgramCounter:" + getProgramCounter() + ", ";
        tmp += "ReturnProgramCounter:" + (getReturnProgramCounter() == UNKNOWN_PC ? "UNKNOWN" : getReturnProgramCounter()) + "]";
        return tmp;
    }
}