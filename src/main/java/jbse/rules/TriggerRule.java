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
		final Pattern p = makeOriginPatternRelative(this.triggerMethodParameter, ref, this.originPattern);

		//checks if the origin of o matches the pattern
		final String originString = o.getOrigin().asOriginString();
		final Matcher m = p.matcher(originString);
		final boolean retVal = m.matches();
		return retVal;
	}
}
