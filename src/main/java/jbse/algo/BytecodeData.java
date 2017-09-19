package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Util.asUnsignedByte;
import static jbse.common.Util.byteCat;
import static jbse.common.Util.byteCatShort;

import java.util.function.Supplier;

import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

/**
 * Encapsulates the data a bytecode operates on:
 * implicits (determined by the opcode), 
 * immediates (determined by the bytecodes after 
 * the opcode), and operands (from the operand stack).
 * Concrete subclasses receive implicits from the constructor, 
 * and extract immediates and operand from  
 * a {@link State}'s current method code and 
 * operand stack.
 *  
 * @author Pietro Braione
 */
public abstract class BytecodeData {    
    //bytecode data: operands
    private Value[] operands;

    //bytecode data: low-level
    private boolean nextWide;
    private byte valByte;
    private short valShort;
    private int valInt;

    //bytecode data: high-level
    private int varSlot;
    private String varName;
    private Value varValue;
    private int jumpOffset;
    private int jumpTarget;
    private Signature signature;
    private String className;
    private char primitiveType;
    private SwitchTable switchTable;

    /**
     * Reads all the data for a bytecode. 
     * 
     * @param state a {@link State}.
     * @param numOperandsSupplier a {@link Supplier}{@code <}{@link Integer}{@code >}
     *        returning the number of operands to be read from {@code state}'s 
     *        operand stack.
     * @throws ThreadStackEmptyException
     * @throws InterruptException
     */
    public final void read(State state, Supplier<Integer> numOperandsSupplier) 
    throws ThreadStackEmptyException, InterruptException {
        this.nextWide = state.nextWide();
        readImmediates(state);
        readOperands(state, numOperandsSupplier.get());
    }

