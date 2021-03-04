package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEOUTKEY;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#refineOnKeyAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONKEYANDBRANCH {
	public Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH() {
		super("JAVA_LINKEDMAP", JBSE_JAVA_LINKEDMAP_REFINEIN, JBSE_JAVA_LINKEDMAP_REFINEOUTKEY);
	}
}
