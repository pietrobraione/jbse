package jbse.meta;

import java.util.Random;

import jbse.meta.annotations.MetaOverridden;


/**
 * The methods in this class can be invoked by the analyzed code to 
 * inform the symbolic executor about things that happen or to ask
 * it to do something.
 * 
 * @author Pietro Braione
 */
final public class Analysis {
	private static boolean mayViolateAssumptions = true;
	
	private Analysis() { } //no instances of this class, thanks
	
	/**
	 * Checks whether JBSE is the current JVM. 
	 * 
	 * @return {@code true} iff the JVM running this method
	 *         is JBSE.
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeIsRunByJBSE")
	public static boolean isRunByJBSE() {
		return false; //if this statement is executed, then it is not run by JBSE
	}
	
	/**
	 * Asks JBSE to stop the execution of the current trace and
	 * discard it because it violates some assumption. It is dual 
	 * to {@link #fail()}. 
	 * It is invoked by instrumentation code to discard execution 
	 * traces that will not fail in future, or that are of no 
	 * interest for the analysis (e.g., because they violate 
	 * some assumed precondition).
	 * When executed on a JVM different from JBSE, it 
	 * exits with error code 99 unless {@link #disableAssumptionViolation()} 
	 * has been invoked before, in which case it does nothing.
	 * 
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeIgnore")
	public static void ignore() { 
		if (mayViolateAssumptions) {
			System.exit(99);
		}
	}
	
	private static String getInvoker() {
		final Throwable foo = new Throwable();
		final StackTraceElement[] stackTrace = foo.getStackTrace();
		final String thisClassName = Analysis.class.getCanonicalName(); 
		StackTraceElement invoker = null;
		//index 0 is the top
		for (int i = 0; i < stackTrace.length; ++i) {
			if (stackTrace[i].getClassName().startsWith(thisClassName)) {
				continue; //do nothing, continue is pleonastic
			} else {
				invoker = stackTrace[i];
				break;
			}
		}
		if (invoker == null) {
			return "an unknown statement.";
		} else {
			return invoker.getClassName() + "." + invoker.getMethodName() + " (line " + invoker.getLineNumber() + ").";
		}
	}
	
	/**
	 * Informs JBSE that the current execution will no 
	 * longer add assumptions henceforth, and thus that
	 * any subsequent call to {@link #succeed()}
	 * should have no effect. When executed on a JVM different
	 * from JBSE the effect is identical (see also {@link #ignore()}).
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeDisableAssumptionViolation")
	public static void disableAssumptionViolation() { 
		Analysis.mayViolateAssumptions = false;
	}
	
	/**
	 * Equivalent to {@code if (!b) }{@link #ignore()}; it 
	 * should be used whenever you do not want to continue
	 * execution because you have detected the violation of
	 * some "meaningfulness condition": 
	 * preconditions, data structure invariants, 
	 * bounds on inputs, etc.  
	 * When executed on a JVM different from JBSE it prints 
	 * a message and then behaves as {@link #ignore()}.
	 * 
	 * @param b a {@code boolean}.
	 */
	public static void assume(boolean b) {
		if (!b) {
			if (!isRunByJBSE() && mayViolateAssumptions) {
				System.out.print("Assumption violated by " + getInvoker());
			}
			ignore();
		}
	}
	
	/**
	 * Dual to {@link #ignore()}; it should be 
	 * used whenever you are sure that the current program 
	 * behavior is correct, and will not become incorrect 
	 * henceforth, e.g., because has passed all the 
	 * assertions.
	 * When executed on a JVM different from JBSE it prints 
	 * a message and exits with code 100.
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeSucceed")
	public static void succeed() {
		System.out.print("Execution finished with no errors at " + getInvoker());
		System.exit(100);
	}
	
	/**
	 * Asks JBSE to stop the execution of the current trace
	 * and report it. It is dual to {@link #succeed()}.
	 * It is used whenever instrumentation code discovers a failure. 
	 * of the software under analysis (e.g., a violation of an assertion). 
	 * When executed on a JVM different from JBSE it prints a message and 
	 * exits with error code 98.
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeFail")
	public static void fail() {
		System.out.println("Assertion violated by " + getInvoker());
		System.exit(98);
	}

	/**
	 * Equivalent to {@code if (!b) }{@link #fail()}.
	 * 
	 * @param b a {@code boolean}.
	 */
	public static void ass3rt(boolean b) {
		if (!b) {
			Analysis.fail();
		}
	}
	
	@MetaOverridden("jbse.meta.algo.SEInvokeAssertRepOk")
	public static void assertFinallyEFInitialState(Object target, String methodName) {
		//TODO ???
	}
	
	private static Random r = new Random();

	/**
	 * Asks the symbolic executor to return the "any" value, 
	 * i.e., a special value that signifies the "union" of 
	 * both {@code true} and {@code false}, yielding both 
	 * alternatives whenever it is evaluated. When executed on a 
	 * JVM different from JBSE it draws a random 
	 * {@code boolean} value. Note that this semantics is as close as possible, but 
	 * <em>not</em> equivalent to the one given by JBSE: e.g., when running 
	 * {@code boolean x = any(); if (x != x) foo();} JBSE <em>will</em> 
	 * analyze the case where {@code foo} is invoked, while an ordinary JVM 
	 * will never invoke {@code foo}, whatever value returns {@code any()}.
	 * 
	 * @return a {@code boolean}. 
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeAny")
	public static boolean any() {
		return r.nextBoolean();
	}
	
	/**
	 * Forces the symbolic executor to decide a condition 
	 * by splitting the current symbolic state into the substates 
	 * corresponding to the possible values of the condition. 
	 * Use it whenever you should set the value of an instrumentation
	 * variable. When executed on a JVM different from JBSE it 
	 * does nothing else but evaluating its parameter and returning
	 * its value.
	 * 
	 * @param b the {@code boolean} value to be decided.
	 * @return {@code b} (always behaves as an identity).
	 */
	public static boolean force(boolean b) {
		boolean retVal = false;
		if (b) {
			retVal = true;
		}
		return retVal;
	}
	
	/**
	 * If the symbolic execution is guided by a concrete one, 
	 * ends guidance and starts an unguided exploration.
	 * When executed on a JVM different from JBSE it does nothing.
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeEndGuidance")
	public static void endGuidance() { }
	
	
	/**
	 * Asks the symbolic executor whether a field in an object 
	 * if its type is reference, it is resolved. When executed 
	 * on a JVM different from JBSE it returns {@code true}.
	 * 
	 * @param obj an {@link Object}.
	 * @param fieldName a {@link String}, the name of a field.
	 * @return {@code true} if {@code obj.fieldName} does not 
	 *         indicate the name of a field, is a primitive type
	 *         field, or it is a reference type field and its 
	 *         value is initialized.
	 */
	@MetaOverridden("jbse.meta.algo.SEInvokeIsResolved")
	public static boolean isResolved(Object obj, String fieldName) {
		return true;
	}
}
