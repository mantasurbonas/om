package gugit.om.mapping;

import gugit.om.utils.IDataIterator;

/***
 * merges an already read entity with more data from a specified property on
 * 
 * stateless and thus trhead safe - provided params are thread-specific 
 * @author urbonman
 *
 */
public interface IMerger<E> {

	Object getID(E entity);
	
	int getPropertyIndex(String property);
	
	void leftJoin(E entity, int propIndex, IDataIterator<?> array, int startPosition, ReadContext readContext);

}
