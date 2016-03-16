package gugit.om;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.WriteTimeDependency;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WriteBatch {

	private Map<Class<?>, List<UpdateData<?>>> updates = new HashMap<Class<?>, List<UpdateData<?>>>();
	private Map<Class<?>, List<InsertData<?>>> inserts = new HashMap<Class<?>, List<InsertData<?>>>();
	
	public <E> WritePad<E> createWritePad(E entity, EntityMetadata<E> metadata){
		return new WritePad<E>(entity, metadata, this);
	}
	
	public List<UpdateData<?>> getAllUpdates(Class<?> type) {
		return updates.get(type);
	}
	
	public Map<Class<?>, List<UpdateData<?>>> getUpdates() {
		return updates;
	}
	
	public List<InsertData<?>> getAllInserts(Class<?> type) {
		return inserts.get(type);
	}
	
	public Map<Class<?>, List<InsertData<?>>> getInserts() {
		return inserts;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInserts(Object entity, EntityMetadata<?> metadata, Map<String, Object> props, List<WriteTimeDependency> dependencies) {
		List<InsertData<?>> insertList = getInserts().get(entity.getClass());
		if (insertList == null){
			insertList = new LinkedList<InsertData<?>>();
			getInserts().put(entity.getClass(), insertList);
		}
		insertList.add(new InsertData(entity, metadata, props, dependencies));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addUpdates(Object entity, EntityMetadata<?> metadata, Map<String, Object> props, List<WriteTimeDependency> dependencies) {
		List<UpdateData<?>> updatesList = getUpdates().get(entity.getClass());
		if (updatesList == null){
			updatesList = new LinkedList<UpdateData<?>>();
			getUpdates().put(entity.getClass(), updatesList);
		}
		updatesList.add(new UpdateData(entity, metadata, props, dependencies));	
	}
	

}
