package jbse.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.mem.Clause;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.ReferenceSymbolic;

public final class Util {
	//These must match the analogous definitions in SettingsParser.jj
	static final String MAX = "{MAX}";
	static final String ANY = "{R_ANY}";
	static final String REF = "{$REF}";
	static final String REFANY = "{$R_ANY}";
	static final String UP = "{UP}";
	static final String REGEX_ALLCHARS = "{°}";
	static final String REGEX_ENDLINE = "\\{EOL\\}"; //braces must be escaped because REGEX_ENDLINE substitution comes after brace substitution

	/**
	 * Makes a regular expression pattern from an absolute origin expression
	 * in a rule.
	 * 
	 * @param originExpAbsolute a {@link String}, the absolute origin expression.
	 * @return a {@link Pattern} for {@code originExpAbsolute}.
	 */
	static Pattern makeOriginPatternAbsolute(String originExpAbsolute) {
		return Pattern.compile(translateToOriginPatternString(originExpAbsolute));
	}
	
	/**
	 * Makes a {@link Pattern} for a relative origin expression in 
	 * a rule.
	 * 
	 * @param originExpRelative a {@link String}, the relative origin 
	 *        expression.
	 * @param originTarget a {@link ReferenceSymbolic}, the origin that 
	 *        all the occurrences of {$R_ANY} and {$REF} in 
	 *        {@code originExpRelative} refer to.
	 * @param originPattern a {@link Pattern} that is used to detect 
	 *        the {$R_ANY} in {@code originExpRelative}; {@code originExpRelative}
	 *        must match it, and the first capture group will be used as
	 *        {$R_ANY}.
	 * @return a {@link Pattern} for {@code originExpRelative}.
	 */
	static Pattern makeOriginPatternRelative(String originExpRelative, ReferenceSymbolic originTarget, Pattern originPattern) {
		final String valueForAny = findAny(originPattern, originTarget);
		final String specializedOriginExpRelative = specializeAny(originExpRelative, valueForAny);
		return makeOriginPatternAbsolute(translateOriginExpressionRelativeToAbsolute(specializedOriginExpRelative, originTarget));
	}
	
	/* TODO this is really ugly, but it works with the current 
	 * implementation of origins as strings. Improve it later to a 
	 * separate language. 
	 */
	private static String translateToOriginPatternString(String originExpAbsolute) {
		String retVal = originExpAbsolute.replace(".", "\\."); 
		retVal = retVal.replace(ANY, "(.*)");
		retVal = retVal.replace(REGEX_ALLCHARS, "."); 
		retVal = retVal.replace("{", "\\{"); //this is for {ROOT}
		retVal = retVal.replace("}", "\\}"); //this also is for {ROOT}
		retVal = retVal.replace("[", "\\["); //this is for [<className>] and ::KEY...[...]
		retVal = retVal.replace("]", "\\]"); //this also is for [<className>] and ::KEY...[...]
		retVal = retVal.replace("$", "\\$"); //this is for names of inner classes
		retVal = retVal.replace(REGEX_ENDLINE, "$"); 
		return retVal;
	}

	/* TODO this also is really ugly, and it does not work with 
	 * multiple candidate origins for the same object.
	 */
	private static String translateOriginExpressionRelativeToAbsolute(String originExpRelative, ReferenceSymbolic origin) {
		final String originString = origin.asOriginString();
		
		//replaces {$REF} with ref.origin
		String retVal = originExpRelative.replace(REF, originString);
		
		//eats all /whatever/UP pairs 
		String retValOld;
		do {
			retValOld = retVal;
			retVal = retVal.replaceFirst("\\.[^\\.]+\\.\\Q" + UP + "\\E", "");
		} while (!retVal.equals(retValOld));
		return retVal;
	}
	
	static String findAny(Pattern originPattern, ReferenceSymbolic origin) {
		final Matcher m = originPattern.matcher(origin.asOriginString());
		if (m.matches() && m.pattern().pattern().startsWith("(.*)") && m.groupCount() >= 1) {
			final String valueForAny = m.group(1);
			return valueForAny;
		} else {
			return null;
		}
	}
	
	static String specializeAny(String expression, String valueForAny) {
		return (valueForAny == null ? expression : expression.replace(REFANY, valueForAny));	
	}

	/**
	 * Searches for the actual parameter of a trigger rule.
	 * 
	 * @param r a {@link TriggerRule}.
	 * @param originTarget a {@link ReferenceSymbolic}, the origin of
	 *        the target object that made fire {@code r}. In case of 
	 *        a {@link TriggerRuleExpandsTo} it is the {@link ReferenceSymbolic}
	 *        that matched the rule, in case of a {@link TriggerRuleAliases} it 
	 *        is the origin of the object that the {@link ReferenceSymbolic} that
	 *        matched the rule aliases.
	 * @param state a {@link State}.
	 * @return a {@link ReferenceSymbolic} to (i.e., the origin of) 
	 *         the first {@link Objekt} in the heap of {@code state} 
	 *         that matches the trigger parameter part 
	 *         of {@code r}, or {@code null} if none exists.
	 * @throws FrozenStateException if {@code state} is frozen.
	 */
	public static ReferenceSymbolic getTriggerMethodParameterObject(TriggerRule r, ReferenceSymbolic originTarget, State state) throws FrozenStateException {
        final Iterable<Clause> pathCondition = state.getPathCondition(); //TODO the decision procedure already stores the path condition: eliminate dependence on state
        for (Clause c : pathCondition) {
            if (c instanceof ClauseAssumeExpands) {
                //gets the object and its position in the heap
                final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                final Objekt object = cExp.getObjekt();
                final ReferenceSymbolic originObject = object.getOrigin();
                
    			if (r.isTriggerMethodParameterObject(originTarget, originObject)){
    				return originObject;
    			}
            }
        }
        //in the case r is a TriggerRuleNull, it is possible that the
        //rule parameter is originTarget, which does not point to any
        //object, so we must check this possible situation
        if (r instanceof TriggerRuleNull && r.isTriggerMethodParameterObject(originTarget, originTarget)) {
        	return originTarget;
        }
		return null;
	}

	//do not instantiate it!
	private Util() { }
}
