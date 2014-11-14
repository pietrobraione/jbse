package jbse.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Maps {@code DecisionAlternative} subclasses to the {@code Comparator}s 
 * that impose a canonical order on how the different 
 * alternatives must be explored during symbolic execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeComparators {
    /**
     * Default comparator for {@link DecisionAlternativeIf}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeIf}{@code >}
     *         yielding {@link DecisionAlternativeIfTrue} {@code <} {@link DecisionAlternativeIfFalse}.
     */
	private static Comparator<DecisionAlternativeIf> defaultComparatorDecisionAlternativeIf() {
		return new Comparator<DecisionAlternativeIf>() {
			@Override
			public int compare(DecisionAlternativeIf o1, DecisionAlternativeIf o2) {
				final boolean o1True = (o1 instanceof DecisionAlternativeIfTrue);
				final boolean o2True = (o2 instanceof DecisionAlternativeIfTrue);
				final int o1Pos = o1True ? 0 : 1;
				final int o2Pos = o2True ? 0 : 1;
				return o1Pos - o2Pos;
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternativeComparison}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeComparison}{@code >} yielding
     *         {@link DecisionAlternativeComparisonGt} {@code <} {@link DecisionAlternativeComparisonEq} 
     *         {@code <} {@link DecisionAlternativeComparisonLt}.
     */
	private static Comparator<DecisionAlternativeComparison> defaultComparatorDecisionAlternativeComparison() {
		return new Comparator<DecisionAlternativeComparison>() {
			@Override
			public int compare(DecisionAlternativeComparison o1, DecisionAlternativeComparison o2) {
				final boolean o1Gt = (o1 instanceof DecisionAlternativeComparisonGt);
				final boolean o1Eq = (o1 instanceof DecisionAlternativeComparisonEq);
				final boolean o2Gt = (o2 instanceof DecisionAlternativeComparisonGt);
				final boolean o2Eq = (o2 instanceof DecisionAlternativeComparisonEq);
				final int o1Pos = o1Gt ? 0 : o1Eq ? 1 : 2;
				final int o2Pos = o2Gt ? 0 : o2Eq ? 1 : 2;
				return o1Pos - o2Pos;
			}
		};
	}

    /**
     * Default comparator for <code>DecisionAlternativeSwitch</code>s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeSwitch}<code>&gt;</code> where
     *         {@code default_alt < nondefault_alt} and {@code nondefault1 < nondefault2}
     *         iff {@code nondefault1.value > nondefault2.value}.
     */
	private static Comparator<DecisionAlternativeSwitch> defaultComparatorDecisionAlternativeSwitch() {
	    return new Comparator<DecisionAlternativeSwitch>() {
	        public int compare(DecisionAlternativeSwitch o1, DecisionAlternativeSwitch o2) {
	        	boolean o1def = (o1.isDefault());
	        	boolean o2def = (o2.isDefault());

	        	//o1 before o2
	        	if ((o1def && !o2def) || 
	        		(!o1def && !o2def && o1.value() > o2.value())) {
	        		return -1;
	        	}
	        	
	        	//o1 after o2
	        	if ((!o1def && o2def) || 
	            		(!o1def && !o2def && o1.value() < o2.value())) {
	            	return 1;
	        	}
	        	
	        	//o1 equals o2
	       		return 0;
	        }
	    };
	}

    /**
     * Default comparator for {@link DecisionAlternativeLFLoad}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeLFLoad}{@code >} where
     *         {@code null_alt < object_alt < class_alt}, object alternatives are 
     *         ordered decreasingly by heap position, and class alternatives are 
     *         ordered on inverse lexicographic order on the class name.
     */
	private static Comparator<DecisionAlternativeLFLoad> defaultComparatorDecisionAlternativeLFLoad() { 	
		return new Comparator<DecisionAlternativeLFLoad>() {
			public int compare(DecisionAlternativeLFLoad o1, DecisionAlternativeLFLoad o2) {
				if (o1 instanceof DecisionAlternativeLFLoadRefNull) {
					if (o2 instanceof DecisionAlternativeLFLoadRefNull) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternativeLFLoadRefAliases) {
					if (o2 instanceof DecisionAlternativeLFLoadRefNull) {
						return 1;
					} else if (o2 instanceof DecisionAlternativeLFLoadRefAliases) {
						final DecisionAlternativeLFLoadRefAliases daro1 = (DecisionAlternativeLFLoadRefAliases) o1;
						final DecisionAlternativeLFLoadRefAliases daro2 = (DecisionAlternativeLFLoadRefAliases) o2;
						final long d = daro2.getAliasPosition() - daro1.getAliasPosition();
						return (d < 0 ? -1 : (d == 0 ? 0 : 1));
					} else {
						return -1;
					}
				} else {
					if (o2 instanceof DecisionAlternativeLFLoadRefExpands) {
						DecisionAlternativeLFLoadRefExpands darc1 = (DecisionAlternativeLFLoadRefExpands) o1;
						DecisionAlternativeLFLoadRefExpands darc2 = (DecisionAlternativeLFLoadRefExpands) o2;
						return darc2.getClassNameOfTargetObject().compareTo(darc1.getClassNameOfTargetObject());
					} else {
						return 1;
					}
				}
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternativeNewarray}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeNewarray}{@code >} yielding
     *         {@code outbound_alt < inbound_alt}.
     */
	private static Comparator<DecisionAlternativeNewarray> defaultComparatorDecisionAlternativeNewarray() {
		return new Comparator<DecisionAlternativeNewarray>() {
			public int compare(DecisionAlternativeNewarray o1, DecisionAlternativeNewarray o2) {
				if (o1 instanceof DecisionAlternativeNewarrayWrong) {
					if (o2 instanceof DecisionAlternativeNewarrayOK) {
						return -1;
					} else {
						return 0;
					}
				} else {
					if (o2 instanceof DecisionAlternativeNewarrayOK) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		};
	}
	
    /**
     * Default comparator for {@link DecisionAlternativeAstore}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeAstore}{@code >} 
     *         yielding {@link DecisionAlternativeAstoreIn} {@code <} {@link DecisionAlternativeAstoreOut}.
     */
	private static Comparator<DecisionAlternativeAstore> defaultComparatorDecisionAlternativeAstore() {
		return new Comparator<DecisionAlternativeAstore>() {
			@Override
			public int compare(DecisionAlternativeAstore o1, DecisionAlternativeAstore o2) {
				final boolean o1In = (o1 instanceof DecisionAlternativeAstoreIn);
				final boolean o2In = (o2 instanceof DecisionAlternativeAstoreIn);
				final int o1Pos = o1In ? 0 : 1;
				final int o2Pos = o2In ? 0 : 1;
				return o1Pos - o2Pos;
			}
		};
	}
	

    /**
     * Default comparator for {@link DecisionAlternativeAload}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternativeAload}{@code >} 
     *         yielding {@code value_alt < null_alt < instance_alt < class_alt < outbound_alt}.
     */
	private static Comparator<DecisionAlternativeAload> defaultComparatorDecisionAlternativeAload() {
		return new Comparator<DecisionAlternativeAload>() {
			public int compare(DecisionAlternativeAload o1, DecisionAlternativeAload o2) {
				if (o1 instanceof DecisionAlternativeAloadResolved) {
					if (!(o2 instanceof DecisionAlternativeAloadResolved)) {
						return -1;
					} else {
						DecisionAlternativeAloadResolved daav1 = (DecisionAlternativeAloadResolved) o1;
						DecisionAlternativeAloadResolved daav2 = (DecisionAlternativeAloadResolved) o2;
						return daav2.getValueToLoad().toString().compareTo(daav1.getValueToLoad().toString());
					}
				} else if (o1 instanceof DecisionAlternativeAloadRefNull) {
					if (o2 instanceof DecisionAlternativeAloadResolved) {
						return 1;
					} else if (o2 instanceof DecisionAlternativeAloadRefNull) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternativeAloadRefAliases) {
					if (o2 instanceof DecisionAlternativeAloadResolved ||
							o2 instanceof DecisionAlternativeAloadRefNull) {
						return 1;
					} else if (o2 instanceof DecisionAlternativeAloadRefAliases) {
						final DecisionAlternativeAloadRefAliases daari1 = (DecisionAlternativeAloadRefAliases) o1;
						final DecisionAlternativeAloadRefAliases daari2 = (DecisionAlternativeAloadRefAliases) o2;
						final long d = daari2.getAliasPosition() - daari1.getAliasPosition();
						return (d < 0 ? -1 : (d == 0 ? 0 : 1));
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternativeAloadRefExpands) {
					if (o2 instanceof DecisionAlternativeAloadOut) {
						return -1;
					} else if (o2 instanceof DecisionAlternativeAloadRefExpands) {
						final DecisionAlternativeAloadRefExpands daarc1 = (DecisionAlternativeAloadRefExpands) o1;
						final DecisionAlternativeAloadRefExpands daarc2 = (DecisionAlternativeAloadRefExpands) o2;
						return -(daarc1.getClassNameOfTargetObject().compareTo(daarc2.getClassNameOfTargetObject()));
					} else {
						return 1;
					}
				} else {
					if (o2 instanceof DecisionAlternativeAloadOut) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		};
	}
	
	private final HashMap<Class<?>, Comparator<?>> comparators = new HashMap<>(); 
	
	/**
	 * Default constructor.
	 */
    public DecisionAlternativeComparators() {
    	this.comparators.put(DecisionAlternativeIf.class,         defaultComparatorDecisionAlternativeIf());
    	this.comparators.put(DecisionAlternativeComparison.class, defaultComparatorDecisionAlternativeComparison());
    	this.comparators.put(DecisionAlternativeSwitch.class,     defaultComparatorDecisionAlternativeSwitch());
    	this.comparators.put(DecisionAlternativeLFLoad.class,     defaultComparatorDecisionAlternativeLFLoad());
    	this.comparators.put(DecisionAlternativeNewarray.class,   defaultComparatorDecisionAlternativeNewarray());
    	this.comparators.put(DecisionAlternativeAstore.class,     defaultComparatorDecisionAlternativeAstore());
    	this.comparators.put(DecisionAlternativeAload.class,      defaultComparatorDecisionAlternativeAload());
    }

    /**
     * Constructor allowing to override one or more of the comparators returned 
     * by the <code>DecisionAlternativeComparators</code> getters. 
     * 
     * @param cb 
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeIf}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeBoolean()} 
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeIf}<code>&gt;</code>
     *        must be returned.
     * @param cc
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeComparison}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeComparison()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeComparison}<code>&gt;</code>
     *        must be returned.
     * @param ci
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeSwitch}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeSwitch()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeSwitch}{@code >}
     *        must be returned.
     * @param cr
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeReference}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeReference()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeReference}{@code >}
     *        must be returned.
     * @param cn
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeAnew}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeAnew()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeAnew}{@code >}
     *        must be returned.
     * @param cs
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeAstore}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeAstore()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternativeAstore}{@code >}
     *        must be returned.
     * @param cl
     *        a {@link Comparator}{@code <}{@link DecisionAlternativeAload}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeAload()}
     *        or {@code null} iff the default <{@link Comparator}{@code <}{@link DecisionAlternativeAload}{@code >}
     *        must be returned.
     */
    public DecisionAlternativeComparators(
    		final Comparator<DecisionAlternativeIf> cb, 
			final Comparator<DecisionAlternativeComparison> cc, 
			final Comparator<DecisionAlternativeSwitch> ci,
    		final Comparator<DecisionAlternativeLFLoad> cr,
    		final Comparator<DecisionAlternativeNewarray> cn,
    		final Comparator<DecisionAlternativeAstore> cs,
    		final Comparator<DecisionAlternativeAload> cl) {
    	this(); //sets defaults
		if (cb != null) { this.comparators.put(DecisionAlternativeIf.class,         Collections.reverseOrder(cb)); } 
		if (cc != null) { this.comparators.put(DecisionAlternativeComparison.class, Collections.reverseOrder(cc)); }
		if (ci != null) { this.comparators.put(DecisionAlternativeSwitch.class,     Collections.reverseOrder(ci)); }
		if (cr != null) { this.comparators.put(DecisionAlternativeLFLoad.class,     Collections.reverseOrder(cr)); }
		if (cn != null) { this.comparators.put(DecisionAlternativeNewarray.class,   Collections.reverseOrder(cn)); }
		if (cs != null) { this.comparators.put(DecisionAlternativeAstore.class,     Collections.reverseOrder(cs)); }
		if (cl != null) { this.comparators.put(DecisionAlternativeAload.class,      Collections.reverseOrder(cl)); }
	}
    
    /**
     * Gets the comparator over a given class of decision alternatives.
     * 
     * @param superclassDecisionAlternatives The superclass, a {@link Class}{@code <R>}
     * @return a {@link Comparator}{@code <R>}.
     */
    @SuppressWarnings("unchecked")
	public <R> Comparator<R> get(Class<R> superclassDecisionAlternatives) {
    	return (Comparator<R>) this.comparators.get(superclassDecisionAlternatives);
    }
}
