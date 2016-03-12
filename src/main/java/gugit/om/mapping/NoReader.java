package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

public class NoReader implements IReader{

	private static NoReader instance = new NoReader();
	
	private NoReader(){}
	
	public static NoReader getInstance(){
		return instance;
	}
	
	@Override
	public Object read(ArrayIterator<Object> iterator) {
		return null;
	}

}
