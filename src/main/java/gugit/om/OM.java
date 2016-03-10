package gugit.om;

import gugit.om.mapping.EntityReader;
import gugit.om.mapping.EntityReaderRegistry;
import gugit.om.mapping.EntityWriter;
import gugit.om.mapping.EntityWriterRegistry;
import gugit.om.utils.ArrayIterator;

import java.util.LinkedList;
import java.util.List;



public class OM <E>{
	
	private EntityReaderRegistry readerRegistry = new EntityReaderRegistry();
	private EntityWriterRegistry writerRegistry = new EntityWriterRegistry();
	
	private Class<E> entityClass;
	
	public OM(Class<E> entityClass) {
		this.entityClass = entityClass;
	}
	
	/***
	 * call this before reusins this same object next time
	 */
	public void reset(){
		readerRegistry.resetAll();
		writerRegistry.resetAll();
	}

	public void writeEntity(E entity, WriteBatch batch){
		EntityWriter<E> writer = writerRegistry.getEntityWriterFor(entityClass);
		writer.write(entity, batch);
	}
	
	public WriteBatch writeEntity(E entity){
		WriteBatch result = new WriteBatch();
		writeEntity(entity, result);
		return result;
	}
	
	public E readEntity(Object[] array){
		return readEntity(new ArrayIterator<Object>(array));
	}
	
	public E readEntity(ArrayIterator<Object> array){
		EntityReader<E> reader = readerRegistry.getEntityReaderFor(entityClass);
		E res = reader.read(array);
		assertAllDataWasRead(array);
		return res;
	}
	
	public LinkedList<E> readEntities(List<Object []> dataRows){
		reset();
		
		LinkedList<E> result = new LinkedList<E>();

		ArrayIterator<Object> row = new ArrayIterator<Object>();
		E previousEntity = null;
		for (Object[] array: dataRows){
			row.setData(array); // reusing iterator object
			
			E entity = readEntity(row);
			if (entity == previousEntity)
				continue;
			
			result.add(entity);
			previousEntity = entity;
		}
		
		return result;
	}

	private void assertAllDataWasRead(ArrayIterator<Object> row) {
		if (!row.isFinished())
			throw new RuntimeException("row mapping should be completely finished but only "+row.offset+"/"+row.length()+" fields mapped.");
	}
	
}
