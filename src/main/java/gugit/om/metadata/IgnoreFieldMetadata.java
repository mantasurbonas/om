package gugit.om.metadata;

import java.lang.reflect.Field;

/***
 * Defines a field that exists in the resultset 
 * 	but which holds no business information.
 * 
 * Thus it must be skipped on reading and should not be persisted back to storage. 
 * 
 * @author urbonman
 */
public class IgnoreFieldMetadata extends FieldMetadata {
	
	private int offset;

	public IgnoreFieldMetadata(Field field, int offset) {
		super(field);
		this.offset = offset;
	}
	
	public int getOffset(){
		return offset;
	}
}
