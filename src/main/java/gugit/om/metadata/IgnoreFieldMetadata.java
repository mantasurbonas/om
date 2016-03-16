package gugit.om.metadata;

/***
 * Defines a field that exists in the resultset 
 * 	but which holds no business information.
 * 
 * Thus it must be skipped on reading and should not be persisted back to storage. 
 * 
 * @author urbonman
 */
public class IgnoreFieldMetadata extends FieldMetadata {
	
	public IgnoreFieldMetadata(int offset) {
		super("-=<ignored>=-", "-=<ignored>=-", offset);
	}
}
