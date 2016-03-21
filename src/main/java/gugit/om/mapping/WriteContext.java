package gugit.om.mapping;

/***
 * aggregates any parameters needed for IWriter::write() 
 *  
 * @author urbonman
 *
 */
public class WriteContext {

	protected ISerializerRegistry serializerRegistry;

	public WriteContext(ISerializerRegistry registry){
		this.serializerRegistry = registry;
	}
	
	public <E> IWriter<E> getWriterFor(Class<E> entityClass){
		return serializerRegistry.getSerializerFor(entityClass);
	}
}
