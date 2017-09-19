package jbse.rules;

import jbse.bc.Signature;
import jbse.val.ReferenceSymbolic;

/**
 * The class for an expansion trigger rule.
 * 
 * @author Pietro Braione
 * 
 */
public class TriggerRuleExpandsTo extends TriggerRule {
	/** Should not be {@code null}. */
	private final String className;

	public TriggerRuleExpandsTo(String originExp, String className, Signature triggerMethod, String triggerParameter) {
		super(originExp, triggerMethod, triggerParameter);
		this.className = className;
	}
	
	public boolean satisfies(String className) {
		return this.className.equals(className);
	}

	/**
	 * Returns the class for this {@link TriggerRuleExpandsTo}.
	 * 
	 * @return a class name or {@code null} iff the 
	 *         matching {@link ReferenceSymbolic} shall not be expanded.
	 */
	public String getExpansionClass() {
		return this.className;
	}
	
	@Override
	public String toString() {
		return this.originExp + " EXPANDS TO " + this.className + " TRIGGERS " + 
				this.getTriggerSignature() + "(" + this.getTriggerMethodParameter() + ")";
	}
}