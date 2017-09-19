package jbse.tree;

import static java.util.Collections.reverseOrder;

import java.util.Comparator;
import java.util.HashMap;

import jbse.common.exc.UnexpectedInternalException;

/**
 * Maps classes implementing {@code DecisionAlternative}
 * to the {@code Comparator}s that impose a canonical 
 * order on how the different 
 * alternatives must be explored during symbolic execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeComparators {
    /**
     * Default comparator for {@link DecisionAlternative_NONE}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_NONE}{@code >}. Being 
     *         {@link DecisionAlternative_NONE} singleton the comparator always returns 0.
     */
    private static Comparator<DecisionAlternative_NONE> defaultComparatorDecisionAlternative_NONE() {
        return new Comparator<DecisionAlternative_NONE>() {
            @Override
            public int compare(DecisionAlternative_NONE o1, DecisionAlternative_NONE o2) {
                return 0;
            }
        };
    }

    /**
     * Default comparator for {@link DecisionAlternative_IFX}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_IFX}{@code >} yielding
     *         {@link DecisionAlternative_IFX_True} {@code <} {@link DecisionAlternative_IFX_False}.
     */
	private static Comparator<DecisionAlternative_IFX> defaultComparatorDecisionAlternative_IFX() {
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
	private static Comparator<DecisionAlternative_XCMPY> defaultComparatorDecisionAlternative_XCMPY() {
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
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >}.
     */
	private static Comparator<DecisionAlternative_XSWITCH> defaultComparatorDecisionAlternative_XSWITCH() {
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
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >} yielding 
     *         {@link DecisionAlternative_XLOAD_GETX_Resolved} 
     *         {@code <} {@link DecisionAlternative_XLOAD_GETX_Null} 
     *         {@code <} {@link DecisionAlternative_XLOAD_GETX_Aliases}
     *         {@code <} {@link DecisionAlternative_XLOAD_GETX_Expands}.
     */
	private static Comparator<DecisionAlternative_XLOAD_GETX> defaultComparatorDecisionAlternative_XLOAD_GETX() { 	
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
                } else if (o1 instanceof DecisionAlternative_XLOAD_GETX_Null) {
                    if (o2 instanceof DecisionAlternative_XLOAD_GETX_Resolved) {
                        return 1;
                    } else if (o2 instanceof DecisionAlternative_XLOAD_GETX_Null) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XLOAD_GETX_Aliases) {
					if (o2 instanceof DecisionAlternative_XLOAD_GETX_Resolved || 
					    o2 instanceof DecisionAlternative_XLOAD_GETX_Null) {
						return 1;
					} else if (o2 instanceof DecisionAlternative_XLOAD_GETX_Aliases) {
						final DecisionAlternative_XLOAD_GETX_Aliases daro1 = (DecisionAlternative_XLOAD_GETX_Aliases) o1;
						final DecisionAlternative_XLOAD_GETX_Aliases daro2 = (DecisionAlternative_XLOAD_GETX_Aliases) o2;
						final long d = daro2.getAliasPosition() - daro1.getAliasPosition();
						return (d < 0 ? -1 : (d == 0 ? 0 : 1));
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XLOAD_GETX_Expands) {
					if (o2 instanceof DecisionAlternative_XLOAD_GETX_Expands) {
					    final DecisionAlternative_XLOAD_GETX_Expands darc1 = (DecisionAlternative_XLOAD_GETX_Expands) o1;
					    final DecisionAlternative_XLOAD_GETX_Expands darc2 = (DecisionAlternative_XLOAD_GETX_Expands) o2;
						return darc2.getClassNameOfTargetObject().compareTo(darc1.getClassNameOfTargetObject());
					} else {
						return 1;
					}
				} else {
				    throw new UnexpectedInternalException("Unexpected subclass of DecisionAlternative_XLOAD_GETX: " + o1.getClass());
				}
			}
		};
	}

    /**
     * Default comparator for {@link DecisionAlternative_XNEWARRAY}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XNEWARRAY}{@code >} yielding
     *         {@link DecisionAlternative_XNEWARRAY_Wrong} {@code <} {@link DecisionAlternative_XNEWARRAY_Ok}.
     */
	private static Comparator<DecisionAlternative_XNEWARRAY> defaultComparatorDecisionAlternative_XNEWARRAY() {
		return new Comparator<DecisionAlternative_XNEWARRAY>() {
			public int compare(DecisionAlternative_XNEWARRAY o1, DecisionAlternative_XNEWARRAY o2) {
                final boolean o1Wrong = (o1 instanceof DecisionAlternative_XNEWARRAY_Wrong);
                final boolean o2Wrong = (o2 instanceof DecisionAlternative_XNEWARRAY_Wrong);
                final int o1Pos = o1Wrong ? 0 : 1;
                final int o2Pos = o2Wrong ? 0 : 1;
                return o1Pos - o2Pos;
			}
		};
	}
	
    /**
     * Default comparator for {@link DecisionAlternative_XASTORE}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >} yielding
     *         {@link DecisionAlternative_XASTORE_Out} {@code <} {@link DecisionAlternative_XASTORE_In}.
     */
	private static Comparator<DecisionAlternative_XASTORE> defaultComparatorDecisionAlternative_XASTORE() {
		return new Comparator<DecisionAlternative_XASTORE>() {
			@Override
			public int compare(DecisionAlternative_XASTORE o1, DecisionAlternative_XASTORE o2) {
				final boolean o1Out = (o1 instanceof DecisionAlternative_XASTORE_Out);
				final boolean o2Out = (o2 instanceof DecisionAlternative_XASTORE_Out);
				final int o1Pos = o1Out ? 0 : 1;
				final int o2Pos = o2Out ? 0 : 1;
				return o1Pos - o2Pos;
			}
		};
	}
	

    /**
     * Default comparator for {@link DecisionAlternative_XALOAD}s.
     * 
     * @return A {@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >} yielding
     *         {@link DecisionAlternative_XALOAD_Out}
     *         {@code <} {@link DecisionAlternative_XALOAD_Resolved} 
     *         {@code <} {@link DecisionAlternative_XALOAD_Null}
     *         {@code <} {@link DecisionAlternative_XALOAD_Aliases} 
     *         {@code <} {@link DecisionAlternative_XALOAD_Expands}.
     */
	private static Comparator<DecisionAlternative_XALOAD> defaultComparatorDecisionAlternative_XALOAD() {
		return new Comparator<DecisionAlternative_XALOAD>() {
			public int compare(DecisionAlternative_XALOAD o1, DecisionAlternative_XALOAD o2) {
			    if (o1 instanceof DecisionAlternative_XALOAD_Out) {
                    if (o2 instanceof DecisionAlternative_XALOAD_Out) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (o1 instanceof DecisionAlternative_XALOAD_Resolved) {
                    if (o2 instanceof DecisionAlternative_XALOAD_Out) {
                        return 1;
                    } else if (o2 instanceof DecisionAlternative_XALOAD_Resolved) {
                        final DecisionAlternative_XALOAD_Resolved daav1 = (DecisionAlternative_XALOAD_Resolved) o1;
                        final DecisionAlternative_XALOAD_Resolved daav2 = (DecisionAlternative_XALOAD_Resolved) o2;
                        return daav2.toString().compareTo(daav1.toString());
					} else {
                        return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_Null) {
					if (o2 instanceof DecisionAlternative_XALOAD_Out ||
					    o2 instanceof DecisionAlternative_XALOAD_Resolved) {
						return 1;
					} else if (o2 instanceof DecisionAlternative_XALOAD_Null) {
						return 0;
					} else {
						return -1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_Aliases) {
					if (o1 instanceof DecisionAlternative_XALOAD_Expands) {
						return -1;
					} else if (o2 instanceof DecisionAlternative_XALOAD_Aliases) {
						final DecisionAlternative_XALOAD_Aliases daari1 = (DecisionAlternative_XALOAD_Aliases) o1;
						final DecisionAlternative_XALOAD_Aliases daari2 = (DecisionAlternative_XALOAD_Aliases) o2;
                        return daari2.toString().compareTo(daari1.toString());
					} else {
						return 1;
					}
				} else if (o1 instanceof DecisionAlternative_XALOAD_Expands) {
				    if (o2 instanceof DecisionAlternative_XALOAD_Expands) {
						final DecisionAlternative_XALOAD_Expands daarc1 = (DecisionAlternative_XALOAD_Expands) o1;
						final DecisionAlternative_XALOAD_Expands daarc2 = (DecisionAlternative_XALOAD_Expands) o2;
						return daarc2.toString().compareTo(daarc1.toString());
					} else {
						return 1;
					}
				} else {
                    throw new UnexpectedInternalException("Unexpected subclass of DecisionAlternative_XALOAD: " + o1.getClass());
				}
			}
		};
	}
	
	private final HashMap<Class<?>, Comparator<?>> comparators = new HashMap<>(); 
	
	/**
	 * Default constructor.
	 */
    public DecisionAlternativeComparators() {
        this.comparators.put(DecisionAlternative_NONE.class,       defaultComparatorDecisionAlternative_NONE());
    	this.comparators.put(DecisionAlternative_IFX.class,        defaultComparatorDecisionAlternative_IFX());
    	this.comparators.put(DecisionAlternative_XCMPY.class,      defaultComparatorDecisionAlternative_XCMPY());
    	this.comparators.put(DecisionAlternative_XSWITCH.class,    defaultComparatorDecisionAlternative_XSWITCH());
    	this.comparators.put(DecisionAlternative_XLOAD_GETX.class, defaultComparatorDecisionAlternative_XLOAD_GETX());
    	this.comparators.put(DecisionAlternative_XNEWARRAY.class,  defaultComparatorDecisionAlternative_XNEWARRAY());
    	this.comparators.put(DecisionAlternative_XASTORE.class,    defaultComparatorDecisionAlternative_XASTORE());
    	this.comparators.put(DecisionAlternative_XALOAD.class,     defaultComparatorDecisionAlternative_XALOAD());
    }

    /**
     * Constructor allowing to override one or more of the comparators returned 
     * by the <code>DecisionAlternativeComparators</code> getters. 
     * 
     * @param cb 
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_IFX}{@code >} 
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_IFX}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_IFX}{@code >}
     *        must be returned.
     * @param cc
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XCMPY}{@code >}
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XCMPY}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XCMPY}{@code >}
     *        must be returned.
     * @param ci
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >}
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XSWITCH}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XSWITCH}{@code >}
     *        must be returned.
     * @param cr
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >}
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XLOAD_GETX}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >}
     *        must be returned.
     * @param cn
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XNEWARRAY}{@code >}
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XNEWARRAY}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XNEWARRAY}{@code >}
     *        must be returned.
     * @param cs
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >}
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XASTORE}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XASTORE}{@code >}
     *        must be returned.
     * @param cl
     *        a {@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >} to 
     *        to be returned by calls to 
     *        {@link #get(Class) get}{@code (}{@link DecisionAlternative_XALOAD}{@code .class)} 
     *        or {@code null} iff the default 
     *        {@link Comparator}{@code <}{@link DecisionAlternative_XALOAD}{@code >}
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
		if (cb != null) { this.comparators.put(DecisionAlternative_IFX.class,        reverseOrder(cb)); } 
		if (cc != null) { this.comparators.put(DecisionAlternative_XCMPY.class,      reverseOrder(cc)); }
		if (ci != null) { this.comparators.put(DecisionAlternative_XSWITCH.class,    reverseOrder(ci)); }
		if (cr != null) { this.comparators.put(DecisionAlternative_XLOAD_GETX.class, reverseOrder(cr)); }
		if (cn != null) { this.comparators.put(DecisionAlternative_XNEWARRAY.class,  reverseOrder(cn)); }
		if (cs != null) { this.comparators.put(DecisionAlternative_XASTORE.class,    reverseOrder(cs)); }
		if (cl != null) { this.comparators.put(DecisionAlternative_XALOAD.class,     reverseOrder(cl)); }
	}

    /**
     * Gets the comparator over a given class of decision alternatives.
     * 
     * @param superclassDecisionAlternatives The superclass, a 
     *        {@link Class}{@code <R extends DecisionAlternative>}.
     * @return a {@link Comparator}{@code <R>}.
     */
    @SuppressWarnings("unchecked")
	public <R extends DecisionAlternative> Comparator<R> get(Class<R> superclassDecisionAlternatives) {
    	return (Comparator<R>) this.comparators.get(superclassDecisionAlternatives);
    }
}
