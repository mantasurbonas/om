package gugit.om;

import gugit.om.metadata.EntityMetadata;

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

	public void addInserts(Object entity, EntityMetadata<?> metadata, Map<String, Object> props) {
		List<InsertData<?>> insertList = getInserts().get(entity.getClass());
		if (insertList == null){
			insertList = new LinkedList<InsertData<?>>();
			getInserts().put(entity.getClass(), insertList);
		}
		insertList.add(new InsertData(entity, metadata, props));
	}

	public void addUpdates(Object entity, EntityMetadata<?> metadata, Map<String, Object> props) {
		List<UpdateData<?>> updatesList = getUpdates().get(entity.getClass());
		if (updatesList == null){
			updatesList = new LinkedList<UpdateData<?>>();
			getUpdates().put(entity.getClass(), updatesList);
		}
		updatesList.add(new UpdateData(entity, metadata, props));	
	}

}
