package gugit.om;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.WriteTimeDependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateData<E> {

	private EntityMetadata<E> metadata;
	private E entity;
	
	private Map<String, Object> data = new HashMap<String, Object>();
	private List<WriteTimeDependency> dependencies;

	public UpdateData(E entity, EntityMetadata<E> metadata, Map<String, Object> data, List<WriteTimeDependency> dependencies){
		this.setEntity(entity);
		this.setData(data);
		this.setMetadata(metadata);
		this.setDependencies(dependencies);
	}

	public UpdateData(E entity, EntityMetadata<E> metadata){
		this.setEntity(entity);
		this.setMetadata(metadata);
	}
	
	public void add(String propName, Object value){
		getData().put(propName, value);
	}
	
	public EntityMetadata<E> getMetadata() {
		return metadata;
	}

	private void setMetadata(EntityMetadata<E> metadata) {
		this.metadata = metadata;
	}

	public E getEntity() {
		return entity;
	}

	private void setEntity(E entity) {
		this.entity = entity;
	}

	public Object get(String propertyName) {
		return data.get(propertyName);
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	private void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	private void setDependencies(List<WriteTimeDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public List<WriteTimeDependency> getDependencies(){
		return dependencies;
	}
}
