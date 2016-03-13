package gugit.om.mapping;

import gugit.om.WritePad;

public interface IWriter {

	void write(Object value, WritePad<?> writePad);
	
}
