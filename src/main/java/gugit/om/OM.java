package gugit.om;

import gugit.om.mapping.EntityMapper;
import gugit.om.mapping.EntityMapperFactory;
import gugit.om.utils.ArrayIterator;

import java.util.LinkedList;
import java.util.List;



public class OM <E>{
	
	private EntityMapper<E> rootEntityMapper;
	
	public OM(Class<E> entityClass) {
		this.rootEntityMapper  = EntityMapperFactory.createMapper(entityClass);
	}
	
	/***
	 * call this first if you want to reuse this object
	 */
	public void reset(){
		rootEntityMapper.reset();
	}

	public void writeEntity(E entity, WriteDestination destination){
		rootEntityMapper.write(entity, destination);
	}

	public void writeEntity(E entity, PersistInfoRegistry registry){
		rootEntityMapper.write(entity, registry.createWriteDestination(entity.getClass()));
	}
	
	public E readEntity(Object[] array){
		return readEntity(new ArrayIterator<Object>(array));
	}
	
	public E readEntity(ArrayIterator<Object> array){
		E res = rootEntityMapper.read(array);
		assertAllDataWasRead(array);
		return res;
	}
	
	public LinkedList<E> readEntities(List<Object []> dataRows){
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
