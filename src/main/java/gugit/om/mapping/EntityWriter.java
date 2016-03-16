package gugit.om.mapping;

import gugit.om.WritePad;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;

public class EntityWriter <E> implements IWriter{
	
	private EntityMetadata<E> entityMetadata;
	
	public EntityWriter(EntityMetadata<E> metadata){
		this.entityMetadata = metadata;
	}
	
	public void write(Object entity, WritePad<?> where){
		if (entity == null)
			return;
		/*
		Object id = entityMetadata.getIdField().getBinding().retrieveValueFrom(entity);
		
		@SuppressWarnings("unchecked")
		WritePad<E> newPad = new WritePad<E>((E)entity, entityMetadata, where.getWriteBatch()); 
		newPad.setIsInsert(id == null);
		
		for (FieldMetadata fieldMetadata: entityMetadata.getFieldMetadataList()){
			Object fieldValue = fieldMetadata.getBinding().retrieveValueFrom(entity);
			fieldMetadata.getWriter().write(fieldValue, newPad);
		}

		newPad.finish();
		*/
	}
}
