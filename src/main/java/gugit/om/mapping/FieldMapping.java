package gugit.om.mapping;

import java.lang.reflect.Field;
import java.util.Collection;

import com.esotericsoftware.reflectasm.MethodAccess;

public class FieldMapping {
		
	public enum Type{
		COLUMN,
		IGNORE,
		ONE_TO_ONE,
		ONE_TO_MANY
	};
	
	// type of a field mapping - none, simple, one2one, one2many
	private Type type; 
	
	private MethodAccess access;
	
	// how to set value on that field
	private int setterIndex;
	
	// how to retrieve value from that field
	private int getterIndex;

	// in case of a simple column: optionally, name of a column
	private String colName;
	
	// in case of a complex object: optionally, a description of this field 
	private EntityMetadata<?> metadata;

	
	FieldMapping(Type type){
		this.type = type;
	}
	
	FieldMapping(Field field, String colName, Type type, MethodAccess access){
		this.colName = colName;
		this.type = type;
		this.access = access;
		this.setterIndex = access.getIndex(setterName(field));
		this.getterIndex = access.getIndex(getterName(field));
	}

	FieldMapping(Field field, Type type, MethodAccess access, EntityMetadata<?> metadata){
		this.type = type;
		this.access = access;
		this.setterIndex = access.getIndex(setterName(field));
		this.getterIndex = access.getIndex(getterName(field));
		this.metadata = metadata;
	}
	
	public void invokeSetter(Object entity, Object value){
		access.invoke(entity, this.setterIndex, value);
	}
	
	public Object invokeGetter(Object entity){
		return access.invoke(entity, this.getterIndex);
	}
		
	@SuppressWarnings("unchecked")
	public void invokeAdder(Object entity, Object value) {
		if (value == null)
			return;
		
		@SuppressWarnings("rawtypes")
		Collection collection = (Collection)invokeGetter(entity);
		if (!collection.contains(value))
			collection.add(value);
	}
	
	public String getColName() {
		return colName;
	}

	public Type getType() {
		return type;
	}
	
	public EntityMetadata<?> getMetadata() {
		return metadata;
	}
	
	private static String getterName(Field field){
		try {
			return "get"+camelCase(field.getName());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static String setterName(Field field){
		try {
			return "set"+camelCase(field.getName());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String camelCase(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}
}