    /**
     * Reads the immediates from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected abstract void readImmediates(State state) throws InterruptException;

    /**
     * Reads a signed byte immediate from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readImmediateSignedByte(State state, int immediateDisplacement) 
    throws InterruptException {
        try {
            this.valByte = state.getInstruction(immediateDisplacement);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads an unsigned byte immediate from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readImmediateUnsignedByte(State state, int immediateDisplacement) 
    throws InterruptException {
        try {
            this.valShort = asUnsignedByte(state.getInstruction(immediateDisplacement));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a signed word (2 bytes) immediate from a {@link State}'s current 
     * method code.
     * 
     * @param state a {@link State}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readImmediateSignedWord(State state, int immediateDisplacement) 
    throws InterruptException {
        try {
            this.valShort = byteCatShort(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads an unsigned word (2 bytes) immediate from a {@link State}'s current 
     * method code.
     * 
     * @param state a {@link State}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readImmediateUnsignedWord(State state, int immediateDisplacement) 
    throws InterruptException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a signed double word (4 bytes) immediate from a {@link State}'s current 
     * method code.
     * 
     * @param state a {@link State}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readImmediateSignedDword(State state, int immediateDisplacement) 
    throws InterruptException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1), 
                        state.getInstruction(immediateDisplacement + 2), state.getInstruction(immediateDisplacement + 3));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a local variable's data (name and current value).  
     * 
     * @param state a {@link State}.
     * @param varSlot an {@code int}, the number of slot of the
     *        local variable. Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readLocalVariable(State state, int varSlot) 
    throws InterruptException {
        try {
            this.varSlot = varSlot;
            this.varName = state.getLocalVariableDeclaredName(varSlot);
            this.varValue = state.getLocalVariableValue(varSlot);
        } catch (InvalidSlotException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a jump's data (target of the jump).  
     * 
     * @param state a {@link State}.
     * @param varSlot an {@code int}, the offset of the jump. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readJump(State state, int jumpOffset) {
        try {
            this.jumpOffset = jumpOffset;
            this.jumpTarget = state.getPC() + jumpOffset;
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a class name.
     * 
     * @param state a {@link State}.
     * @param classRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the class name is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readClassName(State state, int classRefIndex)
    throws InterruptException {
        try {
            this.className = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getClassSignature(classRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a field signature.
     * 
     * @param state a {@link State}.
     * @param fieldRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the field signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readFieldSignature(State state, int fieldRefIndex)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getFieldSignature(fieldRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads an interface method signature.
     * 
     * @param state a {@link State}.
     * @param interfaceMethodRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the interface method signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readInterfaceMethodSignature(State state, int interfaceMethodRefIndex)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getInterfaceMethodSignature(interfaceMethodRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a noninterface method signature.
     * 
     * @param state a {@link State}.
     * @param noninterfaceMethodRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the noninterface method signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void readNoninterfaceMethodSignature(State state, int noninterfaceMethodRefIndex)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getMethodSignature(noninterfaceMethodRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Stores a primitive type.
     * 
     * @param state a {@link State}.
     * @param primitiveType a {@code char}, the primitive type to store.
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     */
    protected final void setPrimitiveType(State state, char primitiveType)
    throws InterruptException {
        if (isPrimitive(primitiveType)) {
            this.primitiveType = primitiveType;
        } else {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    /**
     * Stores a switch table.
     * 
     * @param switchTable the {@link SwitchTable} to store.
     */
    protected final void setSwitchTable(SwitchTable switchTable) {
        this.switchTable = switchTable;
    }

    /**
     * Reads the operands from the operand stack.
     * 
     * @param state a {@link State}.
     * @param numOperands an {@code int}, the number of 
     *        operands 
     * @throws ThreadStackEmptyException if {@code state}
     *         has an empty stack.
     * @throws InterruptException if the execution of the
     */
    private void readOperands(State state, int numOperands) 
    throws ThreadStackEmptyException, InterruptException {
        final Frame frame = state.getCurrentFrame();
        try {
            this.operands = frame.operands(numOperands);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    /**
     * Returns whether the next bytecode must be
     * executed with wide semantics.
     * 
     * @return {@code true} iff the previous bytecode
     *         was wide.
     */
    public boolean nextWide() {
        return this.nextWide;
    }

    /**
     * Returns the slot of the local variable
     * read with {@link #readLocalVariable(State, int) readLocalVariable}.
     * 
     * @return the slot of the variable.
     */
    public int localVariableSlot() {
        return this.varSlot;
    }

    /**
     * Returns the name of the local variable
     * read with {@link #readLocalVariable(State, int) readLocalVariable}.
     * 
     * @return the name of the variable or {@code null}
     *         if no debug info about the variable is 
     *         available.
     */
    public String localVariableName() {
        return this.varName;
    }

    /**
     * Returns the value of the local variable
     * read with {@link #readLocalVariable(State, int) readLocalVariable}.
     * 
     * @return the value of the variable.
     */
    public Value localVariableValue() {
        return this.varValue;
    }

    /**
     * Returns the jump offset read with 
     * {@link #readJump(State, int) readJump}.
     * 
     * @return an {@code int}, the jump offset.
     */
    public int jumpOffset() {
        return this.jumpOffset;
    }

    /**
     * Returns the jump target read with 
     * {@link #readJump(State, int) readJump}.
     * 
     * @return an {@code int}, the jump target.
     */
    public int jumpTarget() {
        return this.jumpTarget;
    }

    /**
     * Returns the signed byte read with 
     * {@link #readImmediateSignedByte(State, int) readImmediateSignedByte}.
     * 
     * @return a {@code byte}.
     */
    public byte immediateSignedByte() {
        return this.valByte;
    }

    /**
     * Returns the signed byte read with 
     * {@link #readImmediateUnsignedByte(State, int) readImmediateUnsignedByte}.
     * 
     * @return a {@code short}.
     */
    public short immediateUnsignedByte() {
        return this.valShort;
    }

    /**
     * Returns the signed word read with 
     * {@link #readImmediateSignedWord(State, int) readImmediateSignedWord}.
     * 
     * @return a {@code short}.
     */
    public short immediateSignedWord() {
        return this.valShort;
    }

    /**
     * Returns the unsigned word read with 
     * {@link #readImmediateUnsignedWord(State, int) readImmediateUnsignedWord}.
     * 
     * @return an {@code int}.
     */
    public int immediateUnsignedWord() {
        return this.valInt;
    }

    /**
     * Returns the signed word read with 
     * {@link #readImmediateSignedDword(State, int) readImmediateSignedDword}.
     * 
     * @return an {@code int}.
     */
    public int immediateSignedDword() {
        return this.valInt;
    }

    /**
     * Returns the class name read with
     * {@link #readClassName(State, int) readClassName}.
     * 
     * @return a {@link String}.
     */
    public String className() {
        return this.className;
    }

    /**
     * Returns the signature read with 
     * {@link #readFieldSignature(State, int) readFieldSignature}, 
     * {@link #readInterfaceMethodSignature(State, int) readInterfaceMethodSignature}, or
     * {@link #readNoninterfaceMethodSignature(State, int) readNoninterfaceMethodSignature}.
     *  
     * @return a {@link Signature}.
     */
    public Signature signature() {
        return this.signature;
    }

    /**
     * Returns the primitive type set with
     * {@link #setPrimitiveType(State, char) setPrimitiveType}.
     * 
     * @return a {@code char}.
     */
    public char primitiveType() {
        return this.primitiveType;
    }

    /**
     * Returns the switch table set with
     * {@link #setSwitchTable(SwitchTable) setSwitchTable}.
     * 
     * @return a {@link SwitchTable}.
     */
    public SwitchTable switchTable() {
        return this.switchTable;
    }

    /**
     * Returns all the operands from the operand stack.
     * 
     * @return a {@link Value}{@code []} with all 
     *         the operands (each invoker will have
     *         a distinct array). The operands are
     *         in reverse stack depth order, i.e., 
     *         the topmost operand stack element is 
     *         the last element of the array.
     */
    public Value[] operands() {
        return this.operands.clone();
    }

    /**
     * Returns an operand.
     * 
     * @param i an {@code int}; it must be an index, between 0
     *        and the number of operands of the bytecode minus 1.
     *        The operands are in reverse stack depth order, i.e., 
     *        as for the return value of the {@link #operands()}
     *        method.
     * @return a {@link Value}, the {@code i}-th operand, same as
     *          {@link #operands()}{@code [i]}.
     */
    public Value operand(int i) {
        return this.operands[i];
    }    
}
