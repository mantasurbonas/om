package gugit.om.metadata;

import java.lang.reflect.Field;

public class ManyToManyFieldMetadata  extends DetailCollectionFieldMetadata{

	private String myColumnName;
	private String othColumnName;
	private String tableName;

	public ManyToManyFieldMetadata(Field field, Class<?> detailType, int columnOffset,
									String tableName, String myColumn, String othColumn) {
		super(field, detailType, columnOffset);
		this.tableName = tableName;
		this.myColumnName = myColumn;
		this.othColumnName = othColumn;
	}

	public String getMyColumnName() {
		return myColumnName;
	}

	public void setMyColumnName(String myColumnName) {
		this.myColumnName = myColumnName;
	}

	public String getOthColumnName() {
		return othColumnName;
	}

	public void setOthColumnName(String othColumnName) {
		this.othColumnName = othColumnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
