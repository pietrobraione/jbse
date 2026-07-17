package jbse.apps;

import java.util.Map;
import java.util.Set;

/**
 * A formatter for emitting Evosuite objectives
 * as evaluation functions. An evaluation function
 * compares a test produced by Evosuite against 
 * symbolic state(s) (typically a single final one) produced 
 * by symbolic execution, and returns positive 
 * value(s) standing for the distance of the final state
 * produced by running the test from the symbolic 
 * state(s). Such distance value(s) can be used by Evosuite 
 * as a fitness value(s) for the test. The evaluation
 * function is a Java class (in source code form)
 * called EvoSuiteWrapper_<n>_<m> with method(s) called 
 * test<k> accepting the same input parameters as 
 * the method under test. The EvoSuiteWrapper class
 * must be linked against sushi-lib to work.
 * 
 * @author Pietro Braione
 */
public interface FormatterSushi extends Formatter {
	/**
	 * Used to pass to the formatter the list of the
	 * string literals (constant) that have been created
	 * during symbolic execution.
	 * 
	 * @param stringLiterals a {@link Map}{@code <}{@link Long}{@code , }{@link String}{@code >}
	 *        mapping the heap positions of the literals to their string values.
	 */
	void setStringsConstant(Map<Long, String> stringLiterals);
	
	/**
	 * Used to pass to the formatter the set of the
	 * strings (nonconstant) that have been created
	 * during symbolic execution.
	 * 
	 * @param stringOthers a {@link Set}{@code <}{@link Long}{@code >}
	 *        containing the heap positions of the strings.
	 */
	void setStringsNonconstant(Set<Long> stringOthers);
	
	/**
	 * Used to pass to the formatter the set of 
	 * forbidden reference expansions. This can
	 * be used whenever we want to check for an
	 * expansion of a reference. 
	 * 
	 * @param forbiddenExpansions a {@link Set}{@code <}{@link String}{@code >}
	 *        containing the (internal) type name of a forbidden reference expansion.
	 */
	void setForbiddenExpansions(Set<String> forbiddenExpansions);
}
