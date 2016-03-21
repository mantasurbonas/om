package gugit.om.metadata;

import java.lang.reflect.Field;

public class MasterRefFieldMetadata extends ColumnFieldMetadata{

	private String masterIDName;

	public MasterRefFieldMetadata(Field field, 
									final String masterIDName, 
									final String columnName, 
									int columnOffset) {
		super(field, columnName, columnOffset);
		this.masterIDName = masterIDName;
	}

	public String getMasterIDName() {
		return masterIDName;
	}
}
