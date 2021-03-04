package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEOUTKEY;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnKeyAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONKEYANDBRANCH {
	public Algo_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH() {
		super("JAVA_MAP", JBSE_JAVA_MAP_REFINEIN, JBSE_JAVA_MAP_REFINEOUTKEY);
	}
}
