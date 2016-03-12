package gugit.om.mapping;

import gugit.om.WriteBatch;

import java.util.Map;

public class PrimitiveWriter <E>implements IWriter{

	private String columnName;
	
	public PrimitiveWriter(final String columnName){
		this.columnName=columnName;
	}
	
	@Override
	public void write(Object value, Map<String, Object> props, WriteBatch writeBatch) {
		if (value == null)
			value = NullWriteValue.getInstance();
		props.put(columnName, value);
	}

}
