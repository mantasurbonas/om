package gugit.om.mapping;

public class WritePacketElement{
	public final String columnName;
	public final String fieldName;
	public Object value;
	
	public WritePacketElement(final String colName, final String fName, Object v){
		columnName=colName;
		fieldName=fName;
		value =v;
	}
}
