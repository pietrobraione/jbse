package jbse.algo;

import java.util.function.Supplier;

/**
 * Abstract algorithm for completing the semantics of the 
 * invoke* bytecodes (invoke[interface/special/static/virtual]).
 * Just extends the constructor so it receives the {@link BytecodeData}
 * from its predecessor.
 *  
 * @author Pietro Braione
 */
abstract class Algo_INVOKEX_Completion<D extends BytecodeData> extends Algo_INVOKEX_Abstract<D> {
    private final Supplier<D> bytecodeData;
    
    public Algo_INVOKEX_Completion(boolean isInterface, boolean isSpecial, boolean isStatic, Supplier<D> bytecodeData) {
        super(isInterface, isSpecial, isStatic);
        this.bytecodeData = bytecodeData;
    }

    protected final Supplier<D> bytecodeData() {
        return this.bytecodeData;
    }
}
