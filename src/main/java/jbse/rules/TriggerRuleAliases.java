package jbse.rules;

import jbse.bc.Signature;
import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * Superclass for the alias resolution trigger rules.
 * 
 * @author Pietro Braione
 */
public abstract class TriggerRuleAliases extends TriggerRule { 
	TriggerRuleAliases(String originExp, Signature triggerMethodSignature, String triggerMethodParameter) {
		super(originExp, triggerMethodSignature, triggerMethodParameter);
	}
	
	/**
	 * Checks whether this {@link TriggerRuleAliases} is satisfied.
	 * 
	 * @param ref the {@link ReferenceSymbolic} that made fire 
	 *        this rule.
	 * @param o an {@link Objekt}.
	 * @return {@code true} iff {@code ref}'s resolution by
	 *         aliasing to {@code o} satisfies this rule.
	 */
	abstract public boolean satisfies(ReferenceSymbolic ref, Objekt o);
	
	/**
	 * Checks whether this rule is a "max" rule.
	 * 
	 * @return {@code true} iff the rule is a "max" rule.
	 */
	abstract public boolean requiresMax();
}