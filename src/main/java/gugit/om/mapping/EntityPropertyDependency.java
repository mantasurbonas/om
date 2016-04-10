package gugit.om.mapping;

public class EntityPropertyDependency implements IDependency{
	
	@SuppressWarnings("rawtypes")
	private IPropertyAccessor accessor;
	
	private String columnName;
	private String fieldName;

	@SuppressWarnings("rawtypes")
	public EntityPropertyDependency(IPropertyAccessor accessor, String columnName, String fieldName){
		this.accessor = accessor;
		this.columnName = columnName;
		this.fieldName = fieldName;
	}

	@Override
	public Object solve(Object entity){
		@SuppressWarnings("unchecked")
		Object prop = accessor.getValue(entity);
		if (prop == null)
			return null;
		
		return new Object[]{columnName, fieldName, prop};
	}
}
