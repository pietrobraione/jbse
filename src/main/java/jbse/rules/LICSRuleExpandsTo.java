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
public final class LICSRuleExpandsTo extends LICSRule {
	private final String className;

	/** The toString version of this rule. */
	private final String toString;

	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression over origin
	 *        {@link String}s: If an origin {@link String} 
	 *        matches it, then this rule fires. A {@code null} 
	 *        value is equivalent to "match all".
	 * @param className the name of a class; {@code null}
	 *        means "expands to nothing".
	 */
	public LICSRuleExpandsTo(String originExp, String className) {
		super(originExp);
		this.className = className;
		this.toString = originExp + " expands to " + (this.className == null ? "nothing" : ("instanceof " + this.className));
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
		return this.className == null;
	}
	
	@Override
	public String toString() {
		return this.toString;
	}
}