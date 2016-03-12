package gugit.om.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * this annotation marks a column that DOES NOT EXIST in a resultset and SHOULD NOT BE persisted to the resultset either. 
 * 
 * @author urbonman
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Transient {

}
