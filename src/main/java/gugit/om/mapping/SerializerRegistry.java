package gugit.om.mapping;

import gugit.om.metadata.IEntityMetadataFactory;

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
public class SerializerRegistry implements ISerializerFactory{

	private Map<Class<?>, ISerializer<?>> serializersCache = new HashMap<Class<?>, ISerializer<?>>();
	
	private SerializerCompiler serializerCompiler;

	public SerializerRegistry(IEntityMetadataFactory metadataFactory){
		serializerCompiler = new SerializerCompiler(metadataFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
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
			serializerClass = serializerCompiler.makeSerializerClass(entityClass);
		}
		
		return serializerClass.newInstance();
	}

}
