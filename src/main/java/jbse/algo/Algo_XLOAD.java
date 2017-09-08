package jbse.algo;

import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_WIDE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_OFFSET;

import java.util.function.Supplier;

/**
 * Algorithm managing all the *load (load from local variable) bytecodes 
 * ([a/d/f/i/l]load). It decides over the value loaded 
 * to the operand stack in the case (bytecode aload) this 
 * value is a symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_XLOAD extends Algo_XLOAD_GETX<BytecodeData_1LV> {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_1LV> bytecodeData() {
        return () -> BytecodeData_1LV.get();
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
        return () -> (this.data.nextWide() ? XLOADSTORE_IMMEDIATE_WIDE_OFFSET : XLOADSTORE_IMMEDIATE_OFFSET);
    }
}