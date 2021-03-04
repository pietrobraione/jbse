package jbse.algo.meta;

import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEMAPCOMPLETE;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnFreshEntryAndBranch()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH extends Algo_JBSE_JAVA_XMAP_REFINEONFRESHENTRYANDBRANCH {
	public Algo_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH() {
		super("JAVA_MAP", JBSE_JAVA_MAP_REFINEIN, JBSE_JAVA_MAP_REFINEMAPCOMPLETE);
	}
}
