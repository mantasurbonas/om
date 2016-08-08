package gugit.om.mapping;

import java.util.HashMap;
import java.util.Map;

import gugit.om.utils.FastStack;
import gugit.om.wrapping.IEntityFactory;


/***
 * Contains the state of IReader between invocations to IReader::read().
 * 
 * @author urbonman
 *
 */
public class ReadContext {
	
	// needed to read related entities
	private ISerializerFactory serializers;
	
	// needed to create new (wrapped) entity instances
	private IEntityFactory entityFactory;
	
	// read state
	private FastStack currentlyReadEntities = new FastStack(); 
	private Map<Integer, Map<Object, Object>> previousReads = new HashMap<>();
	
	
	public ReadContext(ISerializerFactory serializers, IEntityFactory factory){
		this.serializers = serializers;
		this.entityFactory = factory;
	}
	
	public void entityIsBeingRead(Object entity, Object id){
		currentlyReadEntities.push(entity, id);
	}
	
	public void entityReadingFinished(){
		currentlyReadEntities.pop();
	}
	
	public Object findMasterEntity(Class<?> type, Object id){
		return currentlyReadEntities.find(type, id);
	}
	
	public Object getCachedRead(int position, Object id){
		Map<Object, Object> dictionary = previousReads.get(position);
		if (dictionary == null)
			return null;
		return dictionary.get(id);
	}
	
	public void cacheRead(int position, Object id, Object obj){
		Map <Object, Object> dictionary = previousReads.get(position);
		if (dictionary == null){
			dictionary = new HashMap<Object, Object>();
			previousReads.put(position, dictionary);
		}
		dictionary.put(id,  obj);
	}
	
	public void resetRead(int position){
		previousReads.put(position, null);
	}
	
	public void clear(){
		currentlyReadEntities.clear();
		previousReads.clear();
	}
	
	public <E> IReader<E> getReaderFor(Class<E> entityClass){
		return serializers.getSerializerFor(entityClass);
	}
	
	public <E> E createEntity(Class<E> entityClass){
		return entityFactory.create(entityClass);
	}
}
