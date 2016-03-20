package gugit.om.mapping;

public class WriteContext {

	private ISerializerRegistry serializerRegistry;

	public WriteContext(ISerializerRegistry registry){
		this.serializerRegistry = registry;
	}
	
	public <E> IWriter<E> getWriterFor(Class<E> entityClass){
		return serializerRegistry.getSerializerFor(entityClass);
	}
}
