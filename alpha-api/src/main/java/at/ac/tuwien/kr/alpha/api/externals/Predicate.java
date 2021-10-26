package at.ac.tuwien.kr.alpha.api.externals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for discovery of methods that represent
 * external predicates at runtime.
 *
 * In order to have your method detected by Alpha, annotate it
 * with this annotation. Also make sure to configure your instance
 * of Alpha to scan the package that contains the annotated method(s).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {
	/**
	 * The name of the predicate that will be used to refer to
	 * the annotated method. If it is the empty string (which
	 * is also the default value), then the name of the annotated
	 * method will be used.
	 */
	String name() default "";
}
