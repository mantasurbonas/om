package gugit.om.mapping;

import gugit.om.WriteBatch;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;

import java.util.HashMap;
import java.util.Map;

public class EntityWriter <E> implements IWriter{
	
	private EntityMetadata<E> entityMetadata;
	
	public EntityWriter(EntityMetadata<E> metadata){
		this.entityMetadata = metadata;
	}
	
	public void write(Object entity, Map<String, Object> props, WriteBatch writeBatch){
		if (entity == null)
			return;
		
		Object id = entityMetadata.getIdField().getBinding().retrieveValueFrom(entity);
		
		props = new HashMap<String, Object>();
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			Object fieldValue = fieldMetadata.getBinding().retrieveValueFrom(entity);
			fieldMetadata.getWriter().write(fieldValue, props, writeBatch);
		}
		
		if (id == null)
			writeBatch.addInserts(entity, entityMetadata, props);
		else
			writeBatch.addUpdates(entity, entityMetadata, props);
	}
}
