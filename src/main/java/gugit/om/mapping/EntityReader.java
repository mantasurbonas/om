package gugit.om.mapping;

import java.util.HashMap;
import java.util.Map;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;
import gugit.om.utils.ArrayIterator;

/***
 * knows how to read a POJO from a stream of objects.
 *  (uses EntityMetadata for this task).
 *  
 * @author urbonman
 */
public class EntityReader <E> implements IReader{

	private EntityMetadata<E> entityMetadata;
	
	private static class State<E>{
		public Object cachedId;
		public E cachedEntity;
		public void reset(){
			cachedId = null;
			cachedEntity = null;
		}
	};
	
	private Map<Integer, State<E>> states = new HashMap<Integer, State<E>>();

	public EntityReader(EntityMetadata<E> metadata){
		this.entityMetadata = metadata;
	}
	
	public void reset(){
		for(State<E> state: states.values())
			state.reset();
	}
	
	private State<E> currentState(ArrayIterator<Object> iterator){
		State<E> state = states.get(iterator.getPosition());
		if (state == null){
			state = new State<E>();
			states.put(iterator.getPosition(), state);
		}
		return state;
	}

	public E read(ArrayIterator<Object> iterator){
		ReadContext context = new ReadContext();
		return read(iterator, context);
	}
	
	public E read(ArrayIterator<Object> iterator, ReadContext context){
		if (iterator.isFinished())
			return null;
		
		Object id = iterator.peek();
		
		if (id == null)
			return skipReading(iterator, context);
		else
		if (id.equals( currentState(iterator).cachedId ) )
			return (E)readCachedEntity(iterator, context);
		else
			return (E)readNewEntity(id, iterator, context);
	}
	
	private E readCachedEntity(ArrayIterator<Object> iterator, ReadContext context){
		E cachedEntity = currentState(iterator).cachedEntity;
		Object id = currentState(iterator).cachedId;
		
		context.entityIsBeingRead(cachedEntity, id);
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			
			Object value = fieldMetadata.getReader().read(iterator, context);
			
			if (fieldMetadata.getBinding().isCollection())
				fieldMetadata.getBinding().assignValueTo(cachedEntity, value);
		}
		
		context.entityReadingFinished();
		
		return cachedEntity;
	}
	
	private E readNewEntity(Object newId, ArrayIterator<Object> iterator, ReadContext context){
		E entity = entityMetadata.createEntity(newId);
		
		currentState(iterator).cachedEntity = entity;
		currentState(iterator).cachedId = newId;
		
		context.entityIsBeingRead(entity, newId);
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			Object value = fieldMetadata.getReader().read(iterator, context);
			fieldMetadata.getBinding().assignValueTo(entity, value);
		}
		
		context.entityReadingFinished();
		
		return entity;
	}
	
	private E skipReading(ArrayIterator<Object> iterator, ReadContext context){
		currentState(iterator).cachedEntity = null;
		currentState(iterator).cachedId = null;
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList())
			fieldMetadata.getReader().read(iterator, context);
		
		return null;
	}
	
}
