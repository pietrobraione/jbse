package jbse.algo.meta;


import java.util.function.Supplier;

/**
 * Meta-level implementation of {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0 extends Algo_SUN_NATIVEXACCESSORIMPL_X0 {
	public Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0() {
		super(true);
	}
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }
}
