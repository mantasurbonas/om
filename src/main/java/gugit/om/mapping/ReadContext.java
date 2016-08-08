package gugit.om.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gugit.om.utils.FastStack;
import gugit.om.wrapping.WrappedEntityGenerator;


/***
 * Contains the state of IReader between invocations to IReader::read().
 * 
 * @author urbonman
 *
 */
public class ReadContext {
	
	// needed to read related entities
	private ISerializerFactory serializers;
	
	// needed to create new (wrapped) entity instances
	private static WrappedEntityGenerator generator = new WrappedEntityGenerator();
	
	// read state
	private FastStack currentlyReadEntities = new FastStack(); 
	
	// cached helpers by resultset offset position
	private static class PositionInfo{
		Map<Object, Object> previousReads = new HashMap<>();
		
		IReader reader;
		
		Class entityClass;
	};
	
	private ArrayList<PositionInfo> positionInfos = new ArrayList<>();
	
	
	public ReadContext(ISerializerFactory serializers){
		this.serializers = serializers;
	}
	
	public void entityIsBeingRead(Object entity, Object id){
		currentlyReadEntities.push(entity, id);
	}
	
	public void entityReadingFinished(){
		currentlyReadEntities.pop();
	}
	
	public Object findMasterEntity(Class<?> type, Object id){
		return currentlyReadEntities.find(type, id);
	}
	
	public Object getCachedRead(int position, Object id){
		return getPositionInfo(position).previousReads.get(id);
	}
	
	public void cacheRead(int position, Object id, Object obj){
		getPositionInfo(position).previousReads.put(id, obj);
	}
	
	public void resetRead(int position){
		//previousReads.put(position, null);
	}
	
	public void clear(){
		currentlyReadEntities.clear();
		positionInfos.clear();
	}

	public <E> IReader<E> getReaderFor(int position, Class<E> entityClass){
		PositionInfo positionInfo = getPositionInfo(position);
		if (positionInfo.reader == null)
			positionInfo.reader = serializers.getSerializerFor(entityClass);
		
		return positionInfo.reader;
	}
	
	public <E> E createEntity(int position, Class<E> entityClass){
		
		try{
			PositionInfo positionInfo = getPositionInfo(position);
			if (positionInfo.entityClass == null)
				positionInfo.entityClass = generator.getWrappedEntityClass(entityClass);
			return (E) positionInfo.entityClass.newInstance();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private void ensurePosition(int position){
		for (int i=positionInfos.size(); i<position+1; i++)
			positionInfos.add(new PositionInfo());
	}
	
	private PositionInfo getPositionInfo(int position){
		ensurePosition(position);

		return positionInfos.get(position);
	}
	
}
