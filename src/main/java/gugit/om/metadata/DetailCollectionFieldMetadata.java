package gugit.om.metadata;

import java.lang.reflect.Field;

public class DetailCollectionFieldMetadata extends FieldMetadata{

	private Class<?> detailType;
	private int columnOffset;
	private boolean readOnly;
	
	public DetailCollectionFieldMetadata(Field field, Class<?> detailType, boolean readOnly, int columnOffset) {
		super(field);
		super.setType(detailType);
		
		this.detailType = detailType;
		this.columnOffset = columnOffset;
		this.readOnly = readOnly;
	}

	public Class<?> getType(){
		return detailType;
	}
	
	public int getColumnOffset(){
		return columnOffset;
	}

	public boolean isReadOnly(){
		return readOnly;
	}
}
