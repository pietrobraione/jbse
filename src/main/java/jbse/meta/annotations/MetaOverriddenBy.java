package jbse.meta.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jbse.algo.Algo_INVOKEMETA;

/**
 * This annotation applies to methods. It indicates that the annotated 
 * has an implementation at the meta level that overrides its implementation
 * at the base level.
 * If such annotation is present on a method, when this is 
 * invoked at the base level the symbolic executor will instead
 * load at the meta level a class implementing {@link jbse.algo.Algorithm},
 * whose name is specified in this annotation's value,
 * and invoke its {@code exec} method by passing to it the expected
 * parameters. 
 * 
 * @author Pietro Braione
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MetaOverriddenBy {
	java.lang.Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>> value();
}
