package gugit.om;


public interface WriteDestination {
	
	public void startNew(Object entity, String entityName);
	public void startExisting(Object entity, String entityName);
	
	public void writeId(String idName, Object idValue);
	public void writeSimpleProperty(String name, Object value);
	public WriteDestination createWriterFor(Class<?> entityClass);
	
	public void done();
}
