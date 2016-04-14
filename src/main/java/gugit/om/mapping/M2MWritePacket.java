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
	public String leftSideCol;
	public String leftSideField;
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor leftSideIdAccessor;
	private Object leftSideEntity;
	
	public String rightSideCol;
	public String rightSideField;
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor rightSideIdAccessor;
	@SuppressWarnings("rawtypes")
	private Collection rightSideEntities;

	public String rightSideTable;
	public String rightSideTableId;
	
	private WritePacketElement leftSideElement;
	private WritePacketElement rightSideElement;

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
	public void setRightSideDependency(String colName, String fieldName, Collection entities, IPropertyAccessor idAccessor, String rightSideTable, String rightSideTableId){
		this.rightSideCol = colName;
		this.rightSideField = fieldName;
		this.rightSideIdAccessor = idAccessor;
		this.rightSideEntities = entities;
		
		this.rightSideTable = rightSideTable;
		this.rightSideTableId = rightSideTableId;
	}
	
	public String getEntityName(){
		return tableName;
	}
	
	@SuppressWarnings("unchecked")
	public boolean trySolveDependencies() {
		
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
		
		leftSideElement = new WritePacketElement(leftSideCol, leftSideField, leftSideID);
		rightSideElement = new WritePacketElement(rightSideCol, rightSideField, rightSideIDs);
		
		return true;
	}

	@Override
	public WritePacketElement getByFieldName(String fieldName) {
		if (fieldName.equalsIgnoreCase(leftSideElement.fieldName))
			return leftSideElement;
		else
		if (fieldName.equalsIgnoreCase(rightSideElement.fieldName))
			return rightSideElement;
		else
			throw new RuntimeException("cannot find field "+fieldName);
	}

	@Override
	public List<WritePacketElement> getElements() {
		ArrayList<WritePacketElement> ret = new ArrayList<WritePacketElement>(2);
			ret.add(leftSideElement);
			ret.add(rightSideElement);
		return ret;
	}
}
