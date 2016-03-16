package gugit.om.metadata;

import gugit.om.mapping.IReader;
import gugit.om.mapping.ReadContext;
import gugit.om.utils.ArrayIterator;


public class MasterDependencyReader implements IReader {

	private Class<?> masterEntityType;

	public MasterDependencyReader(Class<?> masterEntityType){
		this.masterEntityType = masterEntityType;
	}
	
	/***
	 * reads a parent-id from the object stream.
	 * then looks around if we've read an object with such an id from a stream already - assuming it's a needed parent entity then.  
	 */
	@Override
	public Object read(ArrayIterator<Object> iterator, ReadContext context) {
		Object id = iterator.getNext();
		Object master = context.findEntity(masterEntityType, id);
		
		return master;
	}

}
