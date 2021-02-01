package jbse.rules;

import static jbse.rules.Util.ANY;
import static jbse.rules.Util.makeOriginPatternAbsolute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.val.ReferenceSymbolic;

/**
 * A rule predicating on the origin of a symbolic reference
 * by means of an extended regular expression language.
 * Rules are immutable.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Rule {
	protected final Pattern originPattern;
	
	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression over origin
	 *        {@link String}s: If an origin {@link String} 
	 *        matches it, then this rule fires. A {@code null} 
	 *        value is equivalent to "match all".
	 */
	public Rule(String originExp) { 
		this.originPattern = makeOriginPatternAbsolute(originExp == null ? ANY : originExp);
	}
	
	/**
	 * Checks if a reference matches this rule.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff {@code ref} 
	 *         matches this rule.
	 */
	public final boolean matches(ReferenceSymbolic ref) {
		//checks if the origin of ref origin matches the pattern
		final String originString = ref.asOriginString();
		final Matcher m = this.originPattern.matcher(originString);
		final boolean retVal = m.matches();
		return retVal;
	}
}
