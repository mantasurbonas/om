package gugit.om.metadata;

import java.util.LinkedList;

/***
 * a holder for arbitrary information on a particular object (provided it is annotated with gugit::OM annotations)
 * 
 * @author urbonman
 */
public class EntityMetadata<E> {

	// a class of an entity
	private Class<E> entityClass;

	// the user-provided entity name, if different from its class name (for persisting)
	private String entityName;
		
	// one special annotation - for ID field, specifically
	private FieldMetadata idField;

	// how many recordset column does this entity occupy.
	// (NULL if it is impossible to determine precisely - i.e. if subentities contain endless recursions) 
	private Integer width = null;
	
	// the "simple" columns like date, string or integers
	private LinkedList<FieldMetadata> primitiveFields = new LinkedList<FieldMetadata>();
	
	// the 1-to-1 (master to detail) relationships 
	private LinkedList<FieldMetadata> pojoFields = new LinkedList<FieldMetadata>();
	
	// the 1-to-many (master to collection of details) relationships
	private LinkedList<DetailCollectionFieldMetadata> pojoCollectionFields = new LinkedList<DetailCollectionFieldMetadata>();
	
	// references to parent (master) entities, if any
	private LinkedList<MasterRefFieldMetadata> masterRefFields = new LinkedList<MasterRefFieldMetadata>();


	public EntityMetadata(Class<E> entityClass, final String entityName, FieldMetadata idField) {
		this.entityClass = entityClass;
		this.entityName = entityName;		
		this.idField = idField;
	}
	
	public Class<E> getEntityClass() {
		return entityClass;
	}

	public String getEntityName(){
		return entityName;
	}

	public FieldMetadata getIdField(){
		return idField;
	}

	public void setWidth(Integer columnCount) {
		this.width = columnCount;
	}
	
	public Integer getWidth(){
		return width;
	}

	public LinkedList<FieldMetadata> getPrimitiveFields() {
		return primitiveFields;
	}

	public void setPrimitiveFields(LinkedList<FieldMetadata> primitiveFields) {
		this.primitiveFields = primitiveFields;
	}

	public LinkedList<FieldMetadata> getPojoFields() {
		return pojoFields;
	}

	public LinkedList<DetailCollectionFieldMetadata> getPojoCollectionFields() {
		return pojoCollectionFields;
	}

	public LinkedList<MasterRefFieldMetadata> getMasterRefFields() {
		return masterRefFields;
	}

	public void addPrimitiveField(FieldMetadata fieldMetadata){
		primitiveFields.add(fieldMetadata);
	}
	
	public void addPojoField(FieldMetadata fieldMetadata){
		pojoFields.add(fieldMetadata);
	}
	
	public void addPojoCollectionField(DetailCollectionFieldMetadata field){
		pojoCollectionFields.add(field);
	}
	
	public void addMasterRefField(MasterRefFieldMetadata field){
		masterRefFields.add(field);
	}
	
}
