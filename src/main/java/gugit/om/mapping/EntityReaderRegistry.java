package gugit.om.mapping;

import java.util.HashMap;
import java.util.Map;

public class EntityReaderRegistry {

	public Map<Class<?>, EntityReader<?>> registry = new HashMap<Class<?>, EntityReader<?>>();
	
	@SuppressWarnings({ "unchecked" })
	public <T> EntityReader<T> getEntityReaderFor(Class<T> entityClass){
		EntityReader<T> mapper = (EntityReader<T>) registry.get(entityClass);
		if (mapper == null){
			EntityMetadata <T> metadata = EntityMetadataFactory.createMetadata(entityClass);
			mapper = registerEntityReader(entityClass, metadata);
		}
		return mapper;
	}

	public <T> EntityReader<T> registerEntityReader(Class<T> entityClass, EntityMetadata<T> metadata){
		EntityReader<T> result  = new EntityReader<T>(metadata, this);
		registry.put(entityClass, result);
		return result;
	}
	
	public void resetAll(){
		for (EntityReader<?> reader: registry.values())
			reader.reset();
	}
}
