package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

public interface IReader {

	@SuppressWarnings("rawtypes")
	public Object read(ArrayIterator iterator, int position, ReadContext context);
}
