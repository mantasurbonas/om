package gugit.om.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
public @interface Pojo {	
	
	/***
	 * if not empty: 
	 * specifies a column on my entity to store referenced Pojo's ID
	 * (same behaviour as masterRef annotation)
	 */
	String myColumn() default "";
}
