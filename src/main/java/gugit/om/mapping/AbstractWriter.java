package gugit.om.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWriter {
	
	public void writeAll(Collection objects, WriteBatch batch){
		for (Object o: objects)
			write(o, batch);
	}
	
	public abstract void write(Object obj, WriteBatch batch);
	
	
	protected Map writers = new HashMap();
	public void setWriters(Map writers){
		this.writers = writers;
	}
	
	protected AbstractWriter getWriter(Class entityClass) {
		return (AbstractWriter)writers.get(entityClass);
	}
}
