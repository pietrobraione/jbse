package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_LINKEDHASHMAP_CONTAINSVALUE;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEOUTVALUE;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#refineOnValueAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONVALUEANDBRANCH {
	public Algo_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH() {
		super("JAVA_LINKEDMAP", JAVA_LINKEDHASHMAP_CONTAINSVALUE, JBSE_JAVA_LINKEDMAP_REFINEIN, JBSE_JAVA_LINKEDMAP_REFINEOUTVALUE);
	}
}
