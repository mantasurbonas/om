package gugit.om.mapping;

import java.util.HashMap;
import java.util.Map;

public class EntityWriterRegistry {

	public Map<Class<?>, EntityWriter<?>> registry = new HashMap<Class<?>, EntityWriter<?>>();
	
	@SuppressWarnings({ "unchecked" })
	public <T> EntityWriter<T> getEntityWriterFor(Class<T> entityClass){
		EntityWriter<T> mapper = (EntityWriter<T>) registry.get(entityClass);
		if (mapper == null){
			EntityMetadata <T> metadata = EntityMetadataFactory.createMetadata(entityClass);
			mapper = registerEntityWriter(entityClass, metadata);
		}
		return mapper;
	}

	public <T> EntityWriter<T> registerEntityWriter(Class<T> entityClass, EntityMetadata<T> metadata){
		EntityWriter<T> result  = new EntityWriter<T>(metadata, this);
		registry.put(entityClass, result);
		return result;
	}
	
	public void resetAll(){
		for (EntityWriter<?> writer: registry.values())
			writer.reset();
	}
}

