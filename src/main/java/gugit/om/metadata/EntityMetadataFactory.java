package gugit.om.metadata;

import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.mapping.AbstractReader;
import gugit.om.mapping.ReaderCompiler;
import gugit.om.utils.ArrayIterator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;


/***
 * creates a metadata about any object by looking at its annotations.
 * 
 * @author urbonman
 *
 */
public class EntityMetadataFactory{
	
	private Map<Class<?>, EntityMetadata<?>> metadataCache = new HashMap<Class<?>, EntityMetadata<?>>();
	
	private Map<Class<?>, AbstractReader> readersCache = new HashMap<Class<?>, AbstractReader>();
	
	private ReaderCompiler readerCompiler = new ReaderCompiler();
	
	
	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getMetadataFor(Class<T> entityClass){
		
		if (metadataCache.containsKey(entityClass))
			return (EntityMetadata<T>) metadataCache.get(entityClass);
		
		ArrayIterator<Field> fields = new ArrayIterator<Field>(entityClass.getFields());

		EntityMetadata<T> entityMetadata = createMetadata(entityClass, fields);	
		metadataCache.put(entityClass, entityMetadata);
		
		try{
			addFieldsToMetadata(entityMetadata, fields);
		}catch(Exception e){
			metadataCache.remove(entityClass);
			throw e;
		}
	
		this.getEntityReader(entityMetadata);
		
		return entityMetadata;
	}

	private <T> EntityMetadata<T> createMetadata(Class<T> entityClass, ArrayIterator<Field> fields) {
		Field idField = findID(fields);
		FieldMetadata idMetadata = createIDMetadata( entityClass, idField, fields.getPosition() );		
		fields.next();

		return new EntityMetadata<T>(entityClass, 
									determineEntityName(entityClass), 
									idMetadata);
	}

	private <T> void addFieldsToMetadata(EntityMetadata<T> entityMetadata, ArrayIterator<Field> fields) {
		
		Integer columnOffset = fields.getPosition();
		
		while (!fields.isFinished()){
			Field field = fields.getNext();
			AnnotationHelper annotations = new AnnotationHelper(field.getAnnotations());
			
			if (annotations.isID())
				throw new RuntimeException("ID field must be one and only one");
			
			if (annotations.isIgnored()){
				entityMetadata.addPrimitiveField(new IgnoreFieldMetadata(columnOffset));
				columnOffset += 1;
			}
			else
			if (annotations.isTransient())
				; // just skipping transient fields
			else
			if (annotations.isColumn()){
				entityMetadata.addPrimitiveField(new FieldMetadata(field.getName(), annotations.getColumnName(), columnOffset));
				columnOffset += 1;
			}else
			if (annotations.isMasterEntity()){
				entityMetadata.addMasterRefField( new FieldMetadata(field.getName(), annotations.getMasterMyColumnName(), columnOffset));
				columnOffset += 1;
			}else
			if (annotations.isDetailEntity()){
				FieldMetadata fieldMeta = new FieldMetadata(field.getName(), "-=nevermind=-", columnOffset);
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
				entityMetadata.addPojoCollectionField( new DetailCollectionFieldMetadata(field.getName(), detailClass, columnOffset));
				
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
	}

	public AbstractReader getEntityReader(EntityMetadata<?> entityMetadata){
		Class<?> entityClass = entityMetadata.getEntityClass();
		
		if (readersCache.containsKey(entityClass))
			return readersCache.get(entityClass);
		
		AbstractReader reader;
		try {
			reader = makeReader(entityMetadata);
			readersCache.put(entityClass, reader);			
			return reader;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private AbstractReader makeReader(EntityMetadata<?> entityMetadata) throws Exception {
		Class<AbstractReader> readerClass;
		Class<?> entityClass = entityMetadata.getEntityClass();
		if (readerCompiler.doesReaderClassExist(entityClass)) {
			readerClass = readerCompiler.getExistingReaderClass(entityClass);
		}else{		
			readerClass = readerCompiler.makeReaderClass(entityMetadata);
		}
		
		AbstractReader reader = readerClass.newInstance();
		reader.setReaders(readersCache);
		
		return reader;
	}

	private <T> FieldMetadata createIDMetadata(Class<T> entityClass, Field idField, int position) {
		return new FieldMetadata(idField.getName(), 
								resolveIdColumnName(idField), 
								position);
	}

	
	private static Field findID(ArrayIterator<Field> fields){
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
