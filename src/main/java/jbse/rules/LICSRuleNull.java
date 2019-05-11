package jbse.rules;

import jbse.val.ReferenceSymbolic;

/**
 * The class for a null expansion constraint, filtering pattern for
 * possible origins of a {@link ReferenceSymbolic} that may be 
 * expanded to {@code null}.
 * 
 * @author Pietro Braione
 */
public final class LICSRuleNull extends LICSRule {
	/** The toString version of this rule. */
	private final String toString;

	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression over origin
	 *        {@link String}s: If an origin {@link String} 
	 *        matches it, then this rule fires. A {@code null} 
	 *        value is equivalent to "match all".
	 */
	public LICSRuleNull(String originExp) {
		super(originExp);
		this.toString = originExp + " null";
	}

	@Override
	public String toString() {
		return this.toString;
	}
}