package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

public class SkipReader implements IReader{

	private static SkipReader instance = new SkipReader();
	
	private SkipReader(){}
	
	public static SkipReader getInstance(){
		return instance;
	}
	
	@Override
	public Object read(ArrayIterator<Object> iterator) {
		return iterator.getNext();
	}

}
