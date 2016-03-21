package gugit.om.metadata;

import java.lang.reflect.Field;


public class FieldMetadata {

	// a name of a field - just as specified by the developer
	private String name;

	// type of a field
	private Class<?> type;
	
	public FieldMetadata(Field field){
		this.name = field.getName();
		this.type = field.getType();
	}
		
	public String getName(){
		return name;
	}

	public Class<?> getType(){
		return type;
	}

	protected void setType(Class<?> type) {
		this.type = type;
	}
	
}
