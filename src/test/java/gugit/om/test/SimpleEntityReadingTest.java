package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gugit.om.test.model.SimpleAddress;
import gugit.om.test.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SimpleEntityReadingTest {
	
	@Test
	public void testReadOneEntity() {
		Object row[] = new Object[]{
			123, "New Zealand", "Wellington", "London str 42B", null
		};

		SimpleAddress address = TestUtils.createObjectMapper().readEntity(row, SimpleAddress.class);
		
		assertEquals("failed deserializing id", 123, address.getId().intValue());
		assertEquals("failed deserializing entity field", address.getStreet(), row[3]);
		assertNull(address.getPerson());
	}

	@Test
	public void testReadSeveralEntities(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
		
		List<SimpleAddress> entities = TestUtils.createObjectMapper().readEntities(resultset, SimpleAddress.class);
		
		assertEquals(entities.size(), 2);
		assertEquals(entities.get(0).getId().intValue(), 123);
		assertEquals(entities.get(1).getStreet(), "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
	
	@Test
	public void testReadReadEntitiesWithIdenticalIDs(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
			resultset.add(new Object[]{ 456, "Australia", "Darwin", "River str 94/5", null });
		
		List<SimpleAddress> entities = TestUtils.createObjectMapper().readEntities(resultset, SimpleAddress.class);

		assertEquals(2, entities.size());
		assertEquals(entities.get(0).getId().intValue(), 123);
		assertEquals(entities.get(1).getId().intValue(), 456);
		assertEquals(entities.get(1).getStreet(), "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
}
