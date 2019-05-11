package jbse.rules;

import jbse.bc.Signature;
import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * An alias resolution rule mapping a pattern for
 * possible origins to a class of objects to which a 
 * {@link ReferenceSymbolic} can be resolved by aliasing.
 * 
 * @author Pietro Braione
 */
public class TriggerRuleAliasesInstanceof extends TriggerRuleAliases {
	/** If {@code null} means aliases nothing. */
	private final String classAllowed;
	
	/** The toString version of this rule. */
	private final String toString;

	public TriggerRuleAliasesInstanceof(String originExp, String classAllowed, Signature triggerMethodSignature, String triggerMethodParameter) {
		super(originExp, triggerMethodSignature, triggerMethodParameter);
		this.classAllowed = classAllowed;
		this.toString = originExp + " aliases instanceof " + this.classAllowed + " triggers " + 
		                triggerMethodSignature.toString() + (triggerMethodParameter == null ? "" : (":" + triggerMethodParameter));
	}

	@Override
	public boolean satisfies(ReferenceSymbolic ref, Objekt o) {
		if (this.classAllowed == null) {
			return false;
		}

		//ref is not used
		return this.classAllowed.equals(o.getType().getClassName());
	}

	@Override
	public boolean requiresMax() {
		return false;
	}
	
	@Override
	public String toString() {
		return this.toString;
	}
}