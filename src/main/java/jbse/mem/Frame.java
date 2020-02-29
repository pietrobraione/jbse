package jbse.mem;

import java.util.Collection;
import java.util.SortedMap;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * Abstract class representing the activation context of 
 * a bytecode sequence.
 */
public abstract class Frame implements Cloneable {
    /** 
     * The value for the return program counter of 
     * the topmost (current) frame.
     */
    public final static int UNKNOWN_PC = -1;

    /** 
     * The value for the source row when there is no
     * source code information.
     */
    public final static int UNKNOWN_SOURCE_ROW = -1;

    /** The {@link ClassFile} for the method class. */
    private ClassFile methodClass;
    
    /** The bytecode of the frame's method. */
    private byte[] bytecode; //not final to implement clone method (bytecode may be patched)

    /** The program counter for the frame's method. */
    private int programCounter;

    /** The program counter when the frame will be again the current one. */
    private int returnProgramCounter;
    
    /**
     * Constructor.
     * 
     * @param methodClass the {@link ClassFile} for the method class.
     * @param bytecode a {@code byte[]}, the bytecode to be executed.
     */
    public Frame(ClassFile methodClass, byte[] bytecode) {
        this.methodClass = methodClass;
        this.bytecode = bytecode.clone();
        this.programCounter = 0;
        this.returnProgramCounter = UNKNOWN_PC;
    }

    /**
     * Returns the {@link Value}s on the operand stack.
     * 
     * @return an unmodifiable {@link Collection}{@code <}{@link Value}{@code >}
     *  of the operand stack values. 
     */
    public abstract Collection<Value> operands();

    /**
     * Returns the source code row corresponding to the 
     * {@link Frame}'s program counter.
     *  
     * @return the source code row corresponding to the 
     *         current program counter, or {@code -1} 
     *         iff no debug information is available 
     *         about the source code. 
     */
    public abstract int getSourceRow();
    
