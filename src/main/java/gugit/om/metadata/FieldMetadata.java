package gugit.om.metadata;


public class FieldMetadata {

	// a name of a POJO field - just as specified by the developer
	private String name;

	// a label used when persisting
	private String columnName;
		
	// field position relative to ID field, which is at position zero
	private int columnOffset;
	
	public FieldMetadata(final String name, final String columnName, int columnOffset){
		this.name = name;
		this.columnName = columnName;
		this.columnOffset = columnOffset;
	}
		
	public String getName(){
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getColumnOffset() {
		return columnOffset;
	}
}
