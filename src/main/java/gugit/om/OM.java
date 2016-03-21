package gugit.om;

import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ISerializerRegistry;
import gugit.om.mapping.ReadContext;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WriteContext;
import gugit.om.utils.ArrayIterator;
import gugit.om.utils.IDataIterator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/***
 * facade for object reading and writing.
 * 
 * retains some state between invocations, thus it is NOT THREAD SAFE.
 * 
 * @author urbonman
 */
public class OM{
	
	protected ReadContext readContext;
	protected WriteContext writeContext;

	protected ISerializerRegistry serializers;
	
	public OM(ISerializerRegistry serializers) {
		this.serializers = serializers;
		
		readContext = new ReadContext(serializers);
		writeContext = new WriteContext(serializers);
	}
	
	/***
	 * call this before reusing this same object next time
	 */
	public void reset(){
		readContext.clear();
	}

	public <E> WriteBatch writeEntity(E entity){
		WriteBatch result = new WriteBatch();
		writeEntity(entity, result);
		return result;
	}
	
	public <E> void writeEntity(E entity, WriteBatch batch){
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)serializers.getSerializerFor(entity.getClass());
		serializer.write(entity, batch, writeContext);
	}
	
	public <E> WriteBatch writeEntities(Collection<E> entities){
		WriteBatch result = new WriteBatch();
		writeEntities(entities, result);
		return result;
	}
	
	public <E> void writeEntities(Collection<E> entities, WriteBatch writeBatch){
		if (entities.isEmpty())
			return;
		
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)serializers.getSerializerFor(entities.iterator().next().getClass());

		for (E entity: entities)
			serializer.write(entity, writeBatch, writeContext);	
	}
	
	public <E> E readEntity(Object[] array, Class<E> entityClass){
		return readEntity(new ArrayIterator<Object>(array), entityClass);
	}
	
	public <E> E readEntity(IDataIterator<Object> array, Class<E> entityClass){
		ISerializer<E> serializer = (ISerializer<E>)serializers.getSerializerFor(entityClass);
		return serializer.read(array, 0, readContext);
	}
	
	public <E> List<E> readEntities(List<Object []> dataRows, Class<E> entityClass){
		reset();
		
		LinkedList<E> result = new LinkedList<E>();
		ISerializer<E> serializer = (ISerializer<E>)serializers.getSerializerFor(entityClass);
		
		ArrayIterator<Object> row = new ArrayIterator<Object>();
		
		E previousEntity = null;
		for (Object[] array: dataRows){
			row.setData(array); // reusing iterator object
			
			E entity = serializer.read(row, 0, readContext);
			if (entity != previousEntity){
				result.add(entity);
				previousEntity = entity;
			}
		}
		
		return result;
	}

}
