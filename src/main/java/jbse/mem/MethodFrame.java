package jbse.mem;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.bc.ClassFile;
import jbse.bc.LineNumberTable;
import jbse.bc.Signature;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidSlotException;
import jbse.val.Value;

/**
 * Class representing the activation {@link Frame} of a method invocation.
 */
public final class MethodFrame extends Frame implements Cloneable {
    /** The signature of the frame's method. */
    private final Signature methodSignature;

    /** The frame's method line number table. */
    private final LineNumberTable lnt;

    /** The frame's local variable area. */ 
    private LocalVariablesArea localVariables; //not final because of clone

    /** The frame's operand stack. */ 
    private OperandStack operandStack; //not final because of clone

    /**
     * Constructor.
     * 
     * @param methodSignature the {@link Signature} of the frame's method.
     * @param classMethodImpl the {@link ClassFile} where the frame's 
     *        method implementation resides.
     * @throws MethodNotFoundException when {@code classMethodImpl} does
     *         not contain the method {@code methodSignature}.
     * @throws MethodCodeNotFoundException when {@code classMethodImpl}
     *         contains the method {@code methodSignature} but it is
     *         abstract.
     */
    public MethodFrame(Signature methodSignature, ClassFile classMethodImpl) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        super(classMethodImpl, classMethodImpl.getMethodCodeBySignature(methodSignature));
        this.methodSignature = methodSignature;
        this.lnt = classMethodImpl.getLineNumberTable(methodSignature);
        this.localVariables = new LocalVariablesArea(classMethodImpl.getLocalVariableTable(methodSignature));
        this.operandStack = new OperandStack();
    }

    @Override
    public Collection<Value> operands() {
        return this.operandStack.values();
    }

    @Override
    public int getSourceRow() {
        int retVal = UNKNOWN_SOURCE_ROW;
        for (LineNumberTable.Row r : this.lnt) {
            if (r.start > getProgramCounter()) {
                break;
            }
            retVal = r.lineNumber;
        }
        return retVal;
    }
    
    @Override
    public Signature getMethodSignature() {
        return this.methodSignature;
    }

    @Override
    public SortedMap<Integer, Variable> localVariables() {
        final TreeMap<Integer, Variable> retVal = new TreeMap<>();
        for (int slot : this.localVariables.slots()) {
            try {
                retVal.put(slot, this.localVariables.buildLocalVariable(slot, getProgramCounter()));
            } catch (InvalidSlotException e) {
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    public String getLocalVariableDeclaredName(int slot) {
        return this.localVariables.getLocalVariableDeclaredName(slot, getProgramCounter());
    }

    @Override
    public Value getLocalVariableValue(int slot) throws InvalidSlotException {
        return this.localVariables.get(slot);
    }

    @Override
    public Value getLocalVariableValue(String name) {
        for (Variable v : localVariables().values()) {
            if (v.getName().equals(name)) {
                return v.getValue();
            }
        }
        return null;
    }

    @Override
    public void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException {
        this.localVariables.set(slot, currentPC, val);
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
        this.localVariables.setArgs(args);
    }

    @Override
    public MethodFrame clone() {
        final MethodFrame o = (MethodFrame) super.clone();
        o.localVariables = o.localVariables.clone();
        o.operandStack = o.operandStack.clone();
        return o;
    }

    @Override
    public String toString(){
        String tmp = "[";
        tmp += "Method:" + this.methodSignature.toString() + ", ";
        tmp += "ProgramCounter:" + getProgramCounter() + ", ";
        tmp += "ReturnProgramCounter:" + (getReturnProgramCounter() == UNKNOWN_PC ? "UNKNOWN" : getReturnProgramCounter()) + ", ";
        tmp += "OperandStack:" + this.operandStack.toString() + ", ";
        tmp += "Locals:" + this.localVariables.toString() + "]";
        return tmp;
    }
    
    //these are just to implement toString in SnippetFrameContext
    
    OperandStack getOperandStack() {
        return this.operandStack;
    }
    
    LocalVariablesArea getLocalVariableArea() {
        return this.localVariables;
    }
}