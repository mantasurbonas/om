package gugit.om.metadata;

import java.lang.reflect.Field;

public class ColumnFieldMetadata extends FieldMetadata{

	// a label used when persisting
	private String columnName;
		
	// field position relative to ID field, which is at position zero
	private int columnOffset;
	
	private boolean readOnly;
	
	public ColumnFieldMetadata(Field field, String columnName, boolean readOnly, int columnOffset) {
		super(field);
		
		this.columnName = columnName;
		this.columnOffset = columnOffset;
		this.readOnly = readOnly;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public int getColumnOffset() {
		return columnOffset;
	}

	public boolean isReadOnly(){
		return readOnly;
	}
}
