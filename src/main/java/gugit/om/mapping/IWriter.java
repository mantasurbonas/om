package gugit.om.mapping;

import gugit.om.WriteBatch;

import java.util.Map;

public interface IWriter {

	void write(Object value, Map<String, Object> props, WriteBatch writeBatch);
	
}
