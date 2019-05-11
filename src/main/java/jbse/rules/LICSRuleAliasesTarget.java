package jbse.rules;

import static jbse.rules.Util.makeOriginPatternRelative;

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
public final class LICSRuleAliasesTarget extends LICSRuleAliases {
	/** Should not be {@code null}. */
	private final String targetExp;
	
	/** When {@code true} only the maximal path matches. */
	private final boolean hasMax;
	
	/** The toString version of this rule. */
	private final String toString;

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
		//TODO check targetExp != null
		if (targetExp.startsWith(Util.MAX)) {
			this.targetExp = targetExp.substring(Util.MAX.length()).trim();
			this.hasMax = true;
		} else {
			this.targetExp = targetExp;
			this.hasMax = false;
		}
		this.toString = originExp + " aliases target " + (this.hasMax ? Util.MAX : "") + this.targetExp;
	}

	@Override
	public boolean satisfies(ReferenceSymbolic ref, Objekt o) {
		//makes the pattern
		final Pattern p = makeOriginPatternRelative(this.targetExp, ref, this.originPattern);
		
		//checks if the origin of o matches the pattern
		final String originString = o.getOrigin().asOriginString();
		final Matcher m = p.matcher(originString);
		final boolean retVal = m.matches();
		return retVal;
	}
	
	@Override
	public boolean requiresMax() {
		return this.hasMax;
	}
	
	@Override
	public String toString() {
		return this.toString;
	}
}