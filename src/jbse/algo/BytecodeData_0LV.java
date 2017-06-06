package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

/**
 * One implicit (local variable),
 * zero immediates.
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_0LV extends BytecodeData {
    private final int numVarSlot;

    @Override
    protected void readImmediates(State state) throws InterruptException {
        readLocalVariable(state, this.numVarSlot);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_0LV(int numVarSlot) {
        this.numVarSlot = numVarSlot;
    }    

    /**
     * Factory (with fluent interface).
     * 
     * @param numVarSlot an {@code int}, the number of a variable slot.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_0LV}{@code >},
     *         the actual factory for {@link BytecodeData_0LV} objects.
     */
    public static Supplier<BytecodeData_0LV> withVarSlot(int numVarSlot) {
        return () -> new BytecodeData_0LV(numVarSlot);
    }
}
