package gugit.om.metadata;

import gugit.om.mapping.Binding;

public class WriteTimeDependency {

	private Binding fieldAccessor;
	private Binding masterFieldAccessor;
	private String myColumnName;
	private Object value = null;

	public enum Status{
		DEPENDENCY_IS_NULL,
		DEPENDENT_PROPERTY_UNDEFINED,
		DEPENDENT_PROPERTY_DEFINED
	}
	
	public WriteTimeDependency(Binding fieldAccessor, Binding masterFieldAccessor, String myColumnName) {
		this.fieldAccessor = fieldAccessor;
		this.masterFieldAccessor = masterFieldAccessor;
		this.myColumnName = myColumnName;
	}

	public Status apply(Object entity){
		Object masterEntity = fieldAccessor.retrieveValueFrom(entity);
		
		if (masterEntity == null)
			return Status.DEPENDENCY_IS_NULL;
		
		Object masterEntityProperty = masterFieldAccessor.retrieveValueFrom(masterEntity);
		if (masterEntityProperty == null)
			return Status.DEPENDENT_PROPERTY_UNDEFINED;
		
		this.value  = masterEntityProperty;
		return Status.DEPENDENT_PROPERTY_DEFINED;
	}
	
	public Object getValue(){
		return value;
	}
	
	public String getMyColumnName(){
		return myColumnName;
	}
}
