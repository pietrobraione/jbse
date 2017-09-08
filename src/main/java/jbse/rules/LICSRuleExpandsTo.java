package jbse.rules;

import jbse.val.ReferenceSymbolic;

/**
 * The class for an expansion constraint rule, mapping a pattern for
 * possible origins to a concrete class to which a {@link ReferenceSymbolic}
 * with matching origin can be expanded. It also encompasses
 * the special case "expands to nothing".
 * 
 * @author Pietro Braione
 * 
 */
public class LICSRuleExpandsTo extends LICSRule {
	private final String className;

	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression; {@code null} 
	 *        is equivalent to "match all".
	 * @param className the name of a class; {@code null}
	 *        means "expands to nothing".
	 */
	public LICSRuleExpandsTo(String originExp, String className) {
		super(originExp);
		this.className = (className == null ? Util.NOTHING : className);
	}
	
	public boolean satisfies(String className) {
		if (isNothingRule()) {
			return false;
		}
		return this.className.equals(className);
	}

	/**
	 * Checks whether this is an "expands to nothing" rule.
	 * 
	 * @return {@code true} iff the rule is an 
	 *         "expands to nothing" rule.
	 */
	private boolean isNothingRule() {
		return this.className.equals(Util.NOTHING);
	}
	
	@Override
	public String toString() {
		return this.originExp + " EXPANDS TO " + this.className;
	}
}