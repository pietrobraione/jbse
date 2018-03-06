package jbse.mem;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.bc.ClassFileSnippet;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * Class representing the activation {@link Frame} of a bytecode snippet.
 * This frame has no local variables and its own operand stack.
 */
public final class SnippetFrameNoContext extends Frame implements Cloneable {
    /** The frame's operand stack. */ 
    private OperandStack operandStack; //not final because of clone

    /**
     * Constructor.
     * 
     * @param snippet a {@link Snippet}.
     * @param definingClassLoader an {@code int}, the defining classloader 
     *        assumed for the {@link ClassFileSnippet} that will be
     *        created.
     * @param packageName a {@code String}, the name of the package where the
     *        created {@link ClassFileSnippet} must be assumed to reside.
     */
    public SnippetFrameNoContext(Snippet snippet, int definingClassLoader, String packageName) {
        super(new ClassFileSnippet(snippet, definingClassLoader, packageName), snippet.getBytecode());
        this.operandStack = new OperandStack();
    }
    
    @Override
    public Collection<Value> operands() {
        return this.operandStack.values();
    }

    @Override
    public int getSourceRow() {
        return -1;
    }
    
    @Override
    public Signature getCurrentMethodSignature() {
        return new Signature(null, null, null);
    }

    @Override
    public SortedMap<Integer, Variable> localVariables() {
        return new TreeMap<>();
    }

    @Override
    public String getLocalVariableDeclaredName(int slot) {
        return null;
    }

    @Override
    public Value getLocalVariableValue(int slot) throws InvalidSlotException {
        throw new InvalidSlotException("A " + getClass().getName() + " has no local variables.");
    }

    @Override
    public Value getLocalVariableValue(String name) {
        return null;
    }

    @Override
    public void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException {
        throw new InvalidSlotException("A " + getClass().getName() + " has no local variables.");
    }

    @Override
    public void push(Value item) {
        this.operandStack.push(item);
    }

    @Override
    public Value pop() throws InvalidNumberOfOperandsException {
        return this.operandStack.pop();
    }

    @Override
    public void pop(int num) throws InvalidNumberOfOperandsException {
        this.operandStack.pop(num);
    }

    @Override
    public Value top() throws InvalidNumberOfOperandsException {
        return this.operandStack.top();
    }

    @Override
    public Value[] operands(int num) throws InvalidNumberOfOperandsException {
        return this.operandStack.operands(num);
    }

    @Override
    public void clear() {
        this.operandStack.clear();
    }

    @Override
    public void setArgs(Value... args) throws InvalidSlotException {
        throw new InvalidSlotException("A " + getClass().getName() + " has no local variables.");
    }
    
    @Override
    public SnippetFrameNoContext clone() {
        final SnippetFrameNoContext o = (SnippetFrameNoContext) super.clone();
        o.operandStack = o.operandStack.clone();
        return o;
    }

    @Override
    public String toString(){
        String tmp = "[";
        tmp += "Snippet:" + getCode().toString() + ", ";
        tmp += "ProgramCounter:" + getProgramCounter() + ", ";
        tmp += "ReturnProgramCounter:" + (getReturnProgramCounter() == UNKNOWN_PC ? "UNKNOWN" : getReturnProgramCounter()) + ", ";
        tmp += "OperandStack:" + this.operandStack.toString() + "]";
        return tmp;
    }
}