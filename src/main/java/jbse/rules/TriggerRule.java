package jbse.rules;

import static jbse.rules.Util.findAny;
import static jbse.rules.Util.makePatternRelative;
import static jbse.rules.Util.specializeAny;

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
	 * @param ref the {@link ReferenceSymbolic} that made fire 
	 *        this rule.
	 * @param o an {@link Objekt}. It must be symbolic.
	 * @return {@code true} iff {@code o} is the parameter for
	 *         this rule.
	 * @throws NullPointerException if {@code o.}{@link Objekt#getOrigin() getOrigin}{@code () == null}.
	 */
	final boolean isTriggerMethodParameterObject(ReferenceSymbolic ref, Objekt o) {
		if (this.triggerMethodParameter == null) {
			return false;
		}
		
		//makes the pattern
		final String specializedTriggerExp;
		if (this.originExp == null) {
			specializedTriggerExp = this.getTriggerMethodParameter();
		} else {
			final String valueForAny = findAny(this.originExp, ref);
			specializedTriggerExp = specializeAny(this.getTriggerMethodParameter(), valueForAny);
		}
		final Pattern p = makePatternRelative(specializedTriggerExp, ref);

		//checks o's origin matches the resulting pattern
		final ReferenceSymbolic origin = o.getOrigin();
		final Matcher m = p.matcher(origin.asOriginString());
		final boolean retVal = m.matches();
		return retVal;
	}
}
