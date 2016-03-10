package gugit.om.mapping;

import gugit.om.mapping.FieldMapping.Type;

import java.lang.reflect.Field;

import com.esotericsoftware.reflectasm.MethodAccess;

public class FieldMappingFactory {

	public static FieldMapping column(Field field, String colName, MethodAccess access){
		return new FieldMapping(field, colName, Type.COLUMN, access); 
	}
	
	public static FieldMapping dummy(){
		return new FieldMapping(Type.IGNORE);
	}
	
	public static FieldMapping oneToOne(Field field, MethodAccess access, EntityMetadata<?> metadata){
		FieldMapping res = new FieldMapping(field, Type.ONE_TO_ONE, access, metadata);
		return res;
	}
	
	public static FieldMapping oneToMany(Field field, MethodAccess access, EntityMetadata<?> metadata){
		FieldMapping res = new FieldMapping(field, Type.ONE_TO_MANY, access, metadata);
		return res;
	}

}
