package jbse.meta.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation applies to methods. It indicates that the annotated 
 * method must be treated as a pure, uninterpreted function in its 
 * parameters with primitive types.
 * If a method is annotated with this annotation, when it is 
 * invoked at the base level the symbolic executor will not execute 
 * it, and instead will return a symbolic term. The term is the application 
 * of a function symbol, whose name is specified in this annotation's value,
 * to the actual parameters of the method invocation, in the same order.
 * 
 * @author Pietro Braione
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface Uninterpreted {
	String value();
}
