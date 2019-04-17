package jbse.rules;

import jbse.val.ReferenceSymbolic;

/**
 * The class for a non-null expansion constraint, filtering pattern for
 * possible origins of a {@link ReferenceSymbolic} that may not be 
 * expanded to {@code null}.
 * 
 * @author Pietro Braione
 */
public class LICSRuleNotNull extends LICSRule {
	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression over origin
	 *        {@link String}s: If an origin {@link String} 
	 *        matches it, then this rule fires. A {@code null} 
	 *        value is equivalent to "match all".
	 */
	public LICSRuleNotNull(String originExp) {
		super(originExp);
	}
	
	@Override
	public String toString() {
		return this.originExp + " not null";
	}
}