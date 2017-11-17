package jbse.algo;

import static jbse.algo.BytecodeData_1KME.Kind.kind;

import java.util.function.Supplier;

/**
 * Algorithm for the invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEX extends Algo_INVOKEX_Start<BytecodeData_1KME> {
    public Algo_INVOKEX(boolean isInterface, boolean isSpecial, boolean isStatic) {
        super(isInterface, isSpecial, isStatic);
    }

    @Override
    protected Supplier<BytecodeData_1KME> bytecodeData() {
        return () -> BytecodeData_1KME.withMethod(kind(this.isInterface, this.isSpecial, this.isStatic)).get();
    }
}
