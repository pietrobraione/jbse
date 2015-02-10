package jbse.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import jbse.common.exc.UnexpectedInternalException;

/**
 * Maps {@code DecisionAlternative} subclasses to the {@code Comparator}s 
 * that impose a canonical order on how the different 
 * alternatives must be explored during symbolic execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeComparators {
    /**
     * Default comparator for {@link DecisionAlternative_IFX}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_IFX}{@code >}
     *         yielding {@link DecisionAlternative_IFX_True} {@code <} {@link DecisionAlternative_IFX_False}.
     */
	private static Comparator<DecisionAlternative_IFX> defaultComparatorDecisionAlternativeIf() {
		return new Comparator<DecisionAlternative_IFX>() {
			@Override
			public int compare(DecisionAlternative_IFX o1, DecisionAlternative_IFX o2) {
				final boolean o1True = (o1 instanceof DecisionAlternative_IFX_True);
				final boolean o2True = (o2 instanceof DecisionAlternative_IFX_True);
				final int o1Pos = o1True ? 0 : 1;
				final int o2Pos = o2True ? 0 : 1;
				return o1Pos - o2Pos;
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternative_XCMPY}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XCMPY}{@code >} yielding
     *         {@link DecisionAlternative_XCMPY_Gt} {@code <} {@link DecisionAlternative_XCMPY_Eq} 
     *         {@code <} {@link DecisionAlternative_XCMPY_Lt}.
     */
	private static Comparator<DecisionAlternative_XCMPY> defaultComparatorDecisionAlternativeComparison() {
		return new Comparator<DecisionAlternative_XCMPY>() {
			@Override
			public int compare(DecisionAlternative_XCMPY o1, DecisionAlternative_XCMPY o2) {
				final boolean o1Gt = (o1 instanceof DecisionAlternative_XCMPY_Gt);
				final boolean o1Eq = (o1 instanceof DecisionAlternative_XCMPY_Eq);
				final boolean o2Gt = (o2 instanceof DecisionAlternative_XCMPY_Gt);
				final boolean o2Eq = (o2 instanceof DecisionAlternative_XCMPY_Eq);
				final int o1Pos = o1Gt ? 0 : o1Eq ? 1 : 2;
				final int o2Pos = o2Gt ? 0 : o2Eq ? 1 : 2;
				return o1Pos - o2Pos;
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternative_XSWITCH}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >} where
     *         {@code default_alt < nondefault_alt} and {@code nondefault1 < nondefault2}
     *         iff {@code nondefault1.value > nondefault2.value}.
     */
	private static Comparator<DecisionAlternative_XSWITCH> defaultComparatorDecisionAlternativeSwitch() {
	    return new Comparator<DecisionAlternative_XSWITCH>() {
	        public int compare(DecisionAlternative_XSWITCH o1, DecisionAlternative_XSWITCH o2) {
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
     * Default comparator for {@link DecisionAlternative_XLOAD_GETX}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >} where
     *         {@code null_alt < object_alt < class_alt}, object alternatives are 
     *         ordered decreasingly by heap position, and class alternatives are 
     *         ordered on inverse lexicographic order on the class name.
     */
	private static Comparator<DecisionAlternative_XLOAD_GETX> defaultComparatorDecisionAlternativeLFLoad() { 	
		return new Comparator<DecisionAlternative_XLOAD_GETX>() {
			public int compare(DecisionAlternative_XLOAD_GETX o1, DecisionAlternative_XLOAD_GETX o2) {
                if (o1 instanceof DecisionAlternative_XLOAD_GETX_Resolved) {
                    if (o2 instanceof DecisionAlternative_XLOAD_GETX_Resolved) {
                        final DecisionAlternative_XLOAD_GETX_Resolved darr1 = (DecisionAlternative_XLOAD_GETX_Resolved) o1;
                        final DecisionAlternative_XLOAD_GETX_Resolved darr2 = (DecisionAlternative_XLOAD_GETX_Resolved) o2;
                        return darr2.getValueToLoad().toString().compareTo(darr1.getValueToLoad().toString());
                    } else {
                        return -1;
                    }
                } else if (o1 instanceof DecisionAlternative_XLOAD_GETX_RefNull) {
                    if (o2 instanceof DecisionAlternative_XLOAD_GETX_Resolved) {
                        return 1;
                    } else if (o2 instanceof DecisionAlternative_XLOAD_GETX_RefNull) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XLOAD_GETX_RefAliases) {
					if (o2 instanceof DecisionAlternative_XLOAD_GETX_Resolved || 
					    o2 instanceof DecisionAlternative_XLOAD_GETX_RefNull) {
						return 1;
					} else if (o2 instanceof DecisionAlternative_XLOAD_GETX_RefAliases) {
						final DecisionAlternative_XLOAD_GETX_RefAliases daro1 = (DecisionAlternative_XLOAD_GETX_RefAliases) o1;
						final DecisionAlternative_XLOAD_GETX_RefAliases daro2 = (DecisionAlternative_XLOAD_GETX_RefAliases) o2;
						final long d = daro2.getAliasPosition() - daro1.getAliasPosition();
						return (d < 0 ? -1 : (d == 0 ? 0 : 1));
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XLOAD_GETX_RefExpands) {
					if (o2 instanceof DecisionAlternative_XLOAD_GETX_RefExpands) {
					    final DecisionAlternative_XLOAD_GETX_RefExpands darc1 = (DecisionAlternative_XLOAD_GETX_RefExpands) o1;
					    final DecisionAlternative_XLOAD_GETX_RefExpands darc2 = (DecisionAlternative_XLOAD_GETX_RefExpands) o2;
						return darc2.getClassNameOfTargetObject().compareTo(darc1.getClassNameOfTargetObject());
					} else {
						return 1;
					}
				} else {
				    throw new UnexpectedInternalException("Undetected subclass of DecisionAlternativeLFLoad: " + o1.getClass());
				}
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternative_XNEWARRAY}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XNEWARRAY}{@code >} yielding
     *         {@code outbound_alt < inbound_alt}.
     */
	private static Comparator<DecisionAlternative_XNEWARRAY> defaultComparatorDecisionAlternativeNewarray() {
		return new Comparator<DecisionAlternative_XNEWARRAY>() {
			public int compare(DecisionAlternative_XNEWARRAY o1, DecisionAlternative_XNEWARRAY o2) {
				if (o1 instanceof DecisionAlternative_XNEWARRAY_Wrong) {
					if (o2 instanceof DecisionAlternative_XNEWARRAY_Ok) {
						return -1;
					} else {
						return 0;
					}
				} else {
					if (o2 instanceof DecisionAlternative_XNEWARRAY_Ok) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		};
	}
	
    /**
     * Default comparator for {@link DecisionAlternative_XASTORE}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >} 
     *         yielding {@link DecisionAlternative_XASTORE_In} {@code <} {@link DecisionAlternative_XASTORE_Out}.
     */
	private static Comparator<DecisionAlternative_XASTORE> defaultComparatorDecisionAlternativeAstore() {
		return new Comparator<DecisionAlternative_XASTORE>() {
			@Override
			public int compare(DecisionAlternative_XASTORE o1, DecisionAlternative_XASTORE o2) {
				final boolean o1In = (o1 instanceof DecisionAlternative_XASTORE_In);
				final boolean o2In = (o2 instanceof DecisionAlternative_XASTORE_In);
				final int o1Pos = o1In ? 0 : 1;
				final int o2Pos = o2In ? 0 : 1;
				return o1Pos - o2Pos;
			}
		};
	}
	

    /**
     * Default comparator for {@link DecisionAlternative_XALOAD}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >} 
     *         yielding {@code value_alt < null_alt < instance_alt < class_alt < outbound_alt}.
     */
	private static Comparator<DecisionAlternative_XALOAD> defaultComparatorDecisionAlternativeAload() {
		return new Comparator<DecisionAlternative_XALOAD>() {
			public int compare(DecisionAlternative_XALOAD o1, DecisionAlternative_XALOAD o2) {
				if (o1 instanceof DecisionAlternative_XALOAD_Resolved) {
					if (o2 instanceof DecisionAlternative_XALOAD_Resolved) {
                        final DecisionAlternative_XALOAD_Resolved daav1 = (DecisionAlternative_XALOAD_Resolved) o1;
                        final DecisionAlternative_XALOAD_Resolved daav2 = (DecisionAlternative_XALOAD_Resolved) o2;
                        return daav2.getValueToLoad().toString().compareTo(daav1.getValueToLoad().toString());
					} else {
                        return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_RefNull) {
					if (o2 instanceof DecisionAlternative_XALOAD_Resolved) {
						return 1;
					} else if (o2 instanceof DecisionAlternative_XALOAD_RefNull) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_RefAliases) {
					if (o2 instanceof DecisionAlternative_XALOAD_Resolved ||
					    o2 instanceof DecisionAlternative_XALOAD_RefNull) {
						return 1;
					} else if (o2 instanceof DecisionAlternative_XALOAD_RefAliases) {
						final DecisionAlternative_XALOAD_RefAliases daari1 = (DecisionAlternative_XALOAD_RefAliases) o1;
						final DecisionAlternative_XALOAD_RefAliases daari2 = (DecisionAlternative_XALOAD_RefAliases) o2;
						final long d = daari2.getAliasPosition() - daari1.getAliasPosition();
						return (d < 0 ? -1 : (d == 0 ? 0 : 1));
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_RefExpands) {
					if (o2 instanceof DecisionAlternative_XALOAD_Out) {
						return -1;
					} else if (o2 instanceof DecisionAlternative_XALOAD_RefExpands) {
						final DecisionAlternative_XALOAD_RefExpands daarc1 = (DecisionAlternative_XALOAD_RefExpands) o1;
						final DecisionAlternative_XALOAD_RefExpands daarc2 = (DecisionAlternative_XALOAD_RefExpands) o2;
						return daarc2.getClassNameOfTargetObject().compareTo(daarc1.getClassNameOfTargetObject());
					} else {
						return 1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_Out) {
					if (o2 instanceof DecisionAlternative_XALOAD_Out) {
						return 0;
					} else {
						return 1;
					}
                } else {
                    throw new UnexpectedInternalException("Undetected subclass of DecisionAlternativeAload: " + o1.getClass());
				}
			}
		};
	}
	
	private final HashMap<Class<?>, Comparator<?>> comparators = new HashMap<>(); 
	
	/**
	 * Default constructor.
	 */
    public DecisionAlternativeComparators() {
    	this.comparators.put(DecisionAlternative_IFX.class,         defaultComparatorDecisionAlternativeIf());
    	this.comparators.put(DecisionAlternative_XCMPY.class, defaultComparatorDecisionAlternativeComparison());
    	this.comparators.put(DecisionAlternative_XSWITCH.class,     defaultComparatorDecisionAlternativeSwitch());
    	this.comparators.put(DecisionAlternative_XLOAD_GETX.class,     defaultComparatorDecisionAlternativeLFLoad());
    	this.comparators.put(DecisionAlternative_XNEWARRAY.class,   defaultComparatorDecisionAlternativeNewarray());
    	this.comparators.put(DecisionAlternative_XASTORE.class,     defaultComparatorDecisionAlternativeAstore());
    	this.comparators.put(DecisionAlternative_XALOAD.class,      defaultComparatorDecisionAlternativeAload());
    }

    /**
     * Constructor allowing to override one or more of the comparators returned 
     * by the <code>DecisionAlternativeComparators</code> getters. 
     * 
     * @param cb 
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_IFX}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeBoolean()} 
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternative_IFX}<code>&gt;</code>
     *        must be returned.
     * @param cc
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XCMPY}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeComparison()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternative_XCMPY}<code>&gt;</code>
     *        must be returned.
     * @param ci
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeSwitch()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >}
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
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeAstore()}
     *        or {@code null} iff the default {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >}
     *        must be returned.
     * @param cl
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >} to 
     *        be returned by calls to {@link #createEmptyDecisionAlternativeAload()}
     *        or {@code null} iff the default <{@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >}
     *        must be returned.
     */
    public DecisionAlternativeComparators(
    		final Comparator<DecisionAlternative_IFX> cb, 
			final Comparator<DecisionAlternative_XCMPY> cc, 
			final Comparator<DecisionAlternative_XSWITCH> ci,
    		final Comparator<DecisionAlternative_XLOAD_GETX> cr,
    		final Comparator<DecisionAlternative_XNEWARRAY> cn,
    		final Comparator<DecisionAlternative_XASTORE> cs,
    		final Comparator<DecisionAlternative_XALOAD> cl) {
    	this(); //sets defaults
		if (cb != null) { this.comparators.put(DecisionAlternative_IFX.class,         Collections.reverseOrder(cb)); } 
		if (cc != null) { this.comparators.put(DecisionAlternative_XCMPY.class, Collections.reverseOrder(cc)); }
		if (ci != null) { this.comparators.put(DecisionAlternative_XSWITCH.class,     Collections.reverseOrder(ci)); }
		if (cr != null) { this.comparators.put(DecisionAlternative_XLOAD_GETX.class,     Collections.reverseOrder(cr)); }
		if (cn != null) { this.comparators.put(DecisionAlternative_XNEWARRAY.class,   Collections.reverseOrder(cn)); }
		if (cs != null) { this.comparators.put(DecisionAlternative_XASTORE.class,     Collections.reverseOrder(cs)); }
		if (cl != null) { this.comparators.put(DecisionAlternative_XALOAD.class,      Collections.reverseOrder(cl)); }
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
