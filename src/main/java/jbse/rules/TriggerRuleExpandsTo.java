package jbse.rules;

import jbse.bc.Signature;
import jbse.val.ReferenceSymbolic;

/**
 * The class for an expansion trigger rule.
 * 
 * @author Pietro Braione
 * 
 */
public final class TriggerRuleExpandsTo extends TriggerRule {
	/** When {@code null} means that the expansion class doesn't care. */
	private final String className;

	/** The toString version of this rule. */
	private final String toString;

	public TriggerRuleExpandsTo(String originExp, String className, Signature triggerMethodSignature, String triggerMethodParameter) {
		super(originExp, triggerMethodSignature, triggerMethodParameter);
		this.className = className;
		this.toString = originExp + " expands" + (this.className == null ? "" : (" to instanceof " + this.className)) + " triggers " + 
		                triggerMethodSignature.toString() + (triggerMethodParameter == null ? "" : (":" + triggerMethodParameter));
	}
	
	public boolean satisfies(String className) {
		return (this.className == null ? true : this.className.equals(className));
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
		return this.toString;
	}
}