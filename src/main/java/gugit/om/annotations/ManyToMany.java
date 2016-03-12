package gugit.om.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
public @interface ManyToMany {

	String joinTable();
	
	String myProperty();
	String col1();
	
	String othProperty();
	String col2();
	
	Class<?> othClass();
}