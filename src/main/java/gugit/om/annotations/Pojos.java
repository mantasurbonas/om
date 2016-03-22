package gugit.om.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * marks a collection of details - that is, POJOs that point to my ID 
 * 	(as opposed to a "master" - the POJO that some of my property(ies) point to)
 * 
 * 
 * @author urbonman
 *
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Pojos {

	Class<?> detailClass();
}
