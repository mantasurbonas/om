package gugit.om.metadata;

import java.lang.reflect.Field;

public class DetailCollectionFieldMetadata extends FieldMetadata{

	private Class<?> detailType;
	private int columnOffset;
	
	public DetailCollectionFieldMetadata(Field field, Class<?> detailType, int columnOffset) {
		super(field);
		super.setType(detailType);
		this.detailType = detailType;
		this.columnOffset = columnOffset;
	}

	public Class<?> getType(){
		return detailType;
	}
	
	public int getColumnOffset(){
		return columnOffset;
	}
}
