package gugit.om;

import gugit.om.mapping.AbstractReader;
import gugit.om.mapping.EntityWriter;
import gugit.om.mapping.ReadContext;
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
	
	private AbstractReader entityReader;
	private EntityWriter<E> entityWriter;
	private EntityMetadata<E> entityMetadata;
	private ReadContext readContext;
	
	public OM(Class<E> entityClass) {
		EntityMetadataFactory metadataFactory = new EntityMetadataFactory();
		entityMetadata = metadataFactory.getMetadataFor(entityClass);
	
		entityWriter = new EntityWriter<E>(entityMetadata);
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
