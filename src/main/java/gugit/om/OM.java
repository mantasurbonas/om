package gugit.om;

import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ReadContext;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WriteContext;
import gugit.om.metadata.EntityMetadataService;
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
public class OM <E>{
	
	protected ISerializer<E> entitySerializer;

	protected ReadContext readContext;
	private WriteContext writeContext;
	
	public OM(EntityMetadataService metadataService, Class<E> entityClass) {
		entitySerializer = metadataService.getSerializerFor(entityClass);
		
		readContext = new ReadContext(metadataService);
		writeContext = new WriteContext(metadataService);
	}
	
	/***
	 * call this before reusing this same object next time
	 */
	public void reset(){
		readContext.clear();
	}

	public WriteBatch writeEntity(E entity){
		WriteBatch result = new WriteBatch();
		writeEntity(entity, result);
		return result;
	}
	
	public void writeEntity(E entity, WriteBatch batch){
		entitySerializer.write(entity, batch, writeContext);
	}
	
	public WriteBatch writeEntities(Collection<E> entities){
		WriteBatch result = new WriteBatch();
		for (E entity: entities)
			writeEntity(entity, result);
		return result;		
	}
	
	public E readEntity(Object[] array){
		return readEntity(new ArrayIterator<Object>(array));
	}
	
	public E readEntity(IDataIterator<Object> array){
		return (E)entitySerializer.read(array, 0, readContext);
	}
	
	public LinkedList<E> readEntities(List<Object []> dataRows){
		reset();
		
		LinkedList<E> result = new LinkedList<E>();

		ArrayIterator<Object> row = new ArrayIterator<Object>();
		E previousEntity = null;
		for (Object[] array: dataRows){
			row.setData(array); // reusing iterator object
			
			E entity = readEntity(row);
			if (entity != previousEntity){
				result.add(entity);
				previousEntity = entity;
			}
		}
		
		return result;
	}

}
