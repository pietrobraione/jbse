package jbse.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jbse.common.Type;
import jbse.val.ReferenceSymbolic;

public final class LICSRulesRepo implements Cloneable {
	private HashMap<String, Set<LICSRuleExpandsTo>> rulesExpandsTo = new HashMap<>();
	private HashMap<String, Set<LICSRuleAliases>> rulesAliases = new HashMap<>();
    private HashMap<String, Set<LICSRuleAliases>> rulesNeverAliases = new HashMap<>();
	private HashMap<String, Set<LICSRuleNotNull>> rulesNotNull = new HashMap<>();

    /**
     * Specifies a possible expansion for symbolic references. Typically, a 
     * symbolic reference is expanded to a fresh symbolic object with class
     * of its static type, or is not expanded if the static type of the reference
     * is an abstract class or an interface.
     * This method allows to override this default.
     * 
     * @param toExpand     the static type of the reference to be expanded. It must 
     *                     be {@code toExpand != null}.
     * @param originExp    an expression describing the origin of the 
     *                     symbolic references which match this replacement.
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toExpand} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for {@code toExpand}. During the 
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toExpand} and origin matching 
     *                     {@code originExp}, will be expanded 
     *                     when necessary to a symbolic object with class 
     *                     {@code classAllowed}. If {@code originExp == null}, 
     *                     the matching {@link ReferenceSymbolic}s will never 
     *                     be expanded ("expands to nothing" rule), independently
     *                     on the existence of other matching expansion rules.
     */
	public void addExpandTo(String toExpand, String originExp, String classAllowed) {
		Set<LICSRuleExpandsTo> c = this.rulesExpandsTo.get(toExpand);
		if (c == null) {
			c = new HashSet<>();
			this.rulesExpandsTo.put(toExpand, c);
		}
		c.add(new LICSRuleExpandsTo(originExp, classAllowed));
		//TODO detect overlap of expand-to-nothing with expand-to-something rules and throw exception  
	}

