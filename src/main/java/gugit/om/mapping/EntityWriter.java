package gugit.om.mapping;

import gugit.om.WriteBatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/***
 * konws how to create WriteBatch'es from an object graph
 * 
 * @author urbonman
 */
public class EntityWriter <E>{

	// information about the type of entities I work with
	private EntityMetadata<E> metadata;
		
	// where to find entity mappers for other entity types, should I need it
	private EntityWriterRegistry writerRegistry;	
	
	
	public EntityWriter(EntityMetadata<E> metadata, EntityWriterRegistry writerRegistry){
		this.metadata = metadata;
		this.writerRegistry = writerRegistry;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void write(E entity, WriteBatch batch){
		Object id = metadata.getIdMapping().invokeGetter(entity);
		
		Map<String, Object> props = new HashMap<String, Object>();
		
		for (FieldMapping fieldMapping: metadata.getFieldMappings()){
			
			if (fieldMapping == metadata.getIdMapping())
				continue;  // id fields are treated separately
			
			switch (fieldMapping.getType()){
			
			case IGNORE: 
				break;
				
			case COLUMN: 
				Object value = fieldMapping.invokeGetter(entity);
				if (value == null)
					value = NullWriteValue.instance();
				props.put(fieldMapping.getColName(), value);
				break;
				
			case ONE_TO_ONE:
				Object sibling = fieldMapping.invokeGetter(entity);
				if (sibling != null){
					Class<?> siblingClass = fieldMapping.getMetadata().getEntityClass();
					EntityWriter siblingWriter = writerRegistry.getEntityWriterFor(siblingClass);
					siblingWriter.write(sibling, batch);
				}
				break;
				
			case ONE_TO_MANY:
			{
				Collection children = (Collection) fieldMapping.invokeGetter(entity);
				Class<?> childClass = fieldMapping.getMetadata().getEntityClass();
				EntityWriter childWriter = writerRegistry.getEntityWriterFor(childClass);
				for (Object child: children)
					if (child != null)
						childWriter.write(child, batch);
			}
			break;
				
			default:
				throw new RuntimeException("not implemented");
			}
		}
		
		if (id == null)
			batch.addInserts(entity, metadata, props);
		else
			batch.addUpdates(entity, metadata, id, props);
	}
	
	public void reset(){
		; // TODO
	}
	
}
