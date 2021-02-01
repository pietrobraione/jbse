package jbse.rules;

import static jbse.rules.Util.makeOriginPatternRelative;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.bc.Signature;
import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * A rule for triggers.
 * 
 * @author Pietro Braione
 *
 */
public abstract class TriggerRule extends Rule {
	/** Should not be {@code null}. */
	private final Signature triggerMethodSignature;
	
	/** When {@code null} means no parameter. */
	private final String triggerMethodParameter;

	public TriggerRule(String originExp, Signature triggerMethodSignature, String triggerMethodParameter) { 
		super(originExp);
		this.triggerMethodSignature = triggerMethodSignature;
		this.triggerMethodParameter = triggerMethodParameter;
	}
	
	public Signature getTriggerMethodSignature() {
		return this.triggerMethodSignature;
	}
	
	public String getTriggerMethodParameter() {
		return this.triggerMethodParameter;
	}

	/**
	 * Checks whether an {@link Objekt} is the parameter 
	 * of this trigger rule.
	 * 
	 * @param originTarget a {@link ReferenceSymbolic}, the origin of
	 *        the target object of this rule. In case of a {@link TriggerRuleExpandsTo}
	 *        it is the {@link ReferenceSymbolic} that matched the rule, in case 
	 *        of a {@link TriggerRuleAliases} it is the origin of the object that
	 *        the {@link ReferenceSymbolic} that matched the rule aliases.
	 * @param o a {@link ReferenceSymbolic}, the origin of an {@link Objekt}.
	 * @return {@code true} iff {@code o} is the parameter for
	 *         this rule.
	 * @throws NullPointerException if {@code o.}{@link Objekt#getOrigin() getOrigin}{@code () == null}.
	 */
	final boolean isTriggerMethodParameterObject(ReferenceSymbolic originTarget, ReferenceSymbolic originObject) {
		if (this.triggerMethodParameter == null) {
			return false;
		}
		
		//makes the pattern
		final Pattern p = makeOriginPatternRelative(this.triggerMethodParameter, originTarget, this.originPattern);

		//checks if the origin of o matches the pattern
		final String originString = originObject.asOriginString();
		final Matcher m = p.matcher(originString);
		final boolean retVal = m.matches();
		return retVal;
	}
}
