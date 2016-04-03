package gugit.services;

import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ISerializerFactory;
import gugit.om.mapping.SerializerRegistry;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataRegistry;
import gugit.om.metadata.IEntityMetadataFactory;
import gugit.om.metadata.IEntityNameProvider;
import gugit.om.wrapping.EntityFactoryImpl;
import gugit.om.wrapping.IEntityFactory;

public class EntityServiceFacade implements IEntityFactory,
									  		IEntityMetadataFactory, 
									  		ISerializerFactory,
									  		IEntityNameProvider{

	protected EntityMetadataRegistry metadataRegistry;
	protected SerializerRegistry serializerRegistry;
	protected EntityFactoryImpl entityFactory;
	
	public EntityServiceFacade(){
		entityFactory = new EntityFactoryImpl();
		
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

	@Override
	public <E> E create(Class<E> entityClass){
		return entityFactory.create(entityClass);
	}

}