    /**
     * Sets the {@link Frame}'s program counter.
     * 
     * @param programCounter the value of the program counter to set.
     * @throws InvalidProgramCounterException whenever {@code programCounter} 
     *         is out of bounds.
     */
    public final void setProgramCounter(int programCounter) throws InvalidProgramCounterException {
        boundCheckPCValue(programCounter);
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
    public final void setReturnProgramCounter(int returnProgramCounterOffset) throws InvalidProgramCounterException {
        boundCheckPCValue(this.programCounter + returnProgramCounterOffset);
        this.returnProgramCounter = this.programCounter + returnProgramCounterOffset;
    }

    private void boundCheckPCValue(int newPC) throws InvalidProgramCounterException {
        if (newPC < 0 || newPC >= this.bytecode.length) {
            throw new InvalidProgramCounterException();
        }
    }

    /**
     * Return the code of this frame.
     * 
     * @return code a {@code byte[]}, a safety copy of 
     *         the (possibly patched) frame code.
     *         The returned bytecode is patched, if the
     *         frame's bytecode.
     */
    public final byte[] getCode() {
        return this.bytecode.clone();
    }
    
    /**
     * Patches the bytecode at the current program counter.
     * 
     * @param bytecode a {@code byte}. The frame's code
     *        will be modified at the frame's program counter
     *        by replacing the pointed bytecode with {@code bytecode}.
     *        Note that the action is destructive.
     */
    public final void patchCode(byte bytecode) {
        this.bytecode[this.programCounter] = bytecode;
    }

    /**
     * Returns the {@link Frame}'s bytecode instruction pointed by 
     * the program counter.
     * 
     * @return a {@code byte} representing the 
     *         bytecode pointed by the frame's program counter.
     */
    public final byte getInstruction() {
        return this.bytecode[this.programCounter];
    }

    /**
     * Returns the {@link Frame}'s bytecode instruction pointed by 
     * the program counter plus a displacement.
     * 
     * @param displ an {@code int}, a displacement.
     * @return a {@code byte} representing the 
     *         bytecode pointed by the frame's program counter
     *         plus {@code displ}.
     * @throws InvalidProgramCounterException iff the frame's program
     *         counter plus {@code displ} does not point to 
     *         a bytecode.
     */
    public final byte getInstruction(int displ) throws InvalidProgramCounterException {
        boundCheckPCValue(this.programCounter + displ);
        return this.bytecode[this.programCounter + displ];
    }

    /**
     * Return the {@link Frame}'s program counter.
     * 
     * @return the value of program counter.
     */
    public final int getProgramCounter() {
        return this.programCounter;
    }

    /**
     * Returns the program counter of the caller frame
     * stored for a return bytecode.
     * 
     * @return an {@code int}, the return program counter.
     */
    public final int getReturnProgramCounter() {
        return this.returnProgramCounter;
    }
    
    /**
     * Returns the method class.
     * 
     * @return the {@link ClassFile} for the method class.
     */
    public final ClassFile getMethodClass() {
        return this.methodClass;
    }

    /**
     * Returns the {@link Signature} of the {@link Frame}'s 
     * current method.
     * 
     * @return a {@link Signature}.
     */
    public abstract Signature getMethodSignature();

    /**
     * Returns a read-only version of the local variable area.
     * 
     * @return a {@link SortedMap}{@code <}{@link Integer}{@code ,}{@link Variable}{@code >} 
     *         which associates every slot number in the local variable area to its
     *         {@link Variable}.
     */
    public abstract SortedMap<Integer, Variable> localVariables();

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
    public abstract String getLocalVariableDeclaredName(int slot);

    /**
     * Returns the value of a local variable in this {@link Frame}.
     * 
     * @param slot an {@code int}, the slot of the local variable.
     * @return a {@link Value}, the one stored in the local variable.
     * @throws InvalidSlotException if {@code slot} is not a valid slot number.
     */
    public abstract Value getLocalVariableValue(int slot) throws InvalidSlotException;

    /**
     * Returns the value of a local variable in this {@link Frame}.
     * 
     * @param name a {@link String}, the name of the local variable.
     *        It must be in the debug information at the frame's 
     *        current program count.
     * @return a {@link Value}, the one stored in the local variable, 
     *         or {@code null} if no variable with that name exists.
     */
    public abstract Value getLocalVariableValue(String name);

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
    public abstract void setLocalVariableValue(int slot, int currentPC, Value val) 
    throws InvalidSlotException;

    /**
     * Pushes a {@link Value} on this {@link Frame}'s operand stack.
     * 
     * @param item {@link Value} to put on the top of operand stack.
     */
    public abstract void push(Value item);

    /**
     * Return and delete the value from the top of the frame's 
     * operand stack.
     * 
     * @return the {@link Value} on the top of the operand stack.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    public abstract Value pop() throws InvalidNumberOfOperandsException;

    /**
     * Removes the topmost {@code num} elements in the operand stack.
     * 
     * @param num a nonnegative {@code int}.
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, or if 
     *         {@code num} is negative.
     */
    public abstract void pop(int num) throws InvalidNumberOfOperandsException;

    /**
     * Returns the topmost element in the operand stack, 
     * without removing it. Equivalent to {@link #operands}{@code (1)[0]}.
     * 
     * @return a {@link Value}.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    public abstract Value top() throws InvalidNumberOfOperandsException;

    /**
     * Returns the topmost {@code num} elements in the operand stack,
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
    public abstract Value[] operands(int num) throws InvalidNumberOfOperandsException;

    /**
     * Clears the operand stack.
     */
    public abstract void clear();

    /**
     * Initializes the local variables by an array 
     * of {@link Value}s.
     * 
     * @param args a varargs of {@link Value}; The 
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
    public abstract void setArgs(Value... args) throws InvalidSlotException;

    @Override
    public Frame clone() {
        final Frame o;
        try {
            o = (Frame) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        o.bytecode = o.bytecode.clone();
        return o;
    }
}