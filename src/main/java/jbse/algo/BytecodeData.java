package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Util.asUnsignedByte;
import static jbse.common.Util.byteCat;
import static jbse.common.Util.byteCatShort;

import java.util.function.Supplier;

import jbse.bc.CallSiteSpecifier;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
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
    private boolean interfaceMethodSignature;
    private Signature signature;
    private String className;
    private char primitiveType;
    private SwitchTable switchTable;
    private CallSiteSpecifier callSiteSpecifier;

    /**
     * Reads all the data for a bytecode. 
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param numOperandsSupplier a {@link Supplier}{@code <}{@link Integer}{@code >}
     *        returning the number of operands to be read from {@code state}'s 
     *        operand stack.
     * @throws ThreadStackEmptyException when {@code state}'s thread stack is empty.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is empty.
     */
    public final void read(State state, Calculator calc, Supplier<Integer> numOperandsSupplier) 
    throws ThreadStackEmptyException, InterruptException, 
    ClasspathException, FrozenStateException {
        this.nextWide = state.nextWide();
        readImmediates(state, calc);
        readOperands(state, calc, numOperandsSupplier.get());
    }

    /**
     * Reads the immediates from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     * @throws ThreadStackEmptyException if the state's stack is empty.
     */
    protected abstract void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException, ThreadStackEmptyException;

    /**
     * Reads a signed byte immediate from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readImmediateSignedByte(State state, Calculator calc, int immediateDisplacement) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.valByte = state.getInstruction(immediateDisplacement);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads an unsigned byte immediate from a {@link State}'s current method code.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readImmediateUnsignedByte(State state, Calculator calc, int immediateDisplacement) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.valShort = asUnsignedByte(state.getInstruction(immediateDisplacement));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state, calc);
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
     * @param calc a {@link Calculator}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readImmediateSignedWord(State state, Calculator calc, int immediateDisplacement) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.valShort = byteCatShort(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state, calc);
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
     * @param calc a {@link Calculator}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readImmediateUnsignedWord(State state, Calculator calc, int immediateDisplacement) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state, calc);
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
     * @param calc a {@link Calculator}.
     * @param immediateDisplacement an {@code int}, the offset of the immediate 
     *        with respect to {@code state}'s current program counter.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readImmediateSignedDword(State state, Calculator calc, int immediateDisplacement) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediateDisplacement), state.getInstruction(immediateDisplacement + 1), 
                        state.getInstruction(immediateDisplacement + 2), state.getInstruction(immediateDisplacement + 3));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a local variable's data (name and current value).  
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param varSlot an {@code int}, the number of slot of the
     *        local variable. Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    protected final void readLocalVariable(State state, Calculator calc, int varSlot) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            this.varSlot = varSlot;
            this.varName = state.getLocalVariableDeclaredName(varSlot);
            this.varValue = state.getLocalVariableValue(varSlot);
        } catch (InvalidSlotException e) {
            throwVerifyError(state, calc);
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
     */
    protected final void readJump(State state, int jumpOffset) {
        try {
            this.jumpOffset = jumpOffset;
            this.jumpTarget = state.getCurrentProgramCounter() + jumpOffset;
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a class name.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param classRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the class name is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void readClassName(State state, Calculator calc, int classRefIndex)
    throws InterruptException, ClasspathException {
        try {
            this.className = state.getCurrentClass().getClassSignature(classRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a field signature.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param fieldRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the field signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void readFieldSignature(State state, Calculator calc, int fieldRefIndex)
    throws InterruptException, ClasspathException {
        try {
            this.signature = state.getCurrentClass().getFieldSignature(fieldRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads an interface method signature.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param interfaceMethodRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the interface method signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void readInterfaceMethodSignature(State state, Calculator calc, int interfaceMethodRefIndex)
    throws InterruptException, ClasspathException {
        try {
        	this.interfaceMethodSignature = true;
            this.signature = state.getCurrentClass().getInterfaceMethodSignature(interfaceMethodRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Reads a noninterface method signature.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param noninterfaceMethodRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the noninterface method signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void readNoninterfaceMethodSignature(State state, Calculator calc, int noninterfaceMethodRefIndex)
    throws InterruptException, ClasspathException {
        try {
        	this.interfaceMethodSignature = false;
            this.signature = state.getCurrentClass().getMethodSignature(noninterfaceMethodRefIndex);
        } catch (InvalidIndexException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }
    
    /**
     * Reads a method signature that might be either interface or noninterface.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param methodRefIndex an {@code int}, the index in the constant table
     *        of the current class' classfile where the method signature is. 
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void readMethodSignature(State state, Calculator calc, int methodRefIndex)
    throws InterruptException, ClasspathException {
        try {
        	this.interfaceMethodSignature = false;
            this.signature = state.getCurrentClass().getMethodSignature(methodRefIndex);
        } catch (InvalidIndexException e1) {
            try {
            	this.interfaceMethodSignature = true;
                this.signature = state.getCurrentClass().getInterfaceMethodSignature(methodRefIndex);
            } catch (InvalidIndexException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                failExecution(e);
            }
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }
    
    protected final void readCallSiteSpecifier(State state, Calculator calc, int cssRefIndex) 
    throws InterruptException, ClasspathException {
    	try {
			this.callSiteSpecifier = state.getCurrentClass().getCallSiteSpecifier(cssRefIndex);
        } catch (InvalidIndexException | ClassFileIllFormedException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Stores a primitive type.
     * 
     * @param state a {@link State}.
     * @param calc a {@link Calculator}.
     * @param primitiveType a {@code char}, the primitive type to store.
     *        Usually it is itself an immediate.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     */
    protected final void setPrimitiveType(State state, Calculator calc, char primitiveType)
    throws InterruptException, ClasspathException {
        if (isPrimitive(primitiveType)) {
            this.primitiveType = primitiveType;
        } else {
            throwVerifyError(state, calc);
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
     * @param calc a {@link Calculator}.
     * @param numOperands an {@code int}, the number of 
     *        operands 
     * @throws ThreadStackEmptyException if {@code state}
     *         has an empty stack.
     * @throws InterruptException if the execution of the container
     *         {@link Algorithm} must be interrupted.
     * @throws ClasspathException  when some standard classfile is not found, 
     *         or ill-formed, or not accessible.
     * @throws FrozenStateException if the state is frozen.
     */
    private void readOperands(State state, Calculator calc, int numOperands) 
    throws ThreadStackEmptyException, InterruptException, ClasspathException, FrozenStateException {
        final Frame frame = state.getCurrentFrame();
        try {
            this.operands = frame.operands(numOperands);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state, calc);
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
     * Returns whether the signature read with 
     * {@link #readFieldSignature(State, int) readFieldSignature}, 
     * {@link #readInterfaceMethodSignature(State, int) readInterfaceMethodSignature}, or
     * {@link #readNoninterfaceMethodSignature(State, int) readNoninterfaceMethodSignature}
     * was an interface method signature or not.
     * 
     * @return a {@code boolean}.
     */
    public boolean interfaceMethodSignature() {
    	return this.interfaceMethodSignature;
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
     * Returns the call site specifier set with
     * {@link #readCallSiteSpecifier(State, Calculator, int) readCallSiteSpecifier}.
     * 
     * @return a {@link CallSiteSpecifier}.
     */
    public CallSiteSpecifier callSiteSpecifier() {
    	return this.callSiteSpecifier;
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
