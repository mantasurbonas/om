package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

public interface IReader {

	public Object read(ArrayIterator<Object> iterator, ReadContext context);
}
