package gugit.om.mapping;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;
import gugit.om.utils.ArrayIterator;


public class EntityReader <E> implements IReader{

	private EntityMetadata<E> entityMetadata;
	
	private Object cachedId;
	private E cachedEntity;

	public EntityReader(EntityMetadata<E> metadata){
		this.entityMetadata = metadata;
	}
	
	public void reset(){
		cachedId = null;
		cachedEntity = null;
	}
	
	public E read(ArrayIterator<Object> iterator){
		Object id = iterator.peek();
		
		if (id == null)
			return skipReading(iterator);
		else
		if (id.equals(cachedId))
			return (E)readCachedEntity(iterator);
		else
			return (E)readNewEntity(id, iterator);
	}
	
	private E readCachedEntity(ArrayIterator<Object> iterator){
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			
			Object value = fieldMetadata.getReader().read(iterator);
			
			if (fieldMetadata.getBinding().isCollection())
				fieldMetadata.getBinding().assignValueTo(cachedEntity, value);
		}
		
		return cachedEntity;
	}
	
	private E readNewEntity(Object newId, ArrayIterator<Object> iterator){
		E entity = entityMetadata.createEntity(newId);
		cachedEntity = entity;
		cachedId = newId;
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			Object value = fieldMetadata.getReader().read(iterator);
			fieldMetadata.getBinding().assignValueTo(entity, value);
		}
		
		return entity;
	}
	
	private E skipReading(ArrayIterator<Object> iterator){
		cachedEntity = null;
		cachedId = null;
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList())
			fieldMetadata.getReader().read(iterator);
		
		return null;
	}
	
}
