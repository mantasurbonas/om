package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

/***
 * knows how to create a particular object graph from a flat object array.
 * 
 * @author urbonman
 */
public class EntityReader <E>{
	
	// information about the type of entities I work with
	private EntityMetadata<E> metadata;
		
	// cached ID of the entity in a previous row
	private Object lastId;
	
	// cached entity. It will be re-used if a previous row had the same ID
	private E entity;

	// where to find entity mappers for other entity types, should I need it
	private EntityReaderRegistry mapperRegistry;	
	
	
	public EntityReader(EntityMetadata<E> metadata, EntityReaderRegistry mapperRegistry){
		this.metadata = metadata;
		this.mapperRegistry = mapperRegistry;
	}
	
	public void reset(){
		entity = null;
		lastId = null;
	}
	
	public E read(ArrayIterator<Object> row){
		Object id = row.peek();
		
		if (id == null){
			skipReadingCells(row);
			return null;
		}
		
		boolean sameEntity = id.equals(lastId);
		
		if (!sameEntity){
			entity = metadata.createEntity(id);
			lastId = id;
		}			
		
		for (FieldMapping fieldMapping : metadata.getFieldMappings()){
			
			switch(fieldMapping.getType()){
				case IGNORE: 
					row.next();
				break;
				case COLUMN:
				{
					if (!sameEntity)
						fieldMapping.invokeSetter(entity, row.peek());
					row.next();
				}
				break;
				case ONE_TO_ONE:
				{
					EntityReader<?> submapper = mapperRegistry.getEntityReaderFor(fieldMapping.getMetadata().getEntityClass());
					Object siblingEntity = submapper.read(row);
					fieldMapping.invokeSetter(entity, siblingEntity);
				}
				break;
				case ONE_TO_MANY:
				{
					EntityReader<?> submapper = mapperRegistry.getEntityReaderFor(fieldMapping.getMetadata().getEntityClass());
					Object detailEntity = submapper.read(row);
					fieldMapping.invokeAdder(entity, detailEntity);
				}
				break;
				default:
					throw new RuntimeException("not implemented mapping type: "+fieldMapping.getType());
			}												
		}
					
		return entity;
	}

	public E getEntity() {
		return entity;
	}	
	
	
	private void skipReadingCells(ArrayIterator<Object> row) {
		for (FieldMapping fieldMapping: metadata.getFieldMappings()){
			switch(fieldMapping.getType()){
				case IGNORE: 
					row.next();
				break;
				case COLUMN:
					row.next();
				break;
				case ONE_TO_ONE:
				{
					Class<?> siblingClass = fieldMapping.getMetadata().getEntityClass();
					EntityReader<?> siblingMapper = mapperRegistry.getEntityReaderFor(siblingClass);
					siblingMapper.read(row);
				}
				break;
				case ONE_TO_MANY:
				{
					Class<?> childClass = fieldMapping.getMetadata().getEntityClass();
					EntityReader<?> childMapper = mapperRegistry.getEntityReaderFor(childClass);
					childMapper.read(row);
				}
				break;
				default:
					throw new RuntimeException("not implemented mapping type: "+fieldMapping.getType());
			}		
		}
	}

}
