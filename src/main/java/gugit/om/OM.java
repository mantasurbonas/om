package gugit.om;

import gugit.om.mapping.AbstractReader;
import gugit.om.mapping.AbstractWriter;
import gugit.om.mapping.ReadContext;
import gugit.om.mapping.WriteBatch;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataFactory;
import gugit.om.utils.ArrayIterator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/***
 * facade for object reading and writing
 * 
 * @author urbonman
 */
public class OM <E>{
	
	private AbstractReader entityReader;
	private AbstractWriter entityWriter;
	private EntityMetadata<E> entityMetadata;
	private ReadContext readContext;
	
	public OM(Class<E> entityClass) {
		EntityMetadataFactory metadataFactory = new EntityMetadataFactory();
		entityMetadata = metadataFactory.getMetadataFor(entityClass);
	
		entityWriter = metadataFactory.getEntityWriter(entityMetadata);
		entityReader = metadataFactory.getEntityReader(entityMetadata);
		
		readContext = new ReadContext();
	}
	
	/***
	 * call this before reusing this same object next time
	 */
	public void reset(){
		entityReader.reset();
		readContext.clear();
	}

	public WriteBatch writeEntity(E entity){
		WriteBatch result = new WriteBatch();
		writeEntity(entity, result);
		return result;
	}
	
	public void writeEntity(E entity, WriteBatch batch){
		entityWriter.write(entity, batch);
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
	
	@SuppressWarnings("unchecked")
	public E readEntity(ArrayIterator<Object> array){
		return (E)entityReader.read(array, 0, readContext);
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
