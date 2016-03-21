package gugit.om.metadata;

import java.lang.reflect.Field;

public class ColumnFieldMetadata extends FieldMetadata{

	// a label used when persisting
	private String columnName;
		
	// field position relative to ID field, which is at position zero
	private int columnOffset;
	
	public ColumnFieldMetadata(Field field, String columnName, int columnOffset) {
		super(field);
		
		this.columnName = columnName;
		this.columnOffset = columnOffset;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public int getColumnOffset() {
		return columnOffset;
	}


}
