package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEMAPCOMPLETE;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#refineOnFreshEntryAndBranch()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONFRESHENTRYANDBRANCH {
	public Algo_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH() {
		super("JAVA_LINKEDMAP", JBSE_JAVA_LINKEDMAP_REFINEIN, JBSE_JAVA_LINKEDMAP_REFINEMAPCOMPLETE);
	}
}
