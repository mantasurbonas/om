package gugit.om;

import gugit.om.mapping.NullWriteValue;
import gugit.om.metadata.EntityMetadata;

import java.util.HashMap;
import java.util.Map;

public class WritePad <E>{

	private WriteBatch writeBatch;

	private Map<String, Object> props = new HashMap<String, Object>();

	private boolean isInsert = false;

	private EntityMetadata<E> metadata;
	private E entity;
	
	public WritePad(E entity, EntityMetadata<E> metadata, WriteBatch parent){
		this.writeBatch = parent;
		this.entity = entity;
		this.metadata = metadata;
	}
	
	public WriteBatch getWriteBatch(){
		return writeBatch;
	}
	
	public void add(final String propName, Object value){
		if (value == null)
			value = NullWriteValue.getInstance();
		props.put(propName,  value);
	}
	
	public void finish(){
		if (!isInsert)
			writeBatch.addUpdates(entity, metadata, props);
		else
			writeBatch.addInserts(entity, metadata, props);
	}

	public void setIsInsert(boolean b) {
		this.isInsert = b;
	}
}
