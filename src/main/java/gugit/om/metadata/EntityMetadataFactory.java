package gugit.om.metadata;

import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.mapping.Binding;
import gugit.om.mapping.EntityCollectionWriter;
import gugit.om.mapping.EntityReader;
import gugit.om.mapping.EntityWriter;
import gugit.om.mapping.IReader;
import gugit.om.mapping.IWriter;
import gugit.om.mapping.NoBinding;
import gugit.om.mapping.NoReader;
import gugit.om.mapping.NoWriter;
import gugit.om.mapping.PrimitiveReader;
import gugit.om.mapping.PrimitiveWriter;
import gugit.om.utils.ArrayIterator;

import java.lang.reflect.Field;

import com.esotericsoftware.reflectasm.MethodAccess;


/***
 * creates a metadata about any object by looking at its annotations.
 * 
 * @author urbonman
 *
 */
public class EntityMetadataFactory{
	
	public <T> EntityMetadata<T> createMetadata(Class<T> entityClass){			
		ArrayIterator<Field> fields = new ArrayIterator<Field>(entityClass.getFields());

		String entityName = resolveEntityName(entityClass);
		FieldMetadata id = createIDMetadata( entityClass, findID(fields) );		
		EntityMetadata<T> entityMetadata = new EntityMetadata<T>(entityClass, entityName, id);	
			
		addFieldMetadata(entityMetadata, fields);
	
		return entityMetadata;
	}

	private <T> FieldMetadata createIDMetadata(Class<T> entityClass, Field idField) {
		String fieldName = idField.getName();
		String columnName = resolveIdColumnName(idField);
		
		Binding binding = new Binding( MethodAccess.get(entityClass), fieldName, false );
		
		IWriter writer = new PrimitiveWriter<>(columnName);
		IReader reader = new PrimitiveReader<>();
		
		return new FieldMetadata(fieldName, binding, writer, reader);
	}

	private <T> void addFieldMetadata(EntityMetadata<T> entityMetadata, ArrayIterator<Field> fields) {
		MethodAccess access = MethodAccess.get(entityMetadata.getEntityClass());	
		
		while (!fields.isFinished()){
			Field field = fields.getNext();
			AnnotationHelper annotations = new AnnotationHelper(field.getAnnotations());
			
			if (annotations.isID())
				throw new RuntimeException("ID field must be one and only one");
			
			if (annotations.isIgnored())
				entityMetadata.addFieldMetadata(IgnoreFieldMetadata.getInstance());
			else
			if (annotations.isTransient())
				entityMetadata.addFieldMetadata(TransientFieldMetadata.getInstance());
			else
			if (annotations.isColumn())
				entityMetadata.addFieldMetadata( createColumnMetadata(annotations, field, access) );
			else
			if (annotations.isDetailEntity())
				entityMetadata.addFieldMetadata( createDetailMetadata( annotations, field, access ) );
			else
			if (annotations.isDetailEntities())
				entityMetadata.addFieldMetadata( createDetailCollectionMetadata ( annotations, field, access ) );
			else
			if (annotations.isMasterEntity()){
				entityMetadata.addFieldMetadata( createMasterMetadata( annotations, field, access ) );
				
				/*Binding fieldAccessor = new Binding(access, field);
				
				Class<?> masterEntityType = field.getType();
				MethodAccess masterAccess = MethodAccess.get(masterEntityType);
				String masterPropertyName = annotations.getMasterPropertyName();
				Binding masterPropertyAccessor = new Binding(masterAccess, masterPropertyName);
				
				String myColumnName = annotations.getMasterMyColumnName();
				entityMetadata.addFieldMapping(new OLD__MasterEntityFieldMapping(myColumnName, fieldAccessor, masterPropertyAccessor));
				*/
			}/*else
			if (annotations.isManyToMany()){
				Class<?> fieldEntityType = annotations.getManyToManyFieldType();
				EntityMetadata<?> fieldMetadata = createMetadata(fieldEntityType);
				entityMetadata.addFieldMapping(new OLD__ManyToManyFieldMapping(field, fieldMetadata));
			}*/
			else
				throw new RuntimeException("could not recognize annotation");
		}
	}
	
	private FieldMetadata createMasterMetadata(AnnotationHelper annotations, Field field, MethodAccess access) {		
		return new FieldMetadata("NOT IMPLEMENTED",  // TODO
								NoBinding.getInstance(), 
								NoWriter.getInstance(), 
								NoReader.getInstance());
	}

	private FieldMetadata createDetailCollectionMetadata(AnnotationHelper annotations, Field field, MethodAccess access) {
		String name = field.getName();
		Class<?> detailClass = annotations.getDetailEntitiesType();
		EntityMetadata<?> detailEntityMetadata = createMetadata(detailClass);
		
		return new FieldMetadata(name, 
								new Binding(access, name, true), 
								new EntityCollectionWriter<>(detailEntityMetadata), 
								new EntityReader<>(detailEntityMetadata));
	}

	private FieldMetadata createDetailMetadata(AnnotationHelper annotations, Field field, MethodAccess access) {
		String name = field.getName();
		Class<?> fieldType = field.getType();
		EntityMetadata<?> detailEntityMetadata = createMetadata(fieldType);
		return new FieldMetadata(name ,
								new Binding(access, name, false),
								new EntityWriter<>(detailEntityMetadata),
								new EntityReader<>(detailEntityMetadata));
	}


	private FieldMetadata createColumnMetadata(AnnotationHelper annotations, Field field, MethodAccess access) {
		String name = field.getName();
		return new FieldMetadata(name, 
								new Binding(access, name, false), 
								new PrimitiveWriter<>(annotations.getColumnName()), 
								new PrimitiveReader<>());
	}

	private static Field findID(ArrayIterator<Field> fields){
		while (! fields.isFinished()){
			Field field = fields.getNext(); 
			
			AnnotationHelper annotation = new AnnotationHelper(field.getAnnotations());
			
			if (annotation.isTransient())
				continue;
			
			if (annotation.isID())
				return field;
			
			throw new RuntimeException("ID field must be a first non-transient field in the entity");
		}
		throw new RuntimeException("ID field not found in the entity");
	}
		
	private static String resolveIdColumnName(Field field) {
		ID[] annotations = field.getAnnotationsByType(ID.class);
		return annotations[0].name();
	}

	private static String resolveEntityName(Class<?> clazz) {
		Entity annotation = clazz.getAnnotation(Entity.class);
		
		if (annotation == null || annotation.name() == null || annotation.name().trim().isEmpty())
			return clazz.getSimpleName();
		
		return annotation.name().trim();
	}

	
}
