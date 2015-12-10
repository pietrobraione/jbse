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
	private final Signature triggerMethod;
	
	/** Should not be {@code null}. */
	private final String triggerParameter;

	public TriggerRule(String originExp, Signature triggerMethod, String triggerParameter) { 
		super(originExp);
		this.triggerMethod = triggerMethod;
		this.triggerParameter = triggerParameter;
	}
	
	public Signature getTriggerSignature() {
		return this.triggerMethod;
	}
	
	public String getTriggerMethodParameter() {
		return this.triggerParameter;
	}

	final boolean isTargetOfTrigger(ReferenceSymbolic ref, Objekt o) {
		if (this.getTriggerMethodParameter() == null) {
			return false;
		}
		if (o.getOrigin() == null) {
			return false;
		}
		
		//makes the pattern
		final String specializedTriggerExp;
		if (this.originExp == null) {
			specializedTriggerExp = this.getTriggerMethodParameter();
		} else {
			final String valueForAny = findAny(this.originExp, ref.getOrigin());
			specializedTriggerExp = specializeAny(this.getTriggerMethodParameter(), valueForAny);
		}
		final Pattern p = makePatternRelative(specializedTriggerExp, ref.getOrigin());

		// checks o's origin matches the resulting pattern
		final Matcher m = p.matcher(o.getOrigin().toString());
		final boolean retVal = m.matches();
		return retVal;
	}
}
