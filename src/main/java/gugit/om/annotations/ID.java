package gugit.om.annotations;


import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target(FIELD)
@Retention(RUNTIME)
public @interface ID {

	String name() default "ID";
}
