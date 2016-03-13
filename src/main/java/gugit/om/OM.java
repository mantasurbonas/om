package gugit.om;

import gugit.om.mapping.EntityReader;
import gugit.om.mapping.EntityWriter;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataFactory;
import gugit.om.utils.ArrayIterator;

import java.util.LinkedList;
import java.util.List;


/***
 * facade for object reading and writing
 * 
 * @author urbonman
 */
public class OM <E>{
	
	private EntityReader<E> entityReader;
	private EntityWriter<E> entityWriter;
	private EntityMetadata<E> entityMetadata;
	
	public OM(Class<E> entityClass) {
		entityMetadata = new EntityMetadataFactory().createMetadata(entityClass);
	
		entityWriter = new EntityWriter<E>(entityMetadata);
		entityReader = new EntityReader<E>(entityMetadata);
	}
	
	/***
	 * call this before reusing this same object next time
	 */
	public void reset(){
		entityReader.reset();
	}

	public void writeEntity(E entity, WriteBatch batch){
		WritePad<E> writePad = batch.createWritePad(entity, entityMetadata);
		entityWriter.write(entity, writePad);
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
		E res = entityReader.read(array);
		
		if (false) // TODO: parametrize this check
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
			if (entity != previousEntity){
				result.add(entity);
				previousEntity = entity;
			}
		}
		
		return result;
	}

	private void assertAllDataWasRead(ArrayIterator<Object> row) {
		if (!row.isFinished())
			throw new RuntimeException("row mapping should be completely finished but only "+row.offset+"/"+row.length()+" fields mapped.");
	}
	
}
