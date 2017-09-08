package jbse.rules;

import static jbse.rules.Util.makeOriginPattern;

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
	protected final String originExp;
	
	/**
	 * Constructor.
	 * 
	 * @param originExp a regular expression; {@code null} 
	 *        is equivalent to "match all".
	 */
	public Rule(String originExp) { 
		this.originExp = (originExp == null ? Util.ANY : originExp);
	}
	
	/**
	 * Checks if a reference matches this rule.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff {@code ref} 
	 *         matches this rule.
	 */
	public final boolean matches(ReferenceSymbolic ref) {
		// checks ref's origin matches the pattern
		final Pattern p = makeOriginPattern(this.originExp);
		final String originReference = ref.getOrigin().toString();
		final Matcher m = p.matcher(originReference);
		final boolean retVal = m.matches();
		return retVal;
	}
}
