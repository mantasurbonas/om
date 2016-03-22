package gugit.om.mapping;

public interface ISerializerFactory {
	
	<T> ISerializer<T> getSerializerFor(Class<T> entityClass);

}
