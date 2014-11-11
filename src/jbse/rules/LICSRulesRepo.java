package jbse.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jbse.Type;
import jbse.mem.ReferenceSymbolic;

public class LICSRulesRepo {
	private HashMap<String, Set<LICSRuleExpandsTo>> rulesExpandsTo = new HashMap<>();
	private HashMap<String, Set<LICSRuleAliases>> rulesAliases = new HashMap<>();
	private HashMap<String, Set<LICSRuleNull>> rulesNull = new HashMap<>();
	private HashMap<String, Set<LICSRuleNotNull>> rulesNotNull = new HashMap<>();
	private HashSet<String> notInitializedClasses = new HashSet<>();

	public void addNotInitializedClass(String s) {
		this.notInitializedClasses.add(s);
	}
	
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
		Set<LICSRuleExpandsTo> c = rulesExpandsTo.get(toExpand);
		if (c == null) {
			c = new HashSet<>();
			rulesExpandsTo.put(toExpand, c);
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
     *                       {@code pathAllowedExp}. Use {ROOT} to indicate
     *                       the root object, {REF} to indicate the origin  
     *                       of the reference to resolve, and {UP} to move to the upper
     *                       level in some path (if, e.g., the reference to expand has origin 
     *                       {ROOT}/list/header/next/next, then {REF}/{UP}/{UP}/{UP} denotes 
     *                       the path {ROOT}/list).
     */
	public void addResolveAliasOrigin(String toResolve, String originExp, String pathAllowedExp) {
		Set<LICSRuleAliases> c = rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			rulesAliases.put(toResolve, c);
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
		Set<LICSRuleAliases> c = rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			rulesAliases.put(toResolve, c);
		}
		c.add(new LICSRuleAliasesInstanceof(originExp, classAllowed));
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
	public void addResolveNull(String toResolve, String originExp) {
		Set<LICSRuleNull> c = rulesNull.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			rulesNull.put(toResolve, c);
		}
		c.add(new LICSRuleNull(originExp));
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
		Set<LICSRuleNotNull> c = rulesNotNull.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			rulesNotNull.put(toResolve, c);
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
		final Set<LICSRuleExpandsTo> rulesSet = rulesExpandsTo.get(refClass);
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
	 * Returns all the aliasing "max" resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link LICSRuleAliases}{@code >} 
	 *         containing all the "max" aliasing rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<LICSRuleAliases> matchingLICSRulesAliasesNonMax(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<LICSRuleAliases> retVal = new ArrayList<LICSRuleAliases>();
		final Set<LICSRuleAliases> rulesSet = rulesAliases.get(refClass);
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
		final Set<LICSRuleAliases> rulesSet = rulesAliases.get(refClass);
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
	 * Checks if some not-null resolution rule matches a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff some non-null resolution rule matches {@code ref}.
	 */
	public boolean someMatchingLICSRulesNotNull(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final Set<LICSRuleNotNull> rulesSet = rulesNotNull.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleNotNull rule : rulesSet) {
				if (rule.matches(ref)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns all the null resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link LICSRuleNull}{@code >} 
	 *         containing all the expansion rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	//TODO use it for triggers
	ArrayList<LICSRuleNull> matchingRulesResolutionNull(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<LICSRuleNull> retVal = new ArrayList<LICSRuleNull>();
		final Set<LICSRuleNull> rulesSet = rulesNull.get(refClass);
		if (rulesSet != null) {
			for (LICSRuleNull rule : rulesSet) {
				if (rule.matches(ref)) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}

	//TODO do it better!!!
	public boolean notInitializedClassesContains(String c) {
		return this.notInitializedClasses.contains(c);
	}
}
