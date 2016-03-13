package gugit.om.metadata;

import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.reflectasm.ConstructorAccess;

/***
 * a holder for arbitrary information on a particular object (provided it is annotated with gugit::OM annotations)
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
	
	// annotations on the fields
	private LinkedList<FieldMetadata> fieldMetadataList = new LinkedList<FieldMetadata>();
	
	// one special annotation - for ID field, specifically
	private FieldMetadata idField;


	public EntityMetadata(Class<E> entityClass, final String entityName, FieldMetadata idField) {
		this.constructor = ConstructorAccess.get(entityClass);
		this.entityClass = entityClass;
		this.entityName = entityName;
		
		this.idField = idField;
		this.addFieldMetadata(idField);
	}
	
	public void addFieldMetadata(FieldMetadata fieldMetadata) {
		this.fieldMetadataList.add(fieldMetadata);
	}
	
	public List<FieldMetadata> getFieldMetadataList() {
		return fieldMetadataList;
	}
	
	public E createEntity(Object id){
		E entity;
		try {
			entity = //entityClass.newInstance();
					constructor.newInstance();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		if (id != null)
			getIdField().getBinding().assignValueTo(entity, id);
		return entity;
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

	public List<String> getFieldNames(){
		List<String> res = new LinkedList<String>();
		for (FieldMetadata metadata: fieldMetadataList){
			if (metadata.isColumn())
				res.add(metadata.getName());
		}
		return res;
	}
	
	public List<String> getColumnNames(){
		List<String> res = new LinkedList<String>();
		for (FieldMetadata metadata: fieldMetadataList){
			if (metadata.isColumn())
				res.add(((ColumnFieldMetadata)metadata).getColumnName());
		}
		return res ;
	}
}
