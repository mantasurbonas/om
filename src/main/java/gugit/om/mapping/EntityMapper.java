package gugit.om.mapping;

import gugit.om.WriteDestination;
import gugit.om.utils.ArrayIterator;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

/***
 * Maps one entity to the resultset (where probably several entities are serialized to).
 * 
 *  Intended usage:
 *  
 *  personMapper = new EntityMapper<Person>(Person.class);
 *  
 *  personMapper.setIDField(Person.id, "ID");
 *  personMapper.addColumn(Person.name, "NAME");
 *  personMapper.addColumn(Person.address, "ADDRESS");
 *  personMapper.addOneToManyMapping(Person.employments, employmentMapper);
 *  
 *  Person person = personMapper.read(resultset);
 * 
 * @author urbonman
 *
 * @param <E>
 */
public class EntityMapper<E> {

	private MethodAccess access;
	private ConstructorAccess<E> constructor;
	
	// mappings between objects in the resultset and the appropriate fields in the entity class
	private LinkedList<FieldMapping> fieldMappings = new LinkedList<FieldMapping>();
	
	// a field mapping specifically for the ID column
	private FieldMapping idMapping;
	
	// cached ID of the entity in a previous row
	private Object lastId;
	
	// cached entity. It will be re-used if a previous row had the same ID
	private E entity;
	
	// a class of an entity
	private Class<E> entityClass;
	
	
	public EntityMapper(Class<E> clazz){
		this.access = MethodAccess.get(clazz);
		this.constructor = ConstructorAccess.get(clazz);
		this.entityClass = clazz;
	}
	
	public void reset(){
		entity = null;
		lastId = null;
	}
	
	public void setIDField(Field field, String idColumnName){		
		FieldMapping idMapping = FieldMapping.column(idColumnName, access, createGetter(field), createSetter(field));
		this.idMapping = idMapping;
		fieldMappings.add(idMapping);
	}
	
	public void addColumnField(Field field, String columnName){
		fieldMappings.add(FieldMapping.column(columnName, access, createGetter(field), createSetter(field)));
	}

	public void addDummyField(Field field) {
		fieldMappings.add(FieldMapping.dummy());
	}

	public void addTransientField(Field field) {
		; // do nothing 
	}

	public void addOneToOneMapper(Field field, EntityMapper<?> mapper) {
		fieldMappings.add(FieldMapping.oneToOne(access, createGetter(field), createSetter(field), mapper));
	}

	public void addOneToManyMapper(Field field, EntityMapper<?> mapper) {
		fieldMappings.add(FieldMapping.oneToMany(access, createGetter(field), createSetter(field), mapper));
	}

	
	public E read(ArrayIterator<Object> row){
		Object id = row.peek();
		
		if (id == null){
			skipReadingCells(row);
			return null;
		}
		
		boolean sameEntity = false;
		
		if (id.equals(lastId))
			sameEntity = true;
		else{
			entity = constructor.newInstance(); //entityClass.newInstance();
			lastId = id;
		}			
		
		for (FieldMapping mapping : fieldMappings){
			
			switch(mapping.getType()){
				case IGNORE: 
					row.next();
				break;
				case COLUMN:
					if (!sameEntity)
						invokeSetter(mapping, row.peek());
					row.next();
				break;
				case ONE_TO_ONE:
					Object siblingEntity = mapping.getSubmapper().read(row);
					invokeSetter(mapping, siblingEntity);
				break;
				case ONE_TO_MANY:
					Object detailEntity = mapping.getSubmapper().read(row);
					invokeAdder(mapping, detailEntity);
				break;
				default:
					throw new RuntimeException("not implemented mapping type: "+mapping.getType());
			}												
		}
					
		return entity;
	}

	public void write(Object entity, WriteDestination writer){
		Object id = invokeGetter(entity, idMapping);
		
		String entityName = "some";
		
		if (id == null)
			writer.startNew(entityName);
		else{
			writer.startExisting(entityName);
			writer.writeId(id);
		}
		
		for (FieldMapping mapping: fieldMappings){
			if (mapping == idMapping)
				continue; // this was written already, see above
			
			switch (mapping.getType()){
			
			case IGNORE: 
				break;
				
			case COLUMN: 
				writer.writeSimpleProperty(mapping.getColName(), invokeGetter(entity, mapping));
				break;
				
			case ONE_TO_ONE:
				Object sibling = invokeGetter(entity, mapping);
				if (sibling == null)
					return;
				
				mapping.getSubmapper().write(sibling, writer.createWriterFor(mapping.getSubmapper().entityClass));
				
				break;
				
			case ONE_TO_MANY:
				@SuppressWarnings("rawtypes")
				Collection children = (Collection) invokeGetter(entity, mapping);
				EntityMapper<?> submapper = mapping.getSubmapper();
				WriteDestination subwriter = writer.createWriterFor(submapper.entityClass);
				for (Object child: children)
					if (child != null)
						submapper.write(child, subwriter);
				break;
				
			default:
				throw new RuntimeException("not implemented");
			}
		}
		writer.done();
	}
	
	
	private void skipReadingCells(ArrayIterator<Object> row) {
		for (FieldMapping mapping: fieldMappings){
			switch(mapping.getType()){
				case IGNORE: 
					row.next();
				break;
				case COLUMN:
					row.next();
				break;
				case ONE_TO_ONE:
					mapping.getSubmapper().read(row);
				break;
				case ONE_TO_MANY:
					mapping.getSubmapper().read(row);
				break;
				default:
					throw new RuntimeException("not implemented mapping type: "+mapping.getType());
			}		
		}
	}

	public E getEntity() {
		return entity;
	}	
	

	private String createGetter(Field field){
		try {
			return "get"+camelCase(field.getName());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private String createSetter(Field field){
		try {
			return "set"+camelCase(field.getName());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void invokeSetter(FieldMapping mapping, Object value){
		if (value == null)
			return;
		
		try {
			mapping.getMethodAccess().invoke(entity, mapping.getSetterName(), value);
		} catch (IllegalArgumentException e) { // important!! for some reason JVM works many times faster with this catch block here :-/
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Object invokeGetter(Object entity, FieldMapping mapping){
		return mapping.getMethodAccess().invoke(entity, mapping.getGetterName());
	}
	
	@SuppressWarnings("unchecked")
	private void invokeAdder(FieldMapping mapping, Object o) {
		if (o == null)
			return;
		
		try {
			@SuppressWarnings("rawtypes")
			Collection collection = (Collection)mapping.getMethodAccess().invoke(entity, mapping.getGetterName());
			if (!collection.contains(o))
				collection.add(o);
		} catch (IllegalArgumentException e) { // important!! for some reason JVM works many times faster with this catch block here :-/
			throw new RuntimeException(e);
		}
	}
	
	private static String camelCase(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}

}
