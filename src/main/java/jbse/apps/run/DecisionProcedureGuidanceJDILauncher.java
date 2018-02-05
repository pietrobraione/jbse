package jbse.apps.run;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DecisionProcedureGuidanceJDILauncher {
    /**
     * This is the main method for the JVM created by {@link DecisionProcedureGuidanceJDI}. 
     * This main method loads the true target class and runs its
     * target method.
     * 
     * @param args a {@link String}{@code []}, {@code args[0]} is the
     *        name of a class and {@code args[1]} is the name of a 
     *        method implemented in {@code args[0]}.
     * @throws ClassNotFoundException if the class {@code args[0]} 
     *         does not exist.
     * @throws NoSuchMethodException if the method {@code args[1]} 
     *         with no parameters does not exist in the class {@code args[0]}.
     * @throws SecurityException possibly when accessing the method {@code args[1]}.
     * @throws InstantiationException if class {@code args[0]} is abstract, or
     *         interface, or array, or has no nullary constructor.
     * @throws IllegalAccessException if it is not possible to access to the
     *         nullary constructor in class {@code args[0]}.
     * @throws IllegalArgumentException should not happen (no arguments).
     * @throws InvocationTargetException if method {@code args[1]} throws an
     *         exception.
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, 
    SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, 
    InvocationTargetException {
        final Class<?> clazz = Class.forName(args[0]);
        final Method method = clazz.getDeclaredMethod(args[1]);
        method.setAccessible(true);
        Object o = clazz.newInstance();
        method.invoke(o);
    }
}
