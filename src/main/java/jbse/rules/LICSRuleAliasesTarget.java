package jbse.rules;

import static jbse.rules.Util.makePatternRelative;
import static jbse.rules.Util.findAny;
import static jbse.rules.Util.specializeAny;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.mem.Objekt;
import jbse.val.ReferenceSymbolic;

/**
 * An alias resolution rule mapping a pattern for
 * possible origins to a corresponding pattern for 
 * target origins to which a {@link ReferenceSymbolic} 
 * can be resolved by aliasing.
 * 
 * @author Pietro Braione
 */
public class LICSRuleAliasesTarget extends LICSRuleAliases {
	/** Should not be {@code null}. */
	private final String targetExp;
	
	/** When {@code true} only the maximal path matches. */
	private final boolean hasMax;
	
	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression over origin
	 *        {@link String}s: If an origin {@link String} 
	 *        matches it, then this rule fires. A {@code null} 
	 *        value is equivalent to "match all".
	 * @param targetExp a regular expression over origin
	 *        {@link String}s, yielding the possible targets; 
	 *        it must not be {@code null}.
	 */
	public LICSRuleAliasesTarget(String originExp, String targetExp) {
		super(originExp);
		if (targetExp.startsWith(Util.MAX)) {
			this.targetExp = targetExp.substring(Util.MAX.length()).trim();
			this.hasMax = true;
		} else {
			this.targetExp = targetExp;
			this.hasMax = false;
		}
	}

	@Override
	public boolean satisfies(ReferenceSymbolic ref, Objekt o) {
		//builds the pattern
		final String valueForAny = findAny(this.originExp, ref);
		final String specializedPathAllowedExp = specializeAny(this.targetExp, valueForAny);
		final Pattern p = makePatternRelative(specializedPathAllowedExp, ref);
		//checks if the origin of o matches the pattern
		final Matcher m = p.matcher(o.getOrigin().asOriginString());
		final boolean retVal = m.matches();
		
		return retVal;
	}
	
	@Override
	public boolean requiresMax() {
		return this.hasMax;
	}
	
	@Override
	public String toString() {
		return this.originExp + " aliases target " + (this.hasMax ? Util.MAX : "") + this.targetExp;
	}
}