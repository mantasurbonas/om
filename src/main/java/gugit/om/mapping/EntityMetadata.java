package gugit.om.mapping;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

/***
 * a holder for abstract information on a particular object (provided it is annotated with gugit::OM annotations)
 * 
 * @author urbonman
 */
public class EntityMetadata<E> {

	// a class of an entity
	private Class<E> entityClass;
	
	// for creating entity instances
	private ConstructorAccess<E> constructor;

	// the user-provided entity name, if different from its class name
	private String entityName;
	
	// mappings between objects in the resultset and the appropriate fields in the entity class
	private LinkedList<FieldMapping> fieldMappings = new LinkedList<FieldMapping>();
	
	// a field mapping specifically for the ID column
	private FieldMapping idMapping;

	// helper to access actual methods
	private MethodAccess access;
	
	// the column names, as annotated with @Column(name="column-name") annotation
	private LinkedList<String> columnNames = new LinkedList<String>();
	
	// the POJO field names as named by developer
	private LinkedList<String> fieldNames = new LinkedList<String>();

	public EntityMetadata(Class<E> clazz) {
		this.access = MethodAccess.get(clazz);
		this.constructor = ConstructorAccess.get(clazz);
		this.setEntityClass(clazz);
	}

	public E createEntity(Object id){
		E entity = constructor.newInstance(); //entityClass.newInstance();
		if (id != null)
			getIdMapping().invokeSetter(entity, id);
		return entity;
	}
	
	public void setIDField(Field field, String idColumnName) {
		this.setIdMapping(FieldMappingFactory.column(field, idColumnName, access));
		addFieldMapping(getIdMapping());
		addColumn(field, idColumnName);
	}

	public void addColumnField(Field field, String columnName) {
		addFieldMapping(FieldMappingFactory.column(field, columnName, access));
		addColumn(field, columnName);
	}

	public void addDummyField(Field field) {
		addFieldMapping(FieldMappingFactory.dummy());
	}

	public void addTransientField(Field field) {
		;
	}

	
	public void addOneToOneField(Field field, EntityMetadata<?> fieldMetadata) {
		addFieldMapping(FieldMappingFactory.oneToOne(field, access, fieldMetadata));
	}

	public void addOneToManyField(Field field, EntityMetadata<?> fieldMetadata) {
		addFieldMapping(FieldMappingFactory.oneToMany(field, access, fieldMetadata));
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	public LinkedList<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public FieldMapping getIdMapping() {
		return idMapping;
	}

	public void setIdMapping(FieldMapping idMapping) {
		this.idMapping = idMapping;
	}

	public void setEntityName(final String name){
		this.entityName = name;
	}

	public String getEntityName(){
		return entityName;
	}
	
	public List<String> getFieldNames(){
		return fieldNames;
	}
	
	public List<String> getColumnNames(){
		return columnNames;
	}
	
	
	private void addFieldMapping(FieldMapping mapping) {
		getFieldMappings().add(mapping);
	}
	
	private void addColumn(Field field, String columnName) {
		fieldNames.add(field.getName());
		columnNames.add(columnName);
	}

}
