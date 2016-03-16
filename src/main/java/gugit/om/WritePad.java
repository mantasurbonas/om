package gugit.om;

import gugit.om.mapping.NullWriteValue;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.WriteTimeDependency;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WritePad <E>{

	private WriteBatch writeBatch;

	private Map<String, Object> props = new HashMap<String, Object>();
	private List<WriteTimeDependency> dependencies = new LinkedList<WriteTimeDependency>();

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

	public void addDependency(WriteTimeDependency dependency) {
		dependencies.add(dependency);
	}

	
	public void finish(){
		if (!isInsert)
			writeBatch.addUpdates(entity, metadata, props, dependencies);
		else
			writeBatch.addInserts(entity, metadata, props, dependencies);
	}

	public void setIsInsert(boolean b) {
		this.isInsert = b;
	}
}
