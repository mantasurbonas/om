package gugit.om.mapping;

import gugit.om.utils.IDataIterator;

/***
 * Reads entity E from underlying data 
 * 
 * Stateless (and thread-safe) - provided the params are thread-specific, of course
 * 
 * The IReader will return the same entity instance (cached in the ReadContext) 
 * 		if the immediately subsequent invocation to read() will have similar data in the iterator AND the same ReadContext parameter. 
 * 
 * @author urbonman
 */
public interface IReader<E> {
	
	E read(IDataIterator<?> iterator, int startPosition, ReadContext context);
	
}
