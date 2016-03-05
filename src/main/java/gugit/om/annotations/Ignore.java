package gugit.om.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * this annotation marks a property that DOES exist in a resultset but should NOT be assigned to any object's property.
 * 
 * @author urbonman
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface Ignore {

}
