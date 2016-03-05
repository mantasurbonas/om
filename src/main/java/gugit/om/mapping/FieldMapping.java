package gugit.om.mapping;

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
	private int setterName;
	
	// how to retrieve value from that field
	private int getterName;

	// optionally, name of a column
	private String colName;
	
	// optionally, a helper used to deserialize a whole object for that property
	private EntityMapper<?> subMapper;

	
	private FieldMapping(Type type){
		this.type = type;
	}
	
	private FieldMapping(String name, Type type, MethodAccess access, String getterName, String setterName){
		this.colName = name;
		this.type = type;
		this.access = access;
		this.setterName = access.getIndex(setterName);
		this.getterName = access.getIndex(getterName);
	}
	
	
	public static FieldMapping column(String name, MethodAccess access, String getterName, String setterName){
		return new FieldMapping(name, Type.COLUMN, access, getterName, setterName); 
	}
	
	public static FieldMapping dummy(){
		return new FieldMapping(Type.IGNORE);
	}
	
	public static FieldMapping oneToOne(MethodAccess access, String getterName, String setterName, EntityMapper<?> mapper){
		FieldMapping res = new FieldMapping("<one2one>", Type.ONE_TO_ONE, access, getterName, setterName);
			res.subMapper = mapper;
		return res;
	}
	
	public static FieldMapping oneToMany(MethodAccess access, String getterName, String setterName, EntityMapper<?> mapper){
		FieldMapping res = new FieldMapping("one2many", Type.ONE_TO_MANY, access, getterName, setterName);
			res.subMapper = mapper;
		return res;
	}

	public String getColName() {
		return colName;
	}

	public int getSetterName() {
		return setterName;
	}

	public int getGetterName() {
		return getterName;
	}

	public Type getType() {
		return type;
	}

	public MethodAccess getMethodAccess(){
		return access;
	}
	
	public EntityMapper<?> getSubmapper() {
		return subMapper;
	}
}
