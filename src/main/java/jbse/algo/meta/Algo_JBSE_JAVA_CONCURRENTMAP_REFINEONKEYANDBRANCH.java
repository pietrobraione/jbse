package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_REFINEOUTKEY;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_CONCURRENTMAP#refineOnKeyAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONKEYANDBRANCH {
	public Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH() {
		super("JAVA_CONCURRENTMAP", JBSE_JAVA_CONCURRENTMAP_REFINEIN, JBSE_JAVA_CONCURRENTMAP_REFINEOUTKEY);
	}
}
