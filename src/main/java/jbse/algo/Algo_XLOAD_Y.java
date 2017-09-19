package jbse.algo;

import static jbse.bc.Offsets.XLOADSTORE_IMPLICIT_OFFSET;

import java.util.function.Supplier;

/**
 * Algorithm managing all the *load_* (load from local variable) bytecodes 
 * ([a/d/f/i/l]load_[0/1/2/3]). It decides over the value loaded 
 * to the operand stack in the case (bytecodes aload_*) this 
 * value is a symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_XLOAD_Y extends Algo_XLOAD_GETX<BytecodeData_0LV> {

    private final int index; //set by constructor

    /**
     * Constructor.
     * 
     * @param index the index of the local variable.
     */
    public Algo_XLOAD_Y(int index) {
        this.index = index;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_0LV> bytecodeData() {
        return () -> BytecodeData_0LV.withVarSlot(this.index).get(); //TODO possibly replace with: return BytecodeData_0LV.withVarSlot(this.index); possibly do the same in all the other Algorithm_* classes
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            this.valToLoad = this.data.localVariableValue();
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XLOADSTORE_IMPLICIT_OFFSET;
    }
}