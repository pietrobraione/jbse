package jbse.algo.meta;

import java.util.function.Supplier;

/**
 * Meta-level implementation of {@link sun.reflect.NativeConstructorAccessorImpl#newInstance0(Constructor, Object[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 extends Algo_SUN_NATIVEXACCESSORIMPL_X0 {
	public Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0() {
		super(false);
	}
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
}
