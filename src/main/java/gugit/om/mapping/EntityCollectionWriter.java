package gugit.om.mapping;

import gugit.om.WritePad;
import gugit.om.metadata.EntityMetadata;

import java.util.Collection;

public class EntityCollectionWriter<E> extends EntityWriter<E> {

	public EntityCollectionWriter(EntityMetadata<E> metadata) {
		super(metadata);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void write(Object value, WritePad<?> writePad) {
		Collection collection = (Collection)value;
		for (Object val: collection)
			super.write(val, writePad);
	}

}
