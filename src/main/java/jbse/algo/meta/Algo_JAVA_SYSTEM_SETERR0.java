package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_SYSTEM_ERR;

/**
 * Meta-level implementation of {@link java.lang.System#setErr0(java.io.PrintStream)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_SYSTEM_SETERR0 extends Algo_JAVA_SYSTEM_SETX0 {
	public Algo_JAVA_SYSTEM_SETERR0() {
		super("setErr0", JAVA_SYSTEM_ERR);
	}
}
