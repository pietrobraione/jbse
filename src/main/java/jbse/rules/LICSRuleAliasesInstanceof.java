package jbse.rules;

import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * An alias resolution rule mapping a pattern for
 * possible origins to a class of objects to which a 
 * {@link ReferenceSymbolic} can be resolved by aliasing.
 * It also encompasses the special case of "aliases nothing"
 * rules.
 * 
 * @author Pietro Braione
 */
public class LICSRuleAliasesInstanceof extends LICSRuleAliases {
	/** {@code null} means "aliases nothing". */
	private final String classAllowed;

	public LICSRuleAliasesInstanceof(String originExp, String classAllowed) {
		super(originExp);
		this.classAllowed = classAllowed;
	}

	@Override
	public boolean satisfies(ReferenceSymbolic ref, Objekt o) {
		if (isNothingRule()) {
			return false;
		}
		return this.classAllowed.equals(o.getType().getClassName());
	}

	@Override
	public boolean requiresMax() {
		return false;
	}

	/**
	 * Checks whether this is an "aliases nothing" rule.
	 * 
	 * @return {@code true} iff the rule is an 
	 *         "aliases nothing" rule.
	 */
	private boolean isNothingRule() {
		return this.classAllowed == null;
	}

	@Override
	public String toString() {
		return this.originExp + " aliases " + (this.classAllowed == null ? "nothing" : ("instanceof " + this.classAllowed));
	}
}