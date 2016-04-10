package gugit.om.mapping;

import java.util.Collection;

public class M2MWritePacketElement {

	public final String leftSideColumn;
	public final String leftSideFieldName;
	public Object leftSideValue;
	
	public final String rightSideColumn;
	public final String rightSideFieldName;
	public Collection<Object> rightSideValues;
	
	public M2MWritePacketElement(String leftCol, String leftField, Object leftVal, 
								 String rightCol, String rightField, Collection<Object> rightVals){
		this.leftSideColumn = leftCol;
		this.leftSideFieldName = leftField;
		this.leftSideValue = leftVal;
		
		this.rightSideColumn = rightCol;
		this.rightSideFieldName = rightField;
		this.rightSideValues = rightVals;
	}
}
