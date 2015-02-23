package jbse.meta.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target is a concretization check that 
 * may be invoked at the end of a trace. The annotation 
 * only applies to nonstatic (instance) parameterless methods with 
 * boolean return value, and only one method in a same class may 
 * be annotated with it. Whenever a fresh symbolic object is assumed, 
 * all the annotated methods are invoked at the base level on all the 
 * symbolic objects in the heap. 
 * If the execution is guided the method is not invoked.
 * 
 * @author Pietro Braione
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ConcretizationCheck {
}
