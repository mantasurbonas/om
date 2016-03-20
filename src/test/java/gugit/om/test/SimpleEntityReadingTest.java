package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gugit.om.OM;
import gugit.om.metadata.EntityMetadataService;
import gugit.om.test.model.SimpleAddress;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

public class SimpleEntityReadingTest {
	
	
	@Test
	public void testReadOneEntity() {
		Object row[] = new Object[]{
			123, "New Zealand", "Wellington", "London str 42B", null
		};
		EntityMetadataService metadataService = new EntityMetadataService();
		
		SimpleAddress address = new OM<SimpleAddress>(metadataService, SimpleAddress.class).readEntity(row);
		
		assertEquals("failed deserializing id", 123, address.id.intValue());
		assertEquals("failed deserializing entity field", address.street, row[3]);
		assertNull(address.getPerson());
	}

	@Test
	public void testReadSeveralEntities(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
		
		EntityMetadataService metadataService = new EntityMetadataService();
			
		LinkedList<SimpleAddress> entities = new OM<SimpleAddress>(metadataService, SimpleAddress.class).readEntities(resultset);
		
		assertEquals(entities.size(), 2);
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
	
	@Test
	public void testReadReadEntitiesWithIdenticalIDs(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
			resultset.add(new Object[]{ 456, "Australia", "Darwin", "River str 94/5", null });
		
		EntityMetadataService metadataService = new EntityMetadataService();
			
		LinkedList<SimpleAddress> entities = new OM<SimpleAddress>(metadataService, SimpleAddress.class).readEntities(resultset);

		assertEquals(2, entities.size());
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).id.intValue(), 456);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
}
