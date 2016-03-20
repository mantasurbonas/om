package gugit.om.mapping;

public interface ISerializerRegistry {

	<E> ISerializer<E> getSerializerFor(Class<E> entityClass);
	
}
