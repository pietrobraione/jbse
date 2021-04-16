package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_MAP_CONTAINSVALUE;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_REFINEOUTVALUE;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_CONCURRENTMAP#refineOnValueAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONVALUEANDBRANCH {
	public Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH() {
		super("JAVA_CONCURRENTMAP", JAVA_MAP_CONTAINSVALUE, JBSE_JAVA_CONCURRENTMAP_REFINEIN, JBSE_JAVA_CONCURRENTMAP_REFINEOUTVALUE);
	}
}
