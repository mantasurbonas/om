package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

public class PrimitiveReader<E> implements IReader{

	@SuppressWarnings("unchecked")
	@Override
	public E read(ArrayIterator<Object> iterator) {
		return (E)iterator.getNext();
	}

	
}
