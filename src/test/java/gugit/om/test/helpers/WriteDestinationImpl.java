package gugit.om.test.helpers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gugit.om.WriteDestination;

public class WriteDestinationImpl implements WriteDestination{

	public static Map<Class<?>, List<Map<String, Object>>> insertsRegistry = new HashMap<Class<?>, List<Map<String,Object>>>();
	
	public static Map<Class<?>, List<Map<String, Object>>> updatesRegistry = new HashMap<Class<?>, List<Map<String,Object>>>();
	
	public static void reset(){
		insertsRegistry.clear();
		updatesRegistry.clear();
	}
	
	
	private boolean isNew=false;
	private Map<String, Object> currentProps = new HashMap<String, Object>();
	private Class<?> clazz;

	public WriteDestinationImpl(Class<?> entityClass) {
		this.clazz = entityClass;
	}

	public void startNew(String entityName) {
		currentProps = new HashMap<String, Object>();
		isNew = true;
	}

	public void startExisting(String entityName) {
		currentProps = new HashMap<String, Object>();
		isNew = false;
	}

	public void writeId(Object id) {
		currentProps.put("ID", id);
	}

	public void writeSimpleProperty(String name, Object value) {
		currentProps.put(name, value);
	}

	public WriteDestination createWriterFor(Class<?> entityClass) {
		return new WriteDestinationImpl(entityClass);
	}

	public void done() {
		if (isNew)
			addToRegistry(insertsRegistry, clazz, currentProps);
		else
			addToRegistry(updatesRegistry, clazz, currentProps);			
	}
	
	private void addToRegistry(Map<Class<?>, List<Map<String, Object>>> registry, Class<?> type, Map<String, Object> entityProperties) {
		List<Map<String, Object>> res = registry.get(type);
		if (res == null){
			res = new LinkedList<Map<String, Object>>();
			registry.put(type, res);
		}
		res.add(entityProperties);
	}

}
