package gugit.om;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersistInfoRegistry {

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

	public void addInserts(Class<?> type, Map<String, Object> props) {
		List<InsertData<?>> insertList = getInserts().get(type);
		if (insertList == null){
			insertList = new LinkedList<InsertData<?>>();
			getInserts().put(type, insertList);
		}
		insertList.add(new InsertData<>(type, props));
	}

	public void addUpdates(Class<?> type, String idName, Object idValue, Map<String, Object> props) {
		List<UpdateData<?>> updatesList = getUpdates().get(type);
		if (updatesList == null){
			updatesList = new LinkedList<UpdateData<?>>();
			getUpdates().put(type, updatesList);
		}
		updatesList.add(new UpdateData<>(type, idName, idValue, props));	
	}
	
	public WriteDestination createWriteDestination(Class<?> type){
		return new WriteDestinationImpl(type);
	}
	
	private class WriteDestinationImpl implements WriteDestination{
		private Class<?> type;
		private Boolean isNew;
		private String idName;
		private Object idValue;
		private Map<String, Object> props;
		
		public WriteDestinationImpl(Class<?> type){
			this.type = type;
		}
			
		@Override
		public void startNew(String entityName) {
			isNew = true;
			props = new HashMap<String, Object>();
		}

		@Override
		public void startExisting(String entityName) {
			isNew = false;
			props = new HashMap<String, Object>();
		}

		@Override
		public void writeId(String idName, Object idValue) {
			this.idName = idName;
			this.idValue = idValue;
		}

		@Override
		public void writeSimpleProperty(String name, Object value) {
			props.put(name, value);
		}

		@Override
		public WriteDestination createWriterFor(Class<?> entityClass) {
			return new WriteDestinationImpl(entityClass);
		}

		@Override
		public void done() {
			if (isNew)
				addInserts(type, props);
			else
				addUpdates(type, idName, idValue, props);
		}
	}
	
}
