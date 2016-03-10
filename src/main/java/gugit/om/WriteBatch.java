package gugit.om;

import gugit.om.mapping.EntityMetadata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WriteBatch {

	private Map<Class<?>, List<UpdateData<?>>> updates = new HashMap<Class<?>, List<UpdateData<?>>>();
	private Map<Class<?>, List<InsertData<?>>> inserts = new HashMap<Class<?>, List<InsertData<?>>>();
	
	public List<UpdateData<?>> getUpdates(Class<?> type) {
		return updates.get(type);
	}
	
	public Map<Class<?>, List<UpdateData<?>>> getUpdates() {
		return updates;
	}
	
	public List<InsertData<?>> getInserts(Class<?> type) {
		return inserts.get(type);
	}
	
	public Map<Class<?>, List<InsertData<?>>> getInserts() {
		return inserts;
	}

	public <E> void addInserts(E entity, EntityMetadata<E> metadata, Map<String, Object> props) {
		List<InsertData<?>> insertList = getInserts().get(entity.getClass());
		if (insertList == null){
			insertList = new LinkedList<InsertData<?>>();
			getInserts().put(entity.getClass(), insertList);
		}
		insertList.add(new InsertData<E>(entity, metadata, props));
	}

	public <E> void addUpdates(E entity, EntityMetadata<E> metadata, Object idValue, Map<String, Object> props) {
		List<UpdateData<?>> updatesList = getUpdates().get(entity.getClass());
		if (updatesList == null){
			updatesList = new LinkedList<UpdateData<?>>();
			getUpdates().put(entity.getClass(), updatesList);
		}
		updatesList.add(new UpdateData<E>(entity, metadata, idValue, props));	
	}
	
}
