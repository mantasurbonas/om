package gugit.om.metadata;

import java.lang.reflect.Field;

public class PojoFieldMetadata extends FieldMetadata{

	private int columnOffset;

	public PojoFieldMetadata(Field field, int columnOffset) {
		super(field);
		this.columnOffset = columnOffset;
	}
	
	public int getColumnOffset(){
		return columnOffset;
	}

}
