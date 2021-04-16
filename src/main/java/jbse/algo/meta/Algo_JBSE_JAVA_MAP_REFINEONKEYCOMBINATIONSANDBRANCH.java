package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEOUTKEY;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnKeyCombinationsAndBranch(Object...)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONKEYCOMBINATIONSANDBRANCH {
	public Algo_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH() {
		super("JAVA_MAP", JBSE_JAVA_MAP_REFINEOUTKEY, JBSE_JAVA_MAP_REFINEIN);
	}
}
