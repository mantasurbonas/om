package gugit.om.metadata;

public class MasterRefFieldMetadata extends FieldMetadata{

	private String masterIDName;

	public MasterRefFieldMetadata(final String fieldName, 
									final String masterIDName, 
									final String columnName, 
									int columnOffset) {
		super(fieldName, columnName, columnOffset);
		this.masterIDName = masterIDName;
	}

	public String getMasterIDName() {
		return masterIDName;
	}
}
