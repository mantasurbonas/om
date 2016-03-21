package gugit.om.test.utils;

import gugit.om.OM;
import gugit.om.mapping.SerializerRegistry;
import gugit.om.metadata.EntityMetadataService;

public class TestUtils {

	public static OM createObjectMapper(){
		SerializerRegistry serializers = new SerializerRegistry();
		EntityMetadataService entityMetadataService = new EntityMetadataService();
		serializers.setEntityMetadataService(entityMetadataService);
		entityMetadataService.setSerializerRegistry(serializers);
		
		return new OM(serializers);
	}
}
