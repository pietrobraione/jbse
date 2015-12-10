package jbse.dec;

import jbse.val.MemoryPath;
import jbse.val.ReferenceSymbolic;

/**
 * Abstract Factory for decision alternatives. 
 * 
 * @author Pietro Braione
 *
 * @param <DA> The class for the decision alternative for 
 *            resolutions by aliasing.
 * @param <DE> The class for the decision alternative for
 *            resolutions by expansions.
 * @param <DN> The class for the decision alternative for
 *            expansions to null.
 */
interface DecisionAlternativeReferenceFactory<DA, DE, DN> {
	DA createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, MemoryPath objectOrigin, int branchNumber);
	DE createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber);
	DN createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber);
}