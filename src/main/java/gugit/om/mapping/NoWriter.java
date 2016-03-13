package gugit.om.mapping;

import gugit.om.WritePad;

public class NoWriter implements IWriter{

	private static NoWriter instance = new NoWriter();
	
	private NoWriter(){}
	
	public static NoWriter getInstance(){
		return instance;
	}
	
	@Override
	public void write(Object value, WritePad<?> writePad) {
		;
	}

}
