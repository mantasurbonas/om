package gugit.om.mapping;

import java.lang.reflect.Field;
import java.util.Collection;

import com.esotericsoftware.reflectasm.MethodAccess;


/***
 * Knows how to set/get value on a specified property of any object
 * 
 * @author urbonman
 *
 */
public class Binding{

	private MethodAccess access;
	private int setterIndex;
	private int getterIndex;
	private boolean isCollection = false;
	
	protected Binding(){}
	
	public Binding(MethodAccess access, String propName, boolean isCollection){
		this.access = access;
		this.setterIndex = access.getIndex(setterName(propName));
		this.getterIndex = access.getIndex(getterName(propName));
		this.isCollection = isCollection;
	}
	
	public Binding(MethodAccess access, Field field){
		this(access, getFieldName(field), checkIsCollection(field));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void assignValueTo(Object entity, Object value){
		if (!isCollection){
			invokeSetter(entity, value);
		}else{
			if (value == null)
				return;
			Collection c = (Collection)retrieveValueFrom(entity);
			if (!c.contains(value))
				c.add(value);
		}
	}
	
	public Object retrieveValueFrom(Object entity){
		return access.invoke(entity, this.getterIndex);
	}
	
	public boolean isCollection() {
		return isCollection;
	}
	
	private void invokeSetter(Object entity, Object value){
		access.invoke(entity, this.setterIndex, value);
	}
	
	private static boolean checkIsCollection(Field field) {
		return field.getType().isInstance(java.util.Collection.class);
	}
	
	private static String getFieldName(Field field){
		try {
			return field.getName();
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String getterName(final String fieldName){
		return "get"+camelCase(fieldName);
	}

	private static String setterName(final String fieldName){
		return "set"+camelCase(fieldName);
	}
	
	private static String camelCase(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}

}
