package jbse.rules;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.val.MemoryPath;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

public final class Util {
	static final String NOTHING = "{NOTHING}";
	static final String MAX = "{MAX}";
	static final String REF = "{REF}";
	static final String UP = "{UP}";
	static final String ANY = "{ANY}";

	/**
	 * Makes a regular expression pattern from an origin expression
	 * in a rule.
	 * 
	 * @param s a {@link String}, the origin expression of a rule.
	 * @return a {@link Pattern} for {@code s} against which the 
	 *         origin strings contained in the objects can match.
	 */
	public static Pattern makeOriginPattern(String s) {
		return Pattern.compile(translateToOriginPattern(s));
	}
	
	/**
	 * Makes absolute a relative pattern in a origin expression in 
	 * a rule.
	 * 
	 * @param s The relative origin expression in the rule.
	 * @param origin The origin w.r.t. we want to make the origin
	 *        absolute.
	 * @return {@code s} where all the occurrences of {REF} and 
	 *         {UP} are
	 *         resolved using {@code origin}.
	 */
	static Pattern makePatternRelative(String s, MemoryPath origin) {
		return Pattern.compile(translateToOriginPattern(translateRelativeToAbsolute(s, origin.toString())));
	}
	
	/* TODO this is really ugly, but it works with the current 
	 * implementation of origins as strings. Improve it later to a 
	 * separate language. 
	 */
	private static String translateToOriginPattern(String s) {
		String retVal = s.replace(ANY, "(.*)");
		retVal = retVal.replace("/", "\\."); 
		retVal = retVal.replace("{", "\\{"); //this is for {ROOT}
		retVal = retVal.replace("}", "\\}"); //this also is for {ROOT}
		return retVal;
	}

	/* TODO this also is really ugly, and it does not work with 
	 * multiple candidate origins for the same object.
	 */
	private static String translateRelativeToAbsolute(String s, String originString) {
		// replaces REF with ref.origin
		String retVal = s.replace(REF, originString.replace(".", "/"));
		
		// eats all /whatever/UP pairs 
		String retValOld;
		do {
			retValOld = retVal;
			retVal = retVal.replaceFirst("/[^/]+/\\Q" + UP + "\\E", "");
		} while (!retVal.equals(retValOld));
		return retVal;
	}
	
	static String findAny(String pattern, MemoryPath origin) {
		final Pattern p = makeOriginPattern(pattern);
		final Matcher m = p.matcher(origin.toString());
		if (m.matches() && m.pattern().pattern().startsWith("(.*)") && m.groupCount() >= 1) {
			final String valueForAny = m.group(1).replace(".","/");
			return valueForAny;
		} else {
			return null;
		}
	}
	
	static String specializeAny(String expression, String valueForAny) {
		return (valueForAny == null ? expression : expression.replace(ANY, valueForAny));	
	}

	/**
	 * Searches for the actual parameter of a trigger rule.
	 * 
	 * @param r a {@link TriggerRule}.
	 * @param ref a {@link ReferenceSymbolic}, the reference that
	 *        is resolved by {@code r}.
	 * @param state a {@link State}.
	 * 
	 * @return the first {@link Objekt} in the heap of {@code s} 
	 *         whose origin matches the trigger parameter part 
	 *         of {@code r}, or {@code null} if none exists in the heap.
	 */
	public static ReferenceConcrete getTriggerMethodParameterObject(TriggerRule r, ReferenceSymbolic ref, State state) {
		ReferenceConcrete retVal = null;
		final Map<Long, Objekt> allObjs = state.getHeap();
		for (Map.Entry<Long, Objekt> e : allObjs.entrySet()){
			if (r.isTargetOfTrigger(ref, e.getValue())){
				retVal = new ReferenceConcrete(e.getKey());
				break;
			}
		}
		return retVal;
	}

	//do not instantiate it!
	private Util() { }
}
