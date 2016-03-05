package gugit.om.mapping;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;
import gugit.om.annotations.Ignore;
import gugit.om.annotations.OneToMany;
import gugit.om.annotations.OneToOne;
import gugit.om.annotations.Transient;
import gugit.om.utils.ArrayIterator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class EntityMapperFactory{

	public static <T> EntityMapper<T> createMapper(Class<T> entityClass){		
		ArrayIterator<Field> fields = new ArrayIterator<Field>(entityClass.getFields());
		
		EntityMapper<T> entityMapper = new EntityMapper<T>(entityClass);
			mapIDField(entityMapper, fields);
			mapFields(entityMapper, fields);

		return entityMapper;
	}

	private static <T> void mapIDField(EntityMapper<T> entityMapper, ArrayIterator<Field> fields) {
		Field idField = findID(fields);
		entityMapper.setIDField(idField, getIdColumnName(idField.getAnnotations()));
	}

	private static <T> void mapFields(EntityMapper<T> entityMapper, ArrayIterator<Field> fields) {
		while (!fields.isFinished()){
			Field field = fields.getNext();
			Annotation[] annotations = field.getAnnotations();
			
			if (isID(annotations))
				throw new RuntimeException("ID field must be one and only one");
			
			if (isDummy(annotations))
				entityMapper.addDummyField(field);
			else
			if (isTransient(annotations))
				entityMapper.addTransientField(field);
			else
			if (isColumn(annotations))
				entityMapper.addColumnField(field, getColumnName(annotations));
			else
			if (isOneToOne(annotations)){
				Class<?> fieldClass = field.getType();
				EntityMapper<?> one2oneMapper = createMapper(fieldClass);
				entityMapper.addOneToOneMapper(field, one2oneMapper);
			}
			else
			if (isOneToMany(annotations)){
				Class<?> oneToManyType = getOneToManyType(annotations);
				EntityMapper<?> oneToManyMapper = createMapper(oneToManyType);
				entityMapper.addOneToManyMapper(field, oneToManyMapper);
			}
		}
	}
	
	private static Field findID(ArrayIterator<Field> fields){
		while (! fields.isFinished()){
			Field field = fields.getNext(); 
			
			Annotation[] annotations = field.getAnnotations();
			
			if (isTransient(annotations))
				continue;
			
			if (isID(annotations))
				return field;
			
			throw new RuntimeException("ID field must be a first field in the entity");
		}
		throw new RuntimeException("ID field not found in the entity");
	}
		
	private static String getIdColumnName(Annotation[] annotations) {
		ID idAnnotation = (ID)getByClass(annotations, ID.class);
		return idAnnotation.name();
	}

	private static String getColumnName(Annotation[] annotations) {
		Column colAnnotation = (Column)getByClass(annotations, Column.class);
		return colAnnotation.name();
	}

	private static Class<?> getOneToManyType(Annotation[] annotations) {
		OneToMany annotation = (OneToMany)getByClass(annotations, OneToMany.class);
		return annotation.type();
	}
	
	private static boolean isOneToOne(Annotation[] annotations) {
		return containsClass(annotations, OneToOne.class);
	}
	
	private static boolean isDummy(Annotation[] annotations) {
		return containsClass(annotations, Ignore.class);
	}
	
	private static boolean isID(Annotation[] annotations) {
		return containsClass(annotations, ID.class);
	}

	private static boolean isColumn(Annotation[] annotations) {
		return containsClass(annotations, Column.class);
	}

	private static boolean isOneToMany(Annotation[] annotations) {
		return containsClass(annotations, OneToMany.class);
	}

	private static boolean isTransient(Annotation[] annotations) {
		return annotations.length == 0 || containsClass(annotations, Transient.class);
	}
		
	private static boolean containsClass(Annotation[] annotations, Class<?> clazz) {
		return getByClass(annotations, clazz) != null;
	}

	private static Annotation getByClass(Annotation[] annotations, Class<?> clazz) {
		for (Annotation a: annotations)
			if (clazz.isInstance(a))
				return a;
		return null;
	}

}
