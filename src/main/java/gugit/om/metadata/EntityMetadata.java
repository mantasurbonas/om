package gugit.om.metadata;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

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
	private ColumnFieldMetadata idField;

	// how many recordset column does this entity occupy.
	// (NULL if it is impossible to determine precisely - i.e. if subentities contain endless recursions) 
	private Integer width = null;

	// the fields that exist in the resultset but must be ignored when reading or persisting.
	private ArrayList<IgnoreFieldMetadata> ignoreFields = new ArrayList<IgnoreFieldMetadata>();
	
	// the "simple" columns like date, string or integers
	private ArrayList<ColumnFieldMetadata> primitiveFields = new ArrayList<ColumnFieldMetadata>();
	
	// the embedded objects. these are embedded into the resultset.
	private ArrayList<ColumnFieldMetadata> pojoFields = new ArrayList<ColumnFieldMetadata>();
	
	// the embedded objects. These are searched from above the current position in the resultset.
	private ArrayList<DetailCollectionFieldMetadata> pojoCollectionFields = new ArrayList<DetailCollectionFieldMetadata>();
	
	// references to parent (master) entities. these are searched to the left of the current position in the resultset.
	private ArrayList<ColumnFieldMetadata> masterRefFields = new ArrayList<ColumnFieldMetadata>();


	public EntityMetadata(Class<E> entityClass, final String entityName, ColumnFieldMetadata idField) {
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

	public ColumnFieldMetadata getIdField(){
		return idField;
	}

	public void setWidth(Integer columnCount) {
		this.width = columnCount;
	}
	
	public Integer getWidth(){
		return width;
	}

	public List<ColumnFieldMetadata> getPrimitiveFields() {
		return primitiveFields;
	}

	public List<ColumnFieldMetadata> getPojoFields() {
		return pojoFields;
	}

	public List<DetailCollectionFieldMetadata> getPojoCollectionFields() {
		return pojoCollectionFields;
	}

	public List<ColumnFieldMetadata> getMasterRefFields() {
		return masterRefFields;
	}

	public List<ColumnFieldMetadata> getRefFields(){
		ArrayList<ColumnFieldMetadata> result = new ArrayList<ColumnFieldMetadata>(masterRefFields.size() + pojoFields.size());
		result.addAll(masterRefFields);
		
		for (ColumnFieldMetadata meta: pojoFields)
			if (meta.getColumnName()!= null)
				result.add(meta);
		
		return result;
	}
	
	public void addPrimitiveField(ColumnFieldMetadata fieldMetadata){
		primitiveFields.add(fieldMetadata);
	}
	
	public void addPojoField(ColumnFieldMetadata fieldMetadata){
		pojoFields.add(fieldMetadata);
	}
	
	public void addPojoCollectionField(DetailCollectionFieldMetadata field){
		pojoCollectionFields.add(field);
	}
	
	public void addMasterRefField(ColumnFieldMetadata field){
		masterRefFields.add(field);
	}

	public void addIgnoreField(IgnoreFieldMetadata field) {
		ignoreFields.add(field);
	}
	
}
