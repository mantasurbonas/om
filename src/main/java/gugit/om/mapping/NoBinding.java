package gugit.om.mapping;


public class NoBinding extends Binding{

	private static NoBinding instance = new NoBinding();
	
	private NoBinding() {
	}
	
	public static NoBinding getInstance(){
		return instance; 
	}

	public void assignValueTo(Object entity, Object value){
	}
	
	public Object retrieveValueFrom(Object entity){
		return null;
	}
	
	public boolean isCollection() { 
		return false; 
	}
}
