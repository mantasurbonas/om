package gugit.services;

import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ISerializerFactory;
import gugit.om.mapping.SerializerRegistry;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataRegistry;
import gugit.om.metadata.IEntityMetadataFactory;
import gugit.om.metadata.IEntityNameProvider;

public class EntityServiceFacade implements IEntityMetadataFactory, 
									  ISerializerFactory,
									  IEntityNameProvider{

	protected EntityMetadataRegistry metadataRegistry;
	protected SerializerRegistry serializerRegistry;
	
	public EntityServiceFacade(){
		metadataRegistry = new EntityMetadataRegistry(){
			protected void onMetadataCreated(EntityMetadata<?> em){
				// ensuring serializers are created for each entity metadata detected
				serializerRegistry.getSerializerFor(em.getEntityClass());
			}
		};
		serializerRegistry = new SerializerRegistry(metadataRegistry);
	}
	
	@Override
	public <E> ISerializer<E> getSerializerFor(Class<E> entityClass) {
		return serializerRegistry.getSerializerFor(entityClass);
	}

	@Override
	public <E> EntityMetadata<E> getMetadataFor(Class<E> entityClass) {
		return metadataRegistry.getMetadataFor(entityClass);
	}

	@Override
	public String getEntityName(Class<?> entityClass) {
		return getMetadataFor(entityClass).getEntityName();
	}

}
