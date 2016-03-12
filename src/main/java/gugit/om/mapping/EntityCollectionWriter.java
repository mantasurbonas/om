package gugit.om.mapping;

import gugit.om.WriteBatch;
import gugit.om.metadata.EntityMetadata;

import java.util.Collection;
import java.util.Map;

public class EntityCollectionWriter<E> extends EntityWriter<E> {

	public EntityCollectionWriter(EntityMetadata<E> metadata) {
		super(metadata);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void write(Object value, Map<String, Object> props, WriteBatch writeBatch) {
		Collection collection = (Collection)value;
		for (Object val: collection)
			super.write(val, props, writeBatch);
	}

}
