package gugit.om.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/***
 * represents data to be used by the INSERT or UPDATE statements for ManyToMany collections
 * 
 * @author urbonman
 *
 */
public class M2MWritePacket implements IWritePacket{

	private String tableName;
	private String leftSideCol;
	private String leftSideField;
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor leftSideIdAccessor;
	private Object leftSideEntity;
	
	private String rightSideCol;
	private String rightSideField;
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor rightSideIdAccessor;
	@SuppressWarnings("rawtypes")
	private Collection rightSideEntities;

	private M2MWritePacketElement element = null;

	public M2MWritePacket(String m2mTableName){
		this.tableName = m2mTableName;
	}
	
	@SuppressWarnings("rawtypes")
	public void setLeftSideDependency(String colName, String fieldName, Object entity, IPropertyAccessor idAccessor){
		this.leftSideCol = colName;
		this.leftSideField = fieldName;
		this.leftSideIdAccessor = idAccessor;
		this.leftSideEntity = entity;
	}
	
	@SuppressWarnings("rawtypes")
	public void setRightSideDependency(String colName, String fieldName, Collection entities, IPropertyAccessor idAccessor){
		this.rightSideCol = colName;
		this.rightSideField = fieldName;
		this.rightSideIdAccessor = idAccessor;
		this.rightSideEntities = entities;
	}
	
	public String getEntityName(){
		return tableName;
	}
	
	@SuppressWarnings("unchecked")
	public boolean trySolveDependencies() {
		element = null;
		
		if (rightSideEntities == null || rightSideEntities.isEmpty())
			return true;
		
		Object leftSideID = leftSideIdAccessor.getValue(leftSideEntity);
		if (leftSideID == null)
			return false;
		
		List<Object> rightSideIDs = new ArrayList<>(rightSideEntities.size());
		Iterator<Object> it = rightSideEntities.iterator();
		while (it.hasNext()){
			Object id = rightSideIdAccessor.getValue(it.next());
			if (id==null)
				return false;
			rightSideIDs.add(id);
		}
		
		element = new M2MWritePacketElement(leftSideCol, leftSideField, leftSideID, rightSideCol, rightSideField, rightSideIDs);
		
		return true;
	}
	
	public M2MWritePacketElement getElement(){
		return element;
	}
}
