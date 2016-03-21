package gugit.om.mapping;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataService;

import java.util.HashMap;
import java.util.Map;

/***
 * creates a new serializer or returns a cached one if exists.
 * 
 * thread-safe.
 * 
 * @author urbonman
 *
 */
public class SerializerRegistry implements ISerializerRegistry{

	private Map<Class<?>, ISerializer<?>> serializersCache = new HashMap<Class<?>, ISerializer<?>>();
	
	private SerializerCompiler serializerCompiler = new SerializerCompiler();

	private EntityMetadataService entityMetadataService;

	public void setEntityMetadataService(EntityMetadataService entityMetadataService){
		this.entityMetadataService = entityMetadataService;
	}
	
	@SuppressWarnings("unchecked")
	public <T> ISerializer<T> getSerializerFor(Class<T> entityClass){
		
		if (serializersCache.containsKey(entityClass))
			return (ISerializer<T>) serializersCache.get(entityClass);
		
		synchronized(this){
			if (serializersCache.containsKey(entityClass))
				return (ISerializer<T>) serializersCache.get(entityClass);
			
			try {
				ISerializer<T> serializer = makeSerializer(entityClass);
				serializersCache.put(entityClass, serializer);			
				return serializer;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private synchronized <T> ISerializer<T> makeSerializer(Class<T> entityClass) throws Exception {
		Class<ISerializer<T>> serializerClass;
		if (serializerCompiler.doesSerializerClassExist(entityClass)) {
			serializerClass = serializerCompiler.getExistingSerializerClass(entityClass);
		}else{		
			EntityMetadata<T> metadata = entityMetadataService.getMetadataFor(entityClass);
			serializerClass = serializerCompiler.makeSerializerClass(metadata);
		}
		
		return serializerClass.newInstance();
	}

}
