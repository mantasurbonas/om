package gugit.om.mapping;

import gugit.om.WritePad;

public class PrimitiveWriter <E>implements IWriter{

	private String columnName;
	
	public PrimitiveWriter(final String columnName){
		this.columnName=columnName;
	}
	
	@Override
	public void write(Object value, WritePad<?> where) {
		where.add(columnName, value);
	}

}
