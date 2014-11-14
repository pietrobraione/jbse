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
	TriggerRuleAliases(String originExp, Signature triggerMethod, String triggerParameter) {
		super(originExp, triggerMethod, triggerParameter);
	}
	
	/**
	 * Checks whether this {@link TriggerRuleAliases} is satisfied.
	 * @param ref a {@link ReferenceSymbolic}.
	 * @param o an {@link Objekt}.
	 * 
	 * @return {@code true} iff {@code ref}'s resolution by
	 *         aliasing to {@code o} satisfies this rule.
	 */
	abstract public boolean satisfies(ReferenceSymbolic ref, Objekt o);
	
	/**
	 * Checks whether the entry has the {MAX} keyword.
	 * 
	 * @return {@code true} iff the entry has the {MAX} keyword.
	 */
	abstract public boolean requiresMax();
}