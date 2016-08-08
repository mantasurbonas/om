package gugit.om;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ReadContext;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WriteContext;
import gugit.om.utils.ArrayIterator;
import gugit.om.utils.IDataIterator;
import gugit.services.EntityServiceFacade;


/***
 * facade for object reading and writing.
 * 
 * Thread safe
 * 
 * @author urbonman
 */
public class OM{
	
	protected EntityServiceFacade entityService = new EntityServiceFacade();
	
	public <E> WriteBatch writeEntity(E entity){
		WriteBatch result = new WriteBatch(entityService);
		writeEntity(entity, result);
		return result;
	}
	
	public <E> void writeEntity(E entity, WriteBatch batch){
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entity.getClass());
		serializer.write(entity, batch, new WriteContext(entityService));
	}
	
	public <E> WriteBatch writeEntities(Collection<E> entities){
		WriteBatch result = new WriteBatch(entityService);
		writeEntities(entities, result);
		return result;
	}
	
	public <E> void writeEntities(Collection<E> entities, WriteBatch writeBatch){
		if (entities.isEmpty())
			return;
		
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entities.iterator().next().getClass());

		WriteContext writeContext = new WriteContext(entityService);
		for (E entity: entities)
			serializer.write(entity, writeBatch, writeContext);	
	}
	
	public <E> E readEntity(Object[] array, Class<E> entityClass){
		return readEntity(new ArrayIterator<Object>(array), entityClass);
	}
		
	public <E> E readEntity(IDataIterator<Object> array, Class<E> entityClass){
		return readEntity(array, entityClass, new ReadContext(entityService));
	}
	
	protected <E> E readEntity(IDataIterator<Object> array, Class<E> entityClass, ReadContext readContext){
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entityClass);
		return serializer.read(array, 0, readContext);		
	}
	
	public <E> E leftJoin(E entity, final String property, Object[] array){
		return leftJoin(entity, property, new ArrayIterator<Object>(array));
	}

	public <E> E leftJoin(E entity, final String property, IDataIterator<Object> array){
		return leftJoin(entity, property, array, new ReadContext(entityService));
	}
	
	public <E> E leftJoin(E entity, final String property, IDataIterator<Object> array, ReadContext readContext){
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entity.getClass());
		int propIndex = serializer.getPropertyIndex(property);
		serializer.leftJoin(entity, propIndex, array, 0, readContext);
		return entity;
	}
	
	public <E> List<E> readEntities(List<Object []> dataRows, Class<E> entityClass){		
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entityClass);
		ReadContext readContext = new ReadContext(entityService);
		
		ArrayIterator<Object> row = new ArrayIterator<Object>();
		List<E> result = new ArrayList<E>();		
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

	public <E> void leftJoin(List<E> entities, final String property, List<Object[]> dataRows){
		if (entities.isEmpty())
			return;
		
		@SuppressWarnings("unchecked")
		ISerializer<E> serializer = (ISerializer<E>)entityService.getSerializerFor(entities.get(0).getClass());
		int propIndex = serializer.getPropertyIndex(property);

		Iterator<E> it = entities.iterator();
		E entity = it.next();
		
		ReadContext readContext = new ReadContext(entityService);
		ArrayIterator<Object> row = new ArrayIterator<Object>();
		for(Object[] array: dataRows){
			while(!array[0].equals(serializer.getID(entity)))
				if (it.hasNext())
					entity=it.next();
				else
					return;
			
			row.setData(array); // reusing iterator object

			serializer.leftJoin(entity, propIndex, row, 0, readContext);
		}
	}
}
