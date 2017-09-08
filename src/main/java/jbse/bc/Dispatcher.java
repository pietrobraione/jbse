package jbse.bc;

import java.util.HashMap;

/**
 * A {@code Dispatcher}{@code <Q,R>} associates keys (objects with
 * class {@code Q}) to {@code DispatchStrategy} objects that return 
 * values with class {@code R}. It is just a thin layer of syntactic
 * sugar spread over a map.
 *  
 * @author Pietro Braione
 *
 * @param <Q> the class for keys.  
 * @param <R> the class for values.  
 */
public abstract class Dispatcher<Q, R> {
	/**
	 * A Strategy returning an object. 
	 * 
	 * @author Pietro Braione
	 *
	 * @param <S> the class of the returned object.
	 */
	@FunctionalInterface
	public interface DispatchStrategy<S> { 
		S doIt() throws Exception; 
	}

	private DispatchStrategy<? extends R> dispatchNonexistent = () -> null;

	private final HashMap<Q, DispatchStrategy<? extends R>> dispatchTable = new HashMap<>();
	
	/**
	 * Sets the {@link DispatchStrategy} for a given key. If the key
	 * was previously associated with another {@link DispatchStrategy}, the 
	 * former association is discarded and replaced with the new one.
	 * 
	 * @param instruction the bytecode.
	 * @param s the {@link DispatchStrategy} associated to {@code instruction}.
	 *          In the case {@code s == null} the method has no effect.
	 * @return {@code this} (allows chain invocations). 
	 */
	public Dispatcher<Q, R> setCase(Q key, DispatchStrategy<? extends R> s) {
		if (s != null) { 
			this.dispatchTable.put(key, s);
		}
		return this;
	}
	
	/**
	 * Sets the {@link DispatchStrategy} to be invoked by default 
	 * for unregistered keys. 
	 * If the {@link DispatchStrategy} was previously set, the 
	 * former setting is discarded and replaced with the new one.
	 * 
	 * @param s the {@link DispatchStrategy} to be used by default 
	 *          with unregistered keys.
	 *          In the case {@code s == null} the method has no effect.
	 * @return {@code this} (allows chain invocations).
	 */
	public Dispatcher<Q, R> setDefault(DispatchStrategy<? extends R> s) {
		if (s != null) {
			this.dispatchNonexistent = s;
		}
		return this;
	}
	
	/**
	 * Executes the {@link DispatchStrategy}{@code <R>} associated with a 
	 * key, or the registered default strategy in case such association 
	 * does not exist, or does nothing if neither a registered Strategy for 
	 * the key nor a default one has been previously set.
	 * 
	 * @param key the key.
	 * @return the {@code R} produced by the invoked 
	 *         {@link DispatchStrategy}{@code <R>}, or {@code null} 
	 *         if neither a registered {@link DispatchStrategy} for the key nor a 
	 *         default one has been previously set.
	 */
	public R select(Q key) throws Exception {
		final DispatchStrategy<? extends R> d;
		if (this.dispatchTable.containsKey(key)) {
			d = this.dispatchTable.get(key);
		} else {
			d = this.dispatchNonexistent;
		}
		return d.doIt();
	}
}
