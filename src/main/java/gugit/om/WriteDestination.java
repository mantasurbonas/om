package gugit.om;

public interface WriteDestination {
	
	public void startNew(String entityName);
	public void startExisting(String entityName);
	
	public void writeId(Object id);
	public void writeSimpleProperty(String name, Object value);
	public WriteDestination createWriterFor(Class<?> entityClass);
	
	public void done();	
}
