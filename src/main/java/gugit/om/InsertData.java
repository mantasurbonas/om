package gugit.om;

import java.util.HashMap;
import java.util.Map;

public class InsertData<E> {

	private Class<E> entityClass;
	private String entityName;
	
	private Map<String, Object> data;

	public InsertData(Class<E> entityClass){
		this(entityClass, new HashMap<String, Object>());
	}
	
	public InsertData(Class<E> entityClass, Map<String, Object> data){
		this.entityClass = entityClass;
		this.data = data;
		this.entityName = "???";
	}
	
	public InsertData(Class<E> entityClass, final String entityName){
		this.entityClass = entityClass;
		this.entityName = entityName;
		this.data = new HashMap<String, Object>();
	}
	
	public void add(String propName, Object propValue){
		data.put(propName, propValue);
	}
	
	public Class<E> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Object get(String propertyName) {
		return data.get(propertyName);
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

}
