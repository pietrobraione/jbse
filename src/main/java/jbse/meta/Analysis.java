package jbse.meta;

import java.lang.reflect.Field;
import java.util.Random;

import sun.misc.Unsafe;

/**
 * The methods in this class can be invoked by the analyzed code to 
 * inform the symbolic executor about things that happen or to ask
 * it to do something.
 * 
 * @author Pietro Braione
 */
final public class Analysis {
    private Analysis() { } //no instances of this class, thanks

    /**
     * Checks whether JBSE is the current JVM. 
     * 
     * @return {@code true} iff the JVM running this method
     *         is JBSE.
     */
    public static boolean isRunByJBSE() {
        return false; //if this statement is executed, then it is not run by JBSE
    }

    /**
     * Asks JBSE to stop the execution of the current path and
     * discard it because it violates some assumption. It is dual 
     * to {@link #fail()}. 
     * It is invoked by instrumentation code to discard execution 
     * paths that will not fail in future, or that are of no 
     * interest for the analysis (e.g., because they violate 
     * some assumed precondition).
     * When executed on a JVM different from JBSE, it 
     * exits with error code 99.
     * 
     */
    public static void ignore() { 
        System.out.print("Assumption violated by " + getInvoker());
        System.exit(99);
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
            ignore();
        }
    }

    /**
     * Opposite to {@link #fail()}; it should be 
     * used whenever you are sure that the current program 
     * behavior is correct, and will not become incorrect 
     * henceforth, e.g., because has passed all the 
     * assertions.
     * When executed on a JVM different from JBSE it prints 
     * a message and exits with code 100.
     */
    public static void succeed() {
        System.out.print("Execution finished with no errors at " + getInvoker());
        System.exit(100);
    }

    /**
     * Asks JBSE to stop the execution of the current path
     * and report it. It is dual to {@link #ignore()} and 
     * opposite to {@link #succeed()}.
     * It is used whenever instrumentation code discovers a failure. 
     * of the software under analysis (e.g., a violation of an assertion). 
     * When executed on a JVM different from JBSE it prints a message and 
     * exits with error code 98.
     */
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
            fail();
        }
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
    public static void endGuidance() { }

    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param b a {@code byte}.
     * @return {@code true} iff {@code b} is symbolic.
     */
    public static boolean isSymbolic(byte b) {
        return false;
    }

    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param c a {@code char}.
     * @return {@code true} iff {@code c} is symbolic.
     */
    public static boolean isSymbolic(char c) {
        return false;
    }

    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param s a {@code short}.
     * @return {@code true} iff {@code s} is symbolic.
     */
    public static boolean isSymbolic(short s) {
        return false;
    }

    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param i an {@code int}.
     * @return {@code true} iff {@code i} is symbolic.
     */
    public static boolean isSymbolic(int i) {
        return false;
    }

    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param l a {@code long}.
     * @return {@code true} iff {@code l} is symbolic.
     */
    public static boolean isSymbolic(long l) {
        return false;
    }
    
    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param z a {@code boolean}.
     * @return {@code true} iff {@code z} is symbolic.
     */
    public static boolean isSymbolic(boolean z) {
        return false;
    }
        
    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param f a {@code float}.
     * @return {@code true} iff {@code f} is symbolic.
     */
    public static boolean isSymbolic(float f) {
        return false;
    }
        
    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param d a {@code double}.
     * @return {@code true} iff {@code d} is symbolic.
     */
    public static boolean isSymbolic(double d) {
        return false;
    }
    
    /**
     * Checks if the argument is a symbolic value.
     * When executed on a JVM different from JBSE
     * always returns {@code false}.
     * 
     * @param o a (reference to an) {@link Object}.
     * @return {@code true} iff {@code o} is symbolic.
     */
    public static boolean isSymbolic(Object o) {
        return false;
    }
    
    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param z a {@code boolean}.
     * @return if {@code z} is symbolic a {@link String} 
     *         with {@code z}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(boolean z) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param b a {@code byte}.
     * @return if {@code b} is symbolic a {@link String} 
     *         with {@code b}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(byte b) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param c a {@code char}.
     * @return if {@code c} is symbolic a {@link String} 
     *         with {@code c}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(char c) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param d a {@code double}.
     * @return if {@code d} is symbolic a {@link String} 
     *         with {@code d}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(double d) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param f a {@code float}.
     * @return if {@code f} is symbolic a {@link String} 
     *         with {@code f}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(float f) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param i an {@code int}.
     * @return if {@code i} is symbolic a {@link String} 
     *         with {@code i}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(int i) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param l a {@code long}.
     * @return if {@code l} is symbolic a {@link String} 
     *         with {@code l}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(long l) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param s a {@code short}.
     * @return if {@code s} is symbolic a {@link String} 
     *         with {@code s}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(short s) {
        return null;
    }

    /**
     * Returns a {@link String} with the symbol name
     * if its argument is symbolic, or {@code null}
     * if it isn't. When executed on a JVM different 
     * from JBSE always returns {@code null}.
     *  
     * @param o a (reference to an) {@link Object}.
     * @return if {@code o} is symbolic a {@link String} 
     *         with {@code o}'s name, {@code null}
     *         otherwise. 
     */
    public static String symbolName(Object o) {
        return null;
    }

    /**
     * Asks the symbolic executor whether a field in an object, 
     * if its type is reference, it is resolved. When executed 
     * on a JVM different from JBSE it returns {@code true}.
     * 
     * @param obj an {@link Object}.
     * @param fieldName a {@link String}, the name of a field.
     * @return {@code true} if {@code obj.fieldName} does not 
     *         indicate the name of a field, is a primitive type
     *         field, or it is a reference type field and its 
     *         value is resolved.
     */
    public static boolean isResolved(Object obj, String fieldName) {
        return true;
    }
    
    //TODO isResolved also for array members
    
    /**
     * Checks whether a reference is resolved by alias. Note that
     * invocation of this method causes resolution, if the reference
     * was not resolved before.
     * When executed on a JVM different from JBSE it returns {@code false}.
     * 
     * @param o a (reference to an) {@link Object}.
     * @return {@code true} if {@code o} is symbolic and resolved 
     *         by alias, {@code false} otherwise.
     */
    public static boolean isResolvedByAlias(Object o) {
    	return false;
    }

    /**
     * Checks whether a reference is resolved by expansion. Note that
     * invocation of this method causes resolution, if the reference
     * was not resolved before.
     * When executed on a JVM different from JBSE it returns {@code false}.
     * 
     * @param o a (reference to an) {@link Object}.
     * @return {@code true} if {@code o} is symbolic and resolved 
     *         by expansion, {@code false} otherwise.
     */
    public static boolean isResolvedByExpansion(Object o) {
    	return false;
    }

    /**
     * Assumes that a class has not yet been initialized; if 
     * it is initialized it {@link #ignore() ignore}s the current
     * path. When executed on a JVM different from JBSE, if
     * the class is initialized exits with error code 99.
     * 
     * @param classToCheck the {@link Class}.
     */
    public static void assumeClassNotInitialized(Class<?> classToCheck) {
        //ugly Unsafe trickery, but it is the only way to check initialization
        //without triggering it
        try {
            final Field fieldUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            fieldUnsafe.setAccessible(true);
            final Unsafe unsafe = (Unsafe) fieldUnsafe.get(null);
            if (!unsafe.shouldBeInitialized(classToCheck)) {
                System.exit(99);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError();
        }
    }
}
