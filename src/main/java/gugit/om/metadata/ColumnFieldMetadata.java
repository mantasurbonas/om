package gugit.om.metadata;

import gugit.om.mapping.Binding;
import gugit.om.mapping.PrimitiveReader;
import gugit.om.mapping.PrimitiveWriter;

import com.esotericsoftware.reflectasm.MethodAccess;

public class ColumnFieldMetadata extends FieldMetadata{

	private String columnName;

	public ColumnFieldMetadata(final String name, final String columnName, MethodAccess access) {
		super(name, 
			new Binding(access, name, false), 
			new PrimitiveWriter<>(columnName), 
			new PrimitiveReader<>());
		
		this.columnName = columnName;
	}
	
	@Override
	public boolean isColumn(){
		return true;
	}
	
	public String getColumnName(){
		return columnName;
	}

}
