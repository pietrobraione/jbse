package jbse.algo;

import java.util.function.Supplier;

import jbse.bc.Signature;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Class providing static method for creating {@link Action}s to continue with.
 * 
 * @author Pietro Braione
 */
public final class Continuations {
    public static Action patchCode(byte bytecode, Algorithm<?, ?, ?, ?, ?> algorithm) {
        final Action retVal =
            new Action() {
                @Override
                public void exec(State state, ExecutionContext ctx) throws ThreadStackEmptyException {
                    state.getCurrentFrame().patchCode(bytecode);
                    ctx.dispatcher.setCase(bytecode, () -> algorithm);
                }
            };
        return retVal;
    }
    
    /**
     * Builds an {@link Algorithm} for an invoke[interface/special/static/virtual] bytecode
     * 
     * @param methodSignature the {@link Signature} of the method to be invoked.
     * @param isInterface {@code true} iff the returned algorithm must perform an invokeinterface.
     * @param isSpecial {@code true} iff the returned algorithm must perform an invokespecial.
     * @param isStatic {@code true} iff the returned algorithm must perform an invokestatic.
     * @param pcOffset an {@code int}, the program counter offset that will be 
     *        applied to the state.
     * @return an {@link Algo_INVOKEX_Abstract}{@code <?>}. Note that at most one of {@code isInterface}, 
     *         {@code isSpecial} or {@code isStatic} can be set at {@code true}. If all are set to {@code false},
     *         then the returned algorithm will perform an invokevirtual.
     */
    private static Algo_INVOKEX_Abstract<?> invoke(Signature methodSignature, boolean isInterface, boolean isSpecial, boolean isStatic, int pcOffset) {
        final Algo_INVOKEX_Start<?> retVal =
            new Algo_INVOKEX_Start<BytecodeData>(isInterface, isSpecial, isStatic) {
                @Override
                protected Supplier<BytecodeData> bytecodeData() {
                    return () -> new BytecodeData() {
                        @Override
                        protected void readImmediates(State state) {
                            setMethodSignature(methodSignature);
                        }
                    };
                }
                
                @Override
                protected int returnPcOffset() {
                    return pcOffset;
                }
            };
        return retVal;
    }
    
    /**
     * Builds an {@link Algorithm} for an invokestatic bytecode that reads
     * its data from this method's parameters rather than from the state it is
     * applied.
     * 
     * @param methodSignature the {@link Signature} of the method to be invoked.
     * @param pcOffset an {@code int}, the program counter offset that will be 
     *        applied to the state.
     * @return an {@link Algo_INVOKEX_Abstract}{@code <?>}.
     */
    public static Algo_INVOKEX_Abstract<?> invokestatic(Signature methodSignature, int pcOffset) {
        return invoke(methodSignature, false, false, true, pcOffset);
    }

    /**
     * Builds an {@link Algorithm} for an invokevirtual bytecode that reads
     * its data from this method's parameters rather than from the state it is
     * applied.
     * 
     * @param methodSignature the {@link Signature} of the method to be invoked.
     * @param pcOffset an {@code int}, the program counter offset that will be 
     *        applied to the state.
     * @return an {@link Algo_INVOKEX_Abstract}{@code <?>}.
     */
    public static Algo_INVOKEX_Abstract<?> invokevirtual(Signature methodSignature, int pcOffset) {
        return invoke(methodSignature, false, false, false, pcOffset);
    }

    //do not instantiate!
    private Continuations() {
        throw new AssertionError();
    }
}
