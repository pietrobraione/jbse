package jbse.rules;

import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * Superclass for the alias resolution LICS rules.
 * 
 * @author Pietro Braione
 */
public abstract class LICSRuleAliases extends LICSRule { 
	LICSRuleAliases(String originExp) {
		super(originExp);
	}
	
	/**
	 * Checks whether this {@link LICSRuleAliases} is satisfied.
	 * @param ref a {@link ReferenceSymbolic}.
	 * @param o an {@link Objekt}.
	 * 
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