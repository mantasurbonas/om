package gugit.om.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/***
 * Contains the results of translation from a POJO entity into a dataset for the SQL UPDATE or INSERT statements.
 * 
 * (A data that should come together into a single INSERT or UPDATE statement)
 * 
 * This can be blocked by dependencies to other WritePackets - i.e. if we need other objects to be
 * 		first inserted into the DB and ID's retrieved so that our entity could satisfy foreign key constraints.  
 * 
 * @author urbonman
 *
 */
public class EntityWritePacket implements IWritePacket{	
	
	/***
	 * an object that got translated. 
	 * (we need to keep a reference to it so we could check if dependencies are solved)
	 */
	private Object entity;
	
	/***
	 * as annotated on the entity
	 */
	private String entityName;
	
	/***
	 * the (very specially treated) ID element
	 */
	private WritePacketElement idElement;
		
	/***
	 * the data that shall go into SELECT or UPDATE statements.
	 */
	private List<WritePacketElement> elements = new ArrayList<WritePacketElement>();
	
	/***
	 * describes values that have to be retrieved from other entities before persisting this entity.
	 */
	private List<IDependency> dependencies = new ArrayList<IDependency>();
	
	/***
	 * a delegate for accessing ID property of our entity.
	 * we need this so we could update Entity::ID (after insert is performed and we've got a auto-generated ID from DB).
	 * this way any WritePackets that depend on our entity's ID can proceed to persistance.
	 */
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor idAccessor;

	
	public EntityWritePacket(Object entity, final String entityName) {
		this.entity = entity;
		this.entityName = entityName;
	}

	public Object getEntity(){
		return entity;
	}
	
	public String getEntityName(){
		return entityName;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setID(final String idColumnName, final String idFieldName, IPropertyAccessor idAccess){
		this.idElement = new WritePacketElement(idColumnName, idFieldName, idAccess.getValue(entity));
		this.idAccessor = idAccess;
	}
	
	public WritePacketElement getIdElement(){
		return idElement;
	}
	
	@SuppressWarnings("unchecked")
	public void updateIDValue(Object idValue) {
		idAccessor.setValue(entity, idValue);
		idElement.value = idValue;
	}
	
	public void addElement(final String columnName, final String fieldName, Object value){
		if (value==null)
			value = NullWriteValue.getInstance();
		
		this.elements.add(new WritePacketElement(columnName, fieldName, value));
	}

	public List<WritePacketElement> getElements(){
		return elements;
	}
	
	public WritePacketElement getByColumnName(final String columnName){
		for (WritePacketElement e: elements)
			if (e.columnName.equalsIgnoreCase(columnName))
				return e;
		if (columnName.equalsIgnoreCase(idElement.columnName))
			return idElement;
		return null;
	}

	public WritePacketElement getByFieldName(final String fieldName){
		for (WritePacketElement e: elements)
			if (e.fieldName.equalsIgnoreCase(fieldName))
				return e;
		if (fieldName.equalsIgnoreCase(idElement.fieldName))
			return idElement;
		return null;
	}
	
	public void addDependency(IDependency dependency) {
		this.dependencies.add(dependency);
	}
	
	public boolean trySolveDependencies() {
		Iterator<IDependency> i = dependencies.iterator();
		
		while(i.hasNext()){
			Object[] solution = (Object[])(i.next().solve(entity));
			if (solution == null)
				return false;
			
			addElement((String)solution[0], (String)solution[1], solution[2]);
			i.remove();
		}
		
		return true;
	}

}
