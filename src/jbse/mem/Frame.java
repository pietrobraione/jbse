package jbse.mem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.LineNumberTable;
import jbse.bc.Signature;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * Class representing the activation record of a method.
 */
public class Frame implements Cloneable {
	/** 
	 * The value for the return program counter of 
	 * the topmost (current) frame.
	 */
    public final static int UNKNOWN_PC = -1;
    
    /** The signature of the frame's method. */
    private final Signature mySignature;
    
	/** The frame's method line number table. */
	private final LineNumberTable lnt;
	
    /** The bytecode of the frame's method. */
    private final byte[] bytecode;
    
	/** The frame's local variable area. */ 
    private LocalVariablesArea localVariables;

	/** The frame's operand stack. */ 
    private OperandStack operandStack;

    /** The program counter for the frame's method. */
    private int programCounter;
    
    /** The program counter when the frame will be again the current one. */
    private int returnProgramCounter;
    
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
    public Frame(Signature methodSignature, ClassFile classMethodImpl) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        this.mySignature = methodSignature;
        this.lnt = classMethodImpl.getLineNumberTable(methodSignature);
        this.bytecode = classMethodImpl.getMethodCodeBySignature(methodSignature).clone();
        this.localVariables = new LocalVariablesArea(classMethodImpl.getLocalVariableTable(methodSignature));
        this.operandStack = new OperandStack();
        this.programCounter = 0;
        this.returnProgramCounter = UNKNOWN_PC;
    }
    
    /**
     * Returns the values on the operand stack.
     * 
     * @return an unmodifiable collection of the
     *         operand stack values.
     */
    public Collection<Value> values() {
    	return this.operandStack.values();
    }
    
    /**
     * Returns the source code row corresponding to the 
     * frame's program counter.
     *  
     * @return the source code row corresponding to the 
     *         frame's program counter, or <code>-1</code> 
     *         iff no debug information is available. 
     */
	public int getSourceRow() {
    	int retVal = -1;
    	for (LineNumberTable.Row r : this.lnt) {
    		if (r.start > this.programCounter) {
    			break;
    		}
    		retVal = r.lineNumber;
    	}
    	return retVal;
    }
    
    /**
     * Sets the {@link Frame}'s program counter.
     * 
     * @param programCounter the value of the program counter to set.
     * @throws InvalidProgramCounterException whenever {@code programCounter} 
     *         is out of bounds.
     */
    public void setProgramCounter(int programCounter) throws InvalidProgramCounterException {
    	this.boundCheckPCValue(programCounter);
        this.programCounter = programCounter;
        this.returnProgramCounter = UNKNOWN_PC;
    }
    
    /**
     * Sets the {@link Frame}'s return program counter.
     * 
     * @param returnProgramCounterOffset the offset of the {@link Frame}'s
     *        return program counter from the current one.
     * @throws InvalidProgramCounterException whenever {@code returnProgramCounterOffset} 
     *         plus the current program counter is out of bounds.
     */
    public void setReturnProgramCounter(int returnProgramCounterOffset) throws InvalidProgramCounterException {
    	this.boundCheckPCValue(this.programCounter + returnProgramCounterOffset);
        this.returnProgramCounter = this.programCounter + returnProgramCounterOffset;
    }
    
    private void boundCheckPCValue(int newPC) throws InvalidProgramCounterException {
    	if (newPC < 0 || newPC >= this.bytecode.length) {
            throw new InvalidProgramCounterException();
    	}
    }

    /**
     * Return the bytecode saved for a method call
     * 
     * @return code[] the bytecode array
     */
    public byte[] getCode() {
        return this.bytecode.clone();
    }
    
    /**
     * Returns the frame's bytecode instruction pointed by 
     * the frame's program counter.
     * 
     * @return a <code>byte</code> representing the 
     *         bytecode pointed by the frame's program counter.
     */
    public byte getInstruction() {
    	return this.bytecode[this.programCounter];
    }
    
    /**
     * Returns the frame's bytecode instruction pointed by 
     * the frame's program counter.
     * 
     * @return a {@code byte} representing the 
     *         bytecode pointed by the frame's program counter
     *         plus {@code displ}.
     * @throws InvalidProgramCounterException iff the frame's program
     *         counter plus {@code displ} does not point to 
     *         a bytecode.
     */
    public byte getInstruction(int displ) throws InvalidProgramCounterException {
    	this.boundCheckPCValue(this.programCounter + displ);
    	return this.bytecode[this.programCounter + displ];
    }
    
    /**
     * Return the frame's program counter.
     * 
     * @return the value of program counter.
     */
    public int getProgramCounter() {
        return this.programCounter;
    }
    
    /**
     * Returns the program counter of the caller frame
     * stored for a return bytecode.
     * 
     * @return an {@code int}, the return program counter.
     */
    public int getReturnProgramCounter() {
        return this.returnProgramCounter;
    }
    
    /**
     * Returns the {@link Signature} of the {@link Frame}'s 
     * current method.
     * 
     * @return a {@link Signature}.
     */
    public Signature getCurrentMethodSignature() {
        return this.mySignature;
    }

    /**
     * Returns a read-only version of the local variable area.
     * 
     * @return a {@link Map}{@code <}{@link Integer}{@code ,}{@link Variable}{@code >} 
     *         which associates every slot number in the local variable area to its
     *         {@link Variable};
     */
    public Map<Integer, Variable> localVariables() {
        final HashMap<Integer, Variable> retVal = new HashMap<>();
        for (int slot : this.localVariables.slots()) {
            try {
                retVal.put(slot, this.localVariables.buildLocalVariable(slot, this.programCounter));
            } catch (InvalidSlotException e) {
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }
    
    /**
     * Returns the name of a local variable as declared in 
     * the debug information of the class.
     *  
     * @param slot the number of the slot of a local variable.
     * @return a {@link String} containing the name of the local
     *         variable at {@code slot} as from the available debug 
     *         information, depending on the frame's program counter,
     *         or {@code null} if no debug information is 
     *         available for the {@code (slot, curPC)} combination.
     */
    public String getLocalVariableDeclaredName(int slot) {
        return this.localVariables.getLocalVariableDeclaredName(slot, this.programCounter);
    }
    
    /**
     * Returns the value of a local variable in this {@link Frame}.
     * 
     * @param slot an {@code int}, the slot of the local variable.
     * @return a {@link Value}, the one stored in the local variable.
     * @throws InvalidSlotException if {@code slot} is not a valid slot number.
     */
    public Value getLocalVariableValue(int slot) throws InvalidSlotException {
    	return this.localVariables.get(slot);
    }
    
    /**
     * Returns the value of a local variable in this {@link Frame}.
     * 
     * @param name a {@link String}, the name of the local variable.
     *        It must be in the debug information at the frame's 
     *        current program count.
     * @return a {@link Value}, the one stored in the local variable, 
     *         or {@code null} if no variable with that name exists.
     */
    public Value getLocalVariableValue(String name) {
    	for (Variable v : localVariables().values()) {
    		if (v.getName().equals(name)) {
    			return v.getValue();
    		}
    	}
    	return null;
    }

    /**
     * Stores a value into a specific slot of the local variable area in 
     * this {@link Frame}.
     * 
     * @param slot an {@code int}, the slot of the local variable.
     * @param currentPC the current program counter; if the local variable
     *        table contains type information about the local variable, this
     *        is used to check type conformance of the access.
     * @param val the {@link Value} to be stored.  
     * @throws InvalidSlotException if {@code slot} is not a valid slot number.
     */
    public void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException {
    	this.localVariables.set(slot, currentPC, val);
    }
    
    /**
     * Pushes a {@link Value} on the frame's operand stack.
     * 
     * @param item {@link Value} to put on the top of operand stack.
     */
    public void push(Value item) {
        this.operandStack.push(item);
    }
    
    /**
     * Return and delete the value from the top of the frame's 
     * operand stack.
     * 
     * @return the {@link Value} on the top of the operand stack.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    public Value pop() throws InvalidNumberOfOperandsException {
        return this.operandStack.pop();
    }
    
    /**
     * Removes the topmost {@code num} elements in the operand stack.
     * 
     * @param num a nonnegative {@code int}.
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, or if 
     *         {@code num} is negative.
     */
    public void pop(int num) throws InvalidNumberOfOperandsException {
        this.operandStack.pop(num);
    }

    /**
     * Returns the topmost element in the frame's operand stack, 
     * without removing it. Equivalent to {@link #operands(1)}{@code [0]}.
     * 
     * @return a {@link Value}.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    public Value top() throws InvalidNumberOfOperandsException {
        return this.operandStack.top();
    }

    /**
     * Returns the topmost {@code num} elements in the frame's operand stack,
     * without removing them.
     * 
     * @param num a nonnegative {@code int}.
     * @return a {@link Value}{@code []} containing the first {@code num}
     *         elements of the operand stack in reverse depth order 
     *         (the topmost value in the operand stack will be the last
     *         value in the return value).
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, 
     *         or if {@code num} is negative. 
     */
    public Value[] operands(int num) throws InvalidNumberOfOperandsException {
        return this.operandStack.operands(num);
    }
    
    /**
     * Clears the operand stack.
     */
    public void clear() {
    	this.operandStack.clear();
    }
    
    /**
     * Initializes the local variables by an array 
     * of {@link Value}s.
     * 
     * @param args a {@link Value}{@code []}; The 
     *        local variables are initialized in sequence 
     *        with these values. 
     *        If there are less values in {@code args} than 
     *        local variables in this object, 
     *        the remaining variables are initialized to 
     *        their default value, according to their type.If  
	 *        {@code args[i] == null}, the i-th local variable will be 
	 *        initialized to its default value. Counting of variables 
	 *        does <em>not</em> follow Java's local variable table convention 
	 *        of variables with size two - i.e., if {@code args[i]} is the 
	 *        initialization value of local variable k, and this variable 
	 *        has type double, then the initialization value of the next 
	 *        local variable k + 2 is contained in {@code args[i + 1]}. 
	 *        If {@code args == null || args.length == 0} all the 
	 *        local variables will be initialized to their default values.
     * @throws InvalidSlotException when there are 
     *         too many {@code arg}s or some of their types are 
     *         incompatible with their respective slots types.
     */
	public void setArgs(Value[] args) throws InvalidSlotException {
		this.localVariables.setArgs(args);
	}

	@Override
    public Frame clone() {
        final Frame o;
        try {
            o = (Frame) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        o.operandStack = o.operandStack.clone();
        o.localVariables = o.localVariables.clone();
        
        return o;
    }

	@Override
	public String toString(){
    	String tmp = "[";
        tmp += "Method:" + mySignature.toString() + ", ";
        tmp += "ProgramCounter:" + programCounter + ", ";
        tmp += "ReturnProgramCounter:" + (returnProgramCounter == UNKNOWN_PC ? "UNKNOWN" : returnProgramCounter) + ", ";
        tmp += "OperandStack:" + operandStack.toString() +", ";
        tmp += "Locals:" + localVariables.toString() + "]";
        return tmp;
    }
}