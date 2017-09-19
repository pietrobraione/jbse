package jbse.rules;

import jbse.bc.Signature;

/**
 * The class for a null expansion trigger.
 * 
 * @author Pietro Braione
 */
public class TriggerRuleNull extends TriggerRule {
	public TriggerRuleNull(String originExp, Signature triggerMethod, String triggerParameter) {
		super(originExp, triggerMethod, triggerParameter);
	}

	@Override
	public String toString() {
		return this.originExp + " NULL TRIGGERS " + this.getTriggerSignature() + "(" + this.getTriggerMethodParameter() + ")";
	}
}