package jbse.rules;

import static jbse.bc.Signatures.JAVA_ABSTRACTMAP;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_CONCURRENTHASHMAP;
import static jbse.bc.Signatures.JAVA_CONCURRENTMAP;
import static jbse.bc.Signatures.JAVA_HASHMAP;
import static jbse.bc.Signatures.JAVA_LINKEDHASHMAP;
import static jbse.bc.Signatures.JAVA_MAP;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_INITSYMBOLIC;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_INITSYMBOLIC;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_ONKEYRESOLUTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jbse.bc.Signature;
import jbse.common.Type;
import jbse.val.ReferenceSymbolic;

/**
 * A repository of rules for triggering method execution upon 
 * reference resolution.
 *  
 * @author Pietro Braione
 */
public final class TriggerRulesRepo implements Cloneable {
	private HashMap<String, Set<TriggerRuleExpandsTo>> rulesExpandsTo = new HashMap<>();
	private HashMap<String, Set<TriggerRuleAliases>> rulesAliases = new HashMap<>();
	private HashMap<String, Set<TriggerRuleNull>> rulesNull = new HashMap<>();
	
	public TriggerRulesRepo() {
		//nothing to do
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
     *                     {@code classAllowed}. If {@code classAllowed == null}, 
     *                     any class will be accepted.
     * @param triggerMethod the {@link Signature} of the instrumentation method to be 
     *                      triggered when this rule fires.
     * @param triggerParameter a specification of the parameter to be passed to the 
     *                        {@code triggerMethod} invocations.
     */
	public void addExpandTo(String toExpand, String originExp,
	String classAllowed, Signature triggerMethod, String triggerParameter) {
		Set<TriggerRuleExpandsTo> c = this.rulesExpandsTo.get(toExpand);
		if (c == null) {
			c = new HashSet<>();
			this.rulesExpandsTo.put(toExpand, c);
		}
		c.add(new TriggerRuleExpandsTo(originExp, classAllowed, triggerMethod, triggerParameter));
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
     * @param pathAllowedExp an expression describing the objects which are 
     *                       acceptable as alias for {@code toResolve}. During the 
     *                       symbolic execution, every symbolic reference with 
     *                       class {@code toResolve} and origin matching 
     *                       {@code originExp}, will be resolved 
     *                       when necessary to all the type- and epoch-compatible 
     *                       symbolic objects whose paths match
     *                       {@code pathAllowedExp} (use root to indicate
     *                       the root object, {REF} to indicate a path 
     *                       starting from the origin of the reference to expand, 
     *                       and {UP} to move back in the path; for instance, if 
     *                       the reference to expand has origin 
     *                       root/list/head/next/next, then {REF}/{UP}/{UP}/{UP} denotes 
     *                       the path root/list). If {@code pathAllowedExp == null}
     *                       the matching {@link ReferenceSymbolic} will not be
     *                       resolved by alias.
     * @param triggerMethod the {@link Signature} of the instrumentation method to be 
     *                      triggered when this rule fires.
     * @param triggerParameter a specification of the parameter to be passed to the 
     *                        {@code triggerMethod} invocations.
     */
	public void addResolveAliasOrigin(String toResolve, String originExp,
	String pathAllowedExp, Signature triggerMethod, String triggerParameter) {
		Set<TriggerRuleAliases> c = this.rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesAliases.put(toResolve, c);
		}
		c.add(new TriggerRuleAliasesTarget(originExp, pathAllowedExp, triggerMethod, triggerParameter));
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
     *                       with class
     *                       {@code classAllowed}. If {@code classAllowed == null}
     *                       any class will be accepted.
     * @param triggerMethod the {@link Signature} of the instrumentation method to be 
     *                      triggered when this rule fires.
     * @param triggerParameter a specification of the parameter to be passed to the 
     *                        {@code triggerMethod} invocations.
     */
	public void addResolveAliasInstanceof(String toResolve, String originExp,
	String classAllowed, Signature triggerMethod, String triggerParameter) {
		Set<TriggerRuleAliases> c = this.rulesAliases.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesAliases.put(toResolve, c);
		}
		c.add(new TriggerRuleAliasesInstanceof(originExp, classAllowed, triggerMethod, triggerParameter));
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
     * @param triggerMethod the {@link Signature} of the instrumentation method to be 
     *                      triggered when this rule fires.
     * @param triggerParameter a specification of the parameter to be passed to the 
     *                        {@code triggerMethod} invocations.
     */ 
	public void addResolveNull(String toResolve, String originExp, Signature triggerMethod, 
	String triggerParameter) {
		Set<TriggerRuleNull> c = this.rulesNull.get(toResolve);
		if (c == null) {
			c = new HashSet<>();
			this.rulesNull.put(toResolve, c);
		}
		c.add(new TriggerRuleNull(originExp, triggerMethod, triggerParameter));
	}
	
	/**
	 * Adds the rules necessary to make the map models work.
	 */
	public void addMapModelsRules() {
		//java.util.HashMap (model)
		//1- expansion of a reference to a (not initial) HashMap triggers initSymbolic
		final String NOT_JBSE_JAVA_HASHMAP_INITIALMAP = "(?!{°}*" + JAVA_HASHMAP + ":initialMap{EOL}){°}*";
		addExpandTo(JAVA_HASHMAP, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_ABSTRACTMAP, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_MAP, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_CLONEABLE, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_SERIALIZABLE, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_OBJECT, NOT_JBSE_JAVA_HASHMAP_INITIALMAP, JAVA_HASHMAP, JBSE_JAVA_MAP_INITSYMBOLIC, "{$REF}");        	
		//2- access to a HashMap key triggers onKeyResolution
		final String JBSE_JAVA_HASHMAP_KEY = "(?!{°}*" + JAVA_HASHMAP + ":initialMap::GET){°}*" + JAVA_HASHMAP + ":initialMap::KEY{°}*";
		addExpandTo(JAVA_OBJECT, JBSE_JAVA_HASHMAP_KEY, null, JBSE_JAVA_MAP_ONKEYRESOLUTION, "{$REF}");
		addResolveAliasInstanceof(JAVA_OBJECT, JBSE_JAVA_HASHMAP_KEY, null, JBSE_JAVA_MAP_ONKEYRESOLUTION, "{$REF}");
		addResolveNull(JAVA_OBJECT, JBSE_JAVA_HASHMAP_KEY, JBSE_JAVA_MAP_ONKEYRESOLUTION, "{$REF}");

		//java.util.concurrent.ConcurrentHashMap (model)
		//1- expansion of a reference to a (not initial) ConcurrentHashMap triggers initSymbolic
		final String NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP = "(?!{°}*" + JAVA_CONCURRENTHASHMAP + ":initialMap{EOL}){°}*";
		addExpandTo(JAVA_CONCURRENTHASHMAP, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_ABSTRACTMAP, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_CONCURRENTMAP, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_MAP, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_SERIALIZABLE, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_OBJECT, NOT_JBSE_JAVA_CONCURRENTHASHMAP_INITIALMAP, JAVA_CONCURRENTHASHMAP, JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC, "{$REF}");
		//2- access to a ConcurrentHashMap key triggers onKeyResolution
		final String JBSE_JAVA_CONCURRENTHASHMAP_KEY = "(?!{°}*" + JAVA_CONCURRENTHASHMAP + ":initialMap::GET){°}*" + JAVA_CONCURRENTHASHMAP + ":initialMap::KEY{°}*";
		addExpandTo(JAVA_OBJECT, JBSE_JAVA_CONCURRENTHASHMAP_KEY, null, JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION, "{$REF}");
		addResolveAliasInstanceof(JAVA_OBJECT, JBSE_JAVA_CONCURRENTHASHMAP_KEY, null, JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION, "{$REF}");
		addResolveNull(JAVA_OBJECT, JBSE_JAVA_CONCURRENTHASHMAP_KEY, JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION, "{$REF}");

		//java.util.LinkedHashMap (model)
		//1- expansion of a reference to a (not initial) LinkedHashMap triggers initSymbolic
		final String NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP = "(?!{°}*" + JAVA_LINKEDHASHMAP + ":initialMap{EOL}){°}*";
		addExpandTo(JAVA_LINKEDHASHMAP, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_HASHMAP, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_ABSTRACTMAP, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");
		addExpandTo(JAVA_MAP, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_CLONEABLE, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_SERIALIZABLE, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");        	
		addExpandTo(JAVA_OBJECT, NOT_JBSE_JAVA_LINKEDHASHMAP_INITIALMAP, JAVA_LINKEDHASHMAP, JBSE_JAVA_LINKEDMAP_INITSYMBOLIC, "{$REF}");        	
		//2- access to a LinkedHashMap key triggers onKeyResolution
		final String JBSE_JAVA_LINKEDHASHMAP_KEY = "(?!{°}*" + JAVA_LINKEDHASHMAP + ":initialMap::GET){°}*" + JAVA_LINKEDHASHMAP + ":initialMap::KEY{°}*";
		addExpandTo(JAVA_OBJECT, JBSE_JAVA_LINKEDHASHMAP_KEY, null, JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION, "{$REF}");
		addResolveAliasInstanceof(JAVA_OBJECT, JBSE_JAVA_LINKEDHASHMAP_KEY, null, JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION, "{$REF}");
		addResolveNull(JAVA_OBJECT, JBSE_JAVA_LINKEDHASHMAP_KEY, JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION, "{$REF}");
	}

	/**
	 * Returns all the expansion resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link TriggerRuleExpandsTo}{@code >} 
	 *         containing all the expansion rules matching {@code ref} (empty 
	 *         in the case no matching rules for {@code ref} exist).
	 */
	public ArrayList<TriggerRuleExpandsTo> matchingTriggerRulesExpandsTo(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<TriggerRuleExpandsTo> retVal = new ArrayList<>();
		final Set<TriggerRuleExpandsTo> rulesSet = this.rulesExpandsTo.get(refClass);
		if (rulesSet != null) {
			for (TriggerRuleExpandsTo rule : rulesSet) {
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
	 * @return an {@link ArrayList}{@code <}{@link TriggerRuleAliases}{@code >} 
	 *         containing all the "max" aliasing rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<TriggerRuleAliases> matchingTriggerRulesAliasesNonMax(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<TriggerRuleAliases> retVal = new ArrayList<>();
		final Set<TriggerRuleAliases> rulesSet = this.rulesAliases.get(refClass);
		if (rulesSet != null) {
			for (TriggerRuleAliases rule : rulesSet) {
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
	 * @return an {@link ArrayList}{@code <}{@link TriggerRuleAliases}{@code >} 
	 *         containing all the "max" aliasing rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<TriggerRuleAliases> matchingTriggerRulesAliasesMax(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<TriggerRuleAliases> retVal = new ArrayList<>();
		final Set<TriggerRuleAliases> rulesSet = this.rulesAliases.get(refClass);
		if (rulesSet != null) {
			for (TriggerRuleAliases rule : rulesSet) {
				if (rule.matches(ref) && rule.requiresMax()) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}

	/**
	 * Returns all the null resolution rules matching a reference to be resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return an {@link ArrayList}{@code <}{@link TriggerRuleNull}{@code >} 
	 *         containing all the expansion rules matching {@code ref} (empty 
	 *         in the case no rule matches {@code ref}).
	 */
	public ArrayList<TriggerRuleNull> matchingTriggerRulesNull(ReferenceSymbolic ref) {
		final String type = ref.getStaticType();
		final String refClass = Type.className(type);
		final ArrayList<TriggerRuleNull> retVal = new ArrayList<>();
		final Set<TriggerRuleNull> rulesSet = this.rulesNull.get(refClass);
		if (rulesSet != null) {
			for (TriggerRuleNull rule : rulesSet) {
				if (rule.matches(ref)) {
					retVal.add(rule);
				}
			}
		}
		return retVal;
	}
	
	@Override
	public TriggerRulesRepo clone() {
        final TriggerRulesRepo o;
        try {
            o = (TriggerRulesRepo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); //will not happen
        }
        
        //deep copy
        o.rulesAliases = new HashMap<>();
        for (Map.Entry<String, Set<TriggerRuleAliases>> e : this.rulesAliases.entrySet()) {
            o.rulesAliases.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        o.rulesExpandsTo = new HashMap<>();
        for (Map.Entry<String, Set<TriggerRuleExpandsTo>> e : this.rulesExpandsTo.entrySet()) {
            o.rulesExpandsTo.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        o.rulesNull = new HashMap<>();
        for (Map.Entry<String, Set<TriggerRuleNull>> e : this.rulesNull.entrySet()) {
            o.rulesNull.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        
        return o;
	}
}
