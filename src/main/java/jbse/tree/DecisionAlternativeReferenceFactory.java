package jbse.tree;

import jbse.bc.ClassFile;
import jbse.val.ReferenceSymbolic;

/**
 * Abstract Factory for decision alternatives for symbolic 
 * reference resolution. 
 * 
 * @author Pietro Braione
 *
 * @param <DA> The class for the decision alternative for 
 *             resolutions by aliasing.
 * @param <DE> The class for the decision alternative for
 *             resolutions by expansions.
 * @param <DN> The class for the decision alternative for
 *             expansions to null.
 */
public interface DecisionAlternativeReferenceFactory<DA, DE, DN> {
    /**
     * Factory method for a {@code DA}.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable/array.
     * @param objectPosition a {@code long}, the position in the heap of the object
     *        {@code referenceToResolve} refers to.
     * @param objectOrigin a {@link ReferenceSymbolic}, the origin of the object {@code referenceToResolve} 
     *        refers to.
     * @param branchNumber an {@code int}, the branch number.
     * @return a {@code DA}.
     */
    DA createAlternativeRefAliases(ReferenceSymbolic referenceToResolve, long objectPosition, ReferenceSymbolic objectOrigin, int branchNumber);
    
    /**
     * Factory method for a {@code DE}.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable/array.
     * @param classFileOfTargetObject the {@link ClassFile} for the class of the
     *        object {@code referenceToResolve} expands to.
     * @param branchNumber an {@code int}, the branch number.
     * @return a {@code DE}.
     */
    DE createAlternativeRefExpands(ReferenceSymbolic referenceToResolve, ClassFile classFile, int branchNumber);
    
    /**
     * Factory method for a {@code DN}.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable/array.
     * @param branchNumber an {@code int}, the branch number.
     * @return a {@code DN}.
     */
    DN createAlternativeRefNull(ReferenceSymbolic referenceToResolve, int branchNumber);
}