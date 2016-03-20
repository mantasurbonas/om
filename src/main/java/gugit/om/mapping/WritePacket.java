package gugit.om.mapping;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
 * Results of translation from a POJO entity into a dataset for the SQL UPDATE or INSERT statements.
 * 
 * (A data that should come together into a single INSERT or UPDATE statement)
 * 
 * This can be blocked by dependencies to other WritePackets - i.e. if we need other objects to be
 * 		inserted into the DB and ID's retrieved so that our entity could satisfy foreign key constraints.  
 * 
 * @author urbonman
 *
 */
public class WritePacket {	
	
	/***
	 * an object that got translated. 
	 * (we need to keep a reference to it so we could check if dependencies are solved)
	 */
	private Object entity;
	
	/***
	 * the (very specially treated) ID element
	 */
	private WritePacketElement idElement;
		
	/***
	 * the data that shall go into SELECT or UPDATE statements.
	 */
	private List<WritePacketElement> elements = new LinkedList<WritePacketElement>();
	
	/***
	 * describes values that have to be retrieved from other entities before persisting this entity.
	 */
	private List<Dependency> dependencies = new LinkedList<Dependency>();
	
	/***
	 * a delegate for accessing ID property of our entity.
	 * we need this so we could update Entity::ID (after insert is performed and we've got a auto-generated ID from DB).
	 * this way any WritePackets that depend on our entity's ID can proceed to persistance.
	 */
	@SuppressWarnings("rawtypes")
	private PropertyAccessor idAccessor;

	
	public WritePacket(Object entity) {
		this.entity = entity;
	}

	public Object getEntity(){
		return entity;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setID(final String idColumnName, final String idFieldName, PropertyAccessor idAccess){
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
		return null;
	}

	public WritePacketElement getByFieldName(final String fieldName){
		for (WritePacketElement e: elements)
			if (e.fieldName.equalsIgnoreCase(fieldName))
				return e;
		return null;
	}
	
	public void addDependency(Dependency dependency) {
		this.dependencies.add(dependency);
	}
	
	public boolean trySolveDependencies() {
		Iterator<Dependency> i = dependencies.iterator();
		
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
