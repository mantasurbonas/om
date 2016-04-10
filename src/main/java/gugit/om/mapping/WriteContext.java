package gugit.om.mapping;

/***
 * aggregates any parameters needed for IWriter::write() 
 *  
 * @author urbonman
 *
 */
public class WriteContext {

	protected ISerializerFactory serializers;

	public WriteContext(ISerializerFactory serializers){
		this.serializers = serializers;
	}
	
	public <E> IWriter<E> getWriterFor(Class<E> entityClass){
		return serializers.getSerializerFor(entityClass);
	}

	public <E> IMerger<E> getMergerFor(Class<E> entityClass){
		return serializers.getSerializerFor(entityClass);
	}
}