    /**
     * Specifies a possible way to resolve a symbolic reference by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathAllowedExp a {@link String} describing the objects which are 
     *                       acceptable as alias for {@code toResolve}. It must 
     *                       be {@code pathAllowedExp != null}. During the 
     *                       symbolic execution, every symbolic reference with 
     *                       class {@code toResolve} and origin matching 
     *                       {@code originExp}, will be resolved 
     *                       when necessary to all the type- and epoch-compatible 
     *                       symbolic objects whose paths match
     *                       {@code pathAllowedExp}. Use {ROOT}:x to indicate the
     *                       parameter with name {@code x} of the root method invocation, 
     *                       {REF} to indicate the origin  
     *                       of the reference to resolve, and {UP} to move to the upper
     *                       level in some path (if, e.g., the reference to expand has origin 
     *                       {ROOT}/list/header/next/next, then {REF}/{UP}/{UP}/{UP} denotes 
     *                       the path {ROOT}/list). Start the expression with {MAX} to indicate
     *                       a max-rule.
     */
	public void addResolveAliasOrigin(String toResolve, String originExp, String pathAllowedExp) {
		Set<LICSRuleAliases> c = this.rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesAliases.put(toResolve, c);
		}
		c.add(new LICSRuleAliasesOrigin(originExp, pathAllowedExp));
	}

    /**
     * Specifies a possible way to resolve a symbolic reference by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param classAllowed   the name of the class whose instances are possible 
     *                       aliases for {@code toResolve}. During the 
     *                       symbolic execution, every symbolic reference with 
     *                       static type {@code toResolve} and origin matching 
     *                       {@code originExp}, will be resolved 
     *                       when necessary to all the epoch-compatible symbolic objects 
     *                       with class {@code classAllowed}. If {@code classAllowed == null}
     *                       the matching {@link ReferenceSymbolic} will never be
     *                       resolved by alias ("aliases nothing" rule), independently
     *                       on the existence of other matching alias rules.
     */
	public void addResolveAliasInstanceof(String toResolve, String originExp, String classAllowed) {
		Set<LICSRuleAliases> c = this.rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesAliases.put(toResolve, c);
		}
		c.add(new LICSRuleAliasesInstanceof(originExp, classAllowed));
	}

    /**
     * Specifies a possible way to resolve a symbolic reference by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathDisallowedExp a {@link String} describing the objects which are not
     *                          acceptable as alias for {@code toResolve}. It must 
     *                          be {@code pathDisallowedExp != null}. During the 
     *                          symbolic execution, every symbolic reference with 
     *                          class {@code toResolve} and origin matching 
     *                          {@code originExp}, will not be resolved 
     *                          a type- and epoch-compatible symbolic objects if its path 
     *                          matches {@code pathDisallowedExp}. Use {ROOT} to indicate
     *                          the root object, {REF} to indicate the origin  
     *                          of the reference to resolve, and {UP} to move to the upper
     *                          level in some path (if, e.g., the reference to expand has origin 
     *                          {ROOT}/list/header/next/next, then {REF}/{UP}/{UP}/{UP} denotes 
     *                          the path {ROOT}/list).
     */
    public void addResolveAliasNever(String toResolve, String originExp, String pathDisallowedExp) {
        Set<LICSRuleAliases> c = this.rulesNeverAliases.get(toResolve);
        if (c == null) {
            c = new HashSet<>();
            this.rulesNeverAliases.put(toResolve, c);
        }
        c.add(new LICSRuleAliasesOrigin(originExp, pathDisallowedExp));
    }

    /**
     * Specifies which symbolic references shall not be resolved to null. By 
     * default all symbolic references are resolved by null. This method
     * allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match.
     */ 
	public void addResolveNotNull(String toResolve, String originExp) {
		Set<LICSRuleNotNull> c = this.rulesNotNull.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesNotNull.put(toResolve, c);
		}
		c.add(new LICSRuleNotNull(originExp));
	}

	/**
	 * Returns all the expansion resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link LICSRuleExpandsTo}{@code >} 
	 *         containing all the expansion rules matching {@code ref} (empty 
	 *         in the case no matching rules for {@code ref} exist).
	 */
	public ArrayList<LICSRuleExpandsTo> matchingLICSRulesExpandsTo(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<LICSRuleExpandsTo> retVal = new ArrayList<LICSRuleExpandsTo>();
		final Set<LICSRuleExpandsTo> rulesSet = this.rulesExpandsTo.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleExpandsTo rule : rulesSet) {
				if (rule.matches(ref)) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}

	/**
	 * Returns all the aliasing non-"max" resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link LICSRuleAliases}{@code >} 
	 *         containing all the non-"max" aliasing rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<LICSRuleAliases> matchingLICSRulesAliasesNonMax(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<LICSRuleAliases> retVal = new ArrayList<LICSRuleAliases>();
		final Set<LICSRuleAliases> rulesSet = this.rulesAliases.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleAliases rule : rulesSet) {
				if (rule.matches(ref) && !rule.requiresMax()) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Returns all the aliasing "max" resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link LICSRuleAliases}{@code >} 
	 *         containing all the "max" aliasing rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<LICSRuleAliases> matchingLICSRulesAliasesMax(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<LICSRuleAliases> retVal = new ArrayList<LICSRuleAliases>();
		final Set<LICSRuleAliases> rulesSet = this.rulesAliases.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleAliases rule : rulesSet) {
				if (rule.matches(ref) && rule.requiresMax()) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}

    /**
     * Returns all the negative aliasing resolution rules matching a reference 
     * to be resolved.
     * 
     * @param ref a {@link ReferenceSymbolic}.
     * @return an {@link ArrayList}{@code <}{@link LICSRuleAliases}{@code >} 
     *         containing all the "never aliases" rules matching {@code ref} (empty 
     *         in the case no rule matches {@code ref}).
     */
    public ArrayList<LICSRuleAliases> matchingLICSRulesNeverAliases(ReferenceSymbolic ref) {
        final String type = ref.getStaticType();
        final String refClass = Type.className(type);
        final ArrayList<LICSRuleAliases> retVal = new ArrayList<LICSRuleAliases>();
        final Set<LICSRuleAliases> rulesSet = this.rulesNeverAliases.get(refClass);
        if (rulesSet != null) {
            for (LICSRuleAliases rule : rulesSet) {
                if (rule.matches(ref)) {
                    retVal.add(rule);
                }
            }
        }
        return retVal;
    }

	/**
	 * Checks if some not-null resolution rule matches a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff some non-null resolution rule matches {@code ref}.
	 */
	public boolean someMatchingLICSRulesNotNull(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final Set<LICSRuleNotNull> rulesSet = this.rulesNotNull.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleNotNull rule : rulesSet) {
				if (rule.matches(ref)) {
					return true;
				}
			}
		}
		return false;
	}

    @Override
	public LICSRulesRepo clone() {
        final LICSRulesRepo o;
        try {
            o = (LICSRulesRepo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); //will not happen
        }
        
        //deep copy
        o.rulesAliases = new HashMap<>();
        for (Map.Entry<String, Set<LICSRuleAliases>> e : this.rulesAliases.entrySet()) {
            o.rulesAliases.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        o.rulesExpandsTo = new HashMap<>();
        for (Map.Entry<String, Set<LICSRuleExpandsTo>> e : this.rulesExpandsTo.entrySet()) {
            o.rulesExpandsTo.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        o.rulesNotNull = new HashMap<>();
        for (Map.Entry<String, Set<LICSRuleNotNull>> e : this.rulesNotNull.entrySet()) {
            o.rulesNotNull.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        
        return o;
	}
}
