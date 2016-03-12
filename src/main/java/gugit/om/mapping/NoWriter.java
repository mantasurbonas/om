package gugit.om.mapping;

import gugit.om.WriteBatch;

import java.util.Map;

public class NoWriter implements IWriter{

	private static NoWriter instance = new NoWriter();
	
	private NoWriter(){}
	
	public static NoWriter getInstance(){
		return instance;
	}
	
	@Override
	public void write(Object value, Map<String, Object> props, WriteBatch writeBatch) {
		;
	}

}
