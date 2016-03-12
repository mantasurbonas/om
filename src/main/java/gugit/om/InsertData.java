package gugit.om;

import gugit.om.metadata.EntityMetadata;

import java.util.HashMap;
import java.util.Map;

public class InsertData<E> {

	private E entity;
	private EntityMetadata<E> metadata;
	
	private Map<String, Object> data;


	public InsertData(E entity, EntityMetadata<E> metadata){
		this(entity, metadata, new HashMap<String, Object>());
	}
	
	public InsertData(E entity, EntityMetadata<E> metadata, Map<String, Object> data){
		this.setEntity(entity);
		this.setData(data);
		this.metadata = metadata;
	}	
	
	public void add(String propName, Object propValue){
		getData().put(propName, propValue);
	}

	public Object get(String propertyName) {
		return getData().get(propertyName);
	}
	
	public E getEntity() {
		return entity;
	}

	public void setEntity(E entity) {
		this.entity = entity;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public EntityMetadata<E> getMetadata() {
		return metadata;
	}

}
