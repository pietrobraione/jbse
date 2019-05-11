package jbse.rules;

import jbse.bc.Signature;

/**
 * The class for a null resolution trigger rule.
 * 
 * @author Pietro Braione
 */
public class TriggerRuleNull extends TriggerRule {
	/** The toString version of this rule. */
	private final String toString;

	public TriggerRuleNull(String originExp, Signature triggerMethodSignature, String triggerMethodParameter) {
		super(originExp, triggerMethodSignature, triggerMethodParameter);
		this.toString = originExp + " null triggers " +
		                triggerMethodSignature.toString() + (triggerMethodParameter == null ? "" : (":" + triggerMethodParameter));
	}

	@Override
	public String toString() {
		return this.toString;
	}
}