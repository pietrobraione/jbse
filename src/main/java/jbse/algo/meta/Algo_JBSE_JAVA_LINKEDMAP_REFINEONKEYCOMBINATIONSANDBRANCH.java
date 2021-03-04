package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEOUTKEY;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#refineOnKeyCombinationsAndBranch(Object...)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONKEYCOMBINATIONSANDBRANCH {
	public Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH() {
		super("JAVA_LINKEDMAP", JBSE_JAVA_LINKEDMAP_REFINEOUTKEY, JBSE_JAVA_LINKEDMAP_REFINEIN);
	}
}
