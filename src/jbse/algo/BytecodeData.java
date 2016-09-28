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
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

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
    
    public final void read(State state, Supplier<Integer> numOperandsSupplier) 
    throws ThreadStackEmptyException, InterruptException {
        this.nextWide = state.nextWide();
        readImmediates(state);
        readOperands(state, numOperandsSupplier.get());
    }
    
    protected abstract void readImmediates(State state) throws InterruptException;

    protected final void readImmediateSignedByte(State state, int immediatePos) 
    throws InterruptException {
        try {
            this.valByte = state.getInstruction(immediatePos);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }
    
    protected final void readImmediateUnsignedByte(State state, int immediatePos) 
    throws InterruptException {
        try {
            this.valShort = asUnsignedByte(state.getInstruction(immediatePos));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }
    
    protected final void readImmediateSignedWord(State state, int immediatePos) 
    throws InterruptException {
        try {
            this.valShort = byteCatShort(state.getInstruction(immediatePos), state.getInstruction(immediatePos + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readImmediateUnsignedWord(State state, int immediatePos) 
    throws InterruptException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediatePos), state.getInstruction(immediatePos + 1));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readImmediateSignedDword(State state, int immediatePos) 
    throws InterruptException {
        try {
            this.valInt = 
                byteCat(state.getInstruction(immediatePos), state.getInstruction(immediatePos + 1), 
                        state.getInstruction(immediatePos + 2), state.getInstruction(immediatePos + 3));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
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
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readJump(State state, int jumpOffset) {
        try {
            this.jumpOffset = jumpOffset;
            this.jumpTarget = state.getPC() + jumpOffset;
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readClassName(State state, int fieldSigSlot)
    throws InterruptException {
        try {
            this.className = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getClassSignature(fieldSigSlot);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readFieldSignature(State state, int fieldSigSlot)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getFieldSignature(fieldSigSlot);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readInterfaceMethodSignature(State state, int fieldSigSlot)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getInterfaceMethodSignature(fieldSigSlot);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    
    protected final void readNoninterfaceMethodSignature(State state, int fieldSigSlot)
    throws InterruptException {
        try {
            this.signature = state.getClassHierarchy().getClassFile(state.getCurrentMethodSignature().getClassName()).getMethodSignature(fieldSigSlot);
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }

    protected final void setPrimitiveType(State state, char primitiveType)
    throws InterruptException {
        if (isPrimitive(primitiveType)) {
            this.primitiveType = primitiveType;
        } else {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    protected final void setSwitchTable(SwitchTable switchTable) {
        this.switchTable = switchTable;
    }
    
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
     * Returns all the operands
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
     * @return the {@code i}-th operands
     */
    public Value operand(int i) {
        return this.operands[i];
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
     * Returns the slot of the local variable.
     * 
     * @return the slot of the variable.
     */
    public int localVariableSlot() {
        return this.varSlot;
    }
    
    /**
     * Returns the name of the local variable.
     * 
     * @return the name of the variable or {@code null}
     *         if no debug info about the variable is 
     *         available.
     */
    public String localVariableName() {
        return this.varName;
    }
    
    /**
     * Returns the value of the local variable.
     * 
     * @return the value of the variable.
     */
    public Value localVariableValue() {
        return this.varValue;
    }
    
    public int jumpOffset() {
        return this.jumpOffset;
    }
    
    public int jumpTarget() {
        return this.jumpTarget;
    }
    
    public byte immediateSignedByte() {
        return this.valByte;
    }
    
    public short immediateUnsignedByte() {
        return this.valShort;
    }
    
    public short immediateSignedWord() {
        return this.valShort;
    }
    
    public int immediateUnsignedWord() {
        return this.valInt;
    }
    
    public int immediateSignedDword() {
        return this.valInt;
    }
    
    public String className() {
        return this.className;
    }
    
    public Signature signature() {
        return this.signature;
    }
    
    public char primitiveType() {
        return this.primitiveType;
    }
    
    public SwitchTable switchTable() {
        return this.switchTable;
    }
}
