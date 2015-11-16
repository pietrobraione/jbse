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
	/** Should not be {@code null}. */
	private final String classAllowed;

	public TriggerRuleAliasesInstanceof(String originExp, String classAllowed, Signature triggerMethod, String triggerParameter) {
		super(originExp, triggerMethod, triggerParameter);
		this.classAllowed = classAllowed;
	}

	@Override
	public boolean satisfies(ReferenceSymbolic ref, Objekt o) {
		if (this.classAllowed == null) {
			return false;
		}

		//ref is not used
		return o.getType().equals(this.classAllowed);
	}

	@Override
	public boolean requiresMax() {
		return false;
	}
	
	@Override
	public String toString() {
		return this.originExp + " ALIASES_INSTANCEOF " + this.classAllowed + " TRIGGERS " + 
				this.getTriggerSignature() + "(" + this.getTriggerMethodParameter() + ")";
	}
}