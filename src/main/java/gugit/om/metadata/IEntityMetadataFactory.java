package gugit.om.metadata;

public interface IEntityMetadataFactory {

	<T> EntityMetadata<T> getMetadataFor(Class<T> entityClass);
}
