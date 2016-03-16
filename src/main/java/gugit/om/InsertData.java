package gugit.om;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.WriteTimeDependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertData<E> {

	private E entity;
	private EntityMetadata<E> metadata;
	
	private Map<String, Object> data;
	private List<WriteTimeDependency> dependencies;


	public InsertData(E entity, EntityMetadata<E> metadata){
		this(entity, metadata, new HashMap<String, Object>(), null);
	}
	
	public InsertData(E entity, EntityMetadata<E> metadata, Map<String, Object> data, List<WriteTimeDependency> dependencies){
		this.setEntity(entity);
		this.setData(data);
		this.metadata = metadata;
		this.dependencies = dependencies;
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

	private void setEntity(E entity) {
		this.entity = entity;
	}

	public Map<String, Object> getData() {
		return data;
	}

	private void setData(Map<String, Object> data) {
		this.data = data;
	}

	public EntityMetadata<E> getMetadata() {
		return metadata;
	}

	public List<WriteTimeDependency> getDependencies(){
		return dependencies;
	}
	
	public String toString(){
		return "InsertData for "+metadata.getEntityClass().getSimpleName()
				+" "+getData();
	}
}
