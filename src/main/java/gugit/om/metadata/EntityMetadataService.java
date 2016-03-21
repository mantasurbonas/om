package gugit.om.metadata;

import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.mapping.ISerializerRegistry;
import gugit.om.utils.ArrayIterator;
import gugit.om.utils.IDataIterator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/***
 * creates a metadata about any object by looking at its annotations.
 * 
 * @author urbonman
 *
 */
public class EntityMetadataService {
	
	private Map<Class<?>, EntityMetadata<?>> metadataCache = new HashMap<Class<?>, EntityMetadata<?>>();
	private ISerializerRegistry serializerRegistry;
		
	public void setSerializerRegistry(ISerializerRegistry serializers){
		this.serializerRegistry = serializers;
	}
	
	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getMetadataFor(Class<T> entityClass){
		
		if (metadataCache.containsKey(entityClass))
			return (EntityMetadata<T>) metadataCache.get(entityClass);
		
		IDataIterator<Field> fields = new ArrayIterator<Field>(entityClass.getFields());

		EntityMetadata<T> entityMetadata = createMetadataInstance(entityClass, fields);	
		metadataCache.put(entityClass, entityMetadata);
		
		try{
			addFieldsToMetadata(entityMetadata, fields);
		}catch(Exception e){
			metadataCache.remove(entityClass);
			throw e;
		}
	
		serializerRegistry.getSerializerFor(entityClass);
		
		return entityMetadata;
	}

	private <T> EntityMetadata<T> createMetadataInstance(Class<T> entityClass, IDataIterator<Field> fields) {
		Field idField = findID(fields);
		ColumnFieldMetadata idMetadata = createIDMetadata(idField, fields.getPosition());		
		fields.next();

		return new EntityMetadata<T>(entityClass, 
									determineEntityName(entityClass), 
									idMetadata);
	}

	private <T> void addFieldsToMetadata(EntityMetadata<T> entityMetadata, IDataIterator<Field> fields) {
		
		Set<Class<?>> relatedTypes = new HashSet<Class<?>>();
		
		Integer columnOffset = fields.getPosition();
		
		while (!fields.isFinished()){
			Field field = fields.getNext();
			AnnotationHelper annotations = new AnnotationHelper(field.getAnnotations());
			
			if (annotations.isID())
				throw new RuntimeException("ID field must be one and only one");
			
			if (annotations.isIgnored()){
				entityMetadata.addIgnoreField(new IgnoreFieldMetadata(field, columnOffset));
				columnOffset += 1;
			}
			else
			if (annotations.isTransient())
				; // just skipping transient fields
			else
			if (annotations.isColumn()){
				entityMetadata.addPrimitiveField(new ColumnFieldMetadata(field, annotations.getColumnName(), columnOffset));
				columnOffset += 1;
			}else
			if (annotations.isMasterEntity()){
				entityMetadata.addMasterRefField( new MasterRefFieldMetadata(field, 
																			annotations.getMasterPropertyName(), 
																			annotations.getMasterMyColumnName(),
																			columnOffset));
				columnOffset += 1;
				relatedTypes.add(field.getType());
			}else
			if (annotations.isDetailEntity()){
				PojoFieldMetadata fieldMeta = new PojoFieldMetadata(field, columnOffset);
				entityMetadata.addPojoField( fieldMeta );
				
				EntityMetadata<?> pojoMetadata = getMetadataFor(field.getType());
				Integer width = pojoMetadata.getWidth();
				if (width == null)
					columnOffset = null;
				else
					columnOffset += width;
			}
			else
			if (annotations.isDetailEntities()){
				Class<?> detailClass = annotations.getDetailEntitiesType();
				entityMetadata.addPojoCollectionField( new DetailCollectionFieldMetadata(field, detailClass, columnOffset));
				
				EntityMetadata<?> pojoMetadata = getMetadataFor(detailClass);
				
				Integer width = pojoMetadata.getWidth();
				if (width == null)
					columnOffset = null;
				else
					columnOffset += width;
			}/*else
			if (annotations.isManyToMany()){
				Class<?> fieldEntityType = annotations.getManyToManyFieldType();
				EntityMetadata<?> fieldMetadata = createMetadata(fieldEntityType);
				entityMetadata.addFieldMapping(new OLD__ManyToManyFieldMapping(field, fieldMetadata));
			}*/
			else
				throw new RuntimeException("could not recognize annotation");
		}
		
		entityMetadata.setWidth(columnOffset);
		
		for (Class<?> type: relatedTypes)
			getMetadataFor(type);
	}

	
	private <T> ColumnFieldMetadata createIDMetadata(Field idField, int position) {
		return new ColumnFieldMetadata(idField, 
										resolveIdColumnName(idField), 
										position);
	}

	
	private static Field findID(IDataIterator<Field> fields){
		while (! fields.isFinished()){
			Field field = fields.peek(); 
			
			AnnotationHelper annotation = new AnnotationHelper(field.getAnnotations());
			
			if (annotation.isID())
				return field;
						
			if (!annotation.isTransient())	
				throw new RuntimeException("ID field must be a first non-transient field in the entity");
			
			fields.next();
		}
		throw new RuntimeException("ID field not found in the entity");
	}
		
	private static String resolveIdColumnName(Field field) {
		ID[] annotations = field.getAnnotationsByType(ID.class);
		return annotations[0].name();
	}

	private static String determineEntityName(Class<?> clazz) {
		Entity annotation = clazz.getAnnotation(Entity.class);
		
		if (annotation == null || annotation.name() == null || annotation.name().trim().isEmpty())
			return clazz.getSimpleName();
		
		return annotation.name().trim();
	}

	
}