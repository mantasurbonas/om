package gugit.om;

import java.util.HashMap;
import java.util.Map;

public class UpdateData<E> {

	private String entityName;
	private Class<E> entityClass;
	
	private String idName;
	private Object idValue;
	
	private Map<String, Object> data = new HashMap<String, Object>();

	public UpdateData(Class<E> entityClass, String idName, Object idValue, Map<String, Object> data){
		this.setEntityClass(entityClass);
		this.setID(idName, idValue);
		this.setData(data);
	}
	
	public UpdateData(Class<E> entityClass, final String entityName){
		this.setEntityClass(entityClass);
		this.setEntityName(entityName);
	}

	public void setID(String propName, Object value){
		this.setIdName(propName);
		this.setIdValue(value);
	}
	
	public void add(String propName, Object value){
		getData().put(propName, value);
	}
	
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}

	public Object getIdValue() {
		return idValue;
	}

	public void setIdValue(Object idValue) {
		this.idValue = idValue;
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
