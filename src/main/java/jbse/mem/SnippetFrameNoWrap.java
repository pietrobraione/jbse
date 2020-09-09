package jbse.mem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.bc.ClassFile;
import jbse.bc.ClassFileSnippetNoWrap;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * The activation {@link Frame} of a bytecode snippet that has 
 * its own operand stack and possibly a number of read-only 
 * local variables initialized with the snippet's args.
 */
public final class SnippetFrameNoWrap extends Frame implements Cloneable {
    /** The frame's operand stack. */ 
    private OperandStack operandStack; //not final because of clone

    /** The frame's local variables (immutable) values. */ 
    private final ArrayList<Value> localVariablesValues;

    /**
     * Constructor.
     * 
     * @param snippet a {@link Snippet}.
     * @param hostClass a {@code ClassFile}, the host class 
     *        assumed for the {@link ClassFileSnippetNoWrap} 
     *        that will be created.
     * @param className a {@code String}, the name of the
     *        created {@link ClassFileSnippetNoWrap}. It must be unique in the dynamic package.
     */
    public SnippetFrameNoWrap(Snippet snippet, ClassFile hostClass, String className) {
        super(new ClassFileSnippetNoWrap(snippet, hostClass, className), snippet.getBytecode());
        this.operandStack = new OperandStack();
        this.localVariablesValues = new ArrayList<>(snippet.getArgs());
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
    public Signature getMethodSignature() {
        return new Signature(null, null, null);
    }

    /**
     * {@inheritDoc}
     * 
     * A {@link SnippetFrameNoWrap} will always return 
     * an empty map.
     */
    @Override
    public SortedMap<Integer, Variable> localVariables() {
        return new TreeMap<>();
    }

    /**
     * {@inheritDoc}
     * 
     * A {@link SnippetFrameNoWrap} will always return 
     * {@code null}.
     */
    @Override
    public String getLocalVariableDeclaredName(int slot) {
        return null;
    }

    @Override
    public Value getLocalVariableValue(int slot) throws InvalidSlotException {
    	if (slot < 0 || slot >= this.localVariablesValues.size()) {
    		throw new InvalidSlotException("This " + getClass().getName() + " has " + this.localVariablesValues.size() + " local variables, but an access was attempted with slot number " + slot + ".");
    	}
    	return this.localVariablesValues.get(slot);
    }

    /**
     * {@inheritDoc}
     * 
     * A {@link SnippetFrameNoWrap} will always return 
     * {@code null}.
     */
    @Override
    public Value getLocalVariableValue(String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * A {@link SnippetFrameNoWrap} will always throw an 
     * {@link InvalidSlotException}.
     */
    @Override
    public void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException {
        throw new InvalidSlotException("The local variables of a " + getClass().getName() + " are immutable. Use the operand stack instead.");
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
        throw new InvalidSlotException("Cannot set the args of a " + getClass().getName() + " by invoking setArgs. Use the constructor instead.");
    }
    
    @Override
    public SnippetFrameNoWrap clone() {
        final SnippetFrameNoWrap o = (SnippetFrameNoWrap) super.clone();
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