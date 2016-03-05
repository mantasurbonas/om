package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.WriteDestination;
import gugit.om.test.model.Address;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SimpleEntityWritingTest {

	private static class TestWriteDestination implements WriteDestination{
		public Map <String, Object> props = new HashMap<String, Object>();
		public boolean isNew = false;
		
		public void startNew(String entityName) {
			props.clear();
			isNew = true;
		}

		public void startExisting(String entityName) {
			props.clear();
			isNew = false;
		}

		public void writeId(Object id) {
			props.put("id", id);
		}

		@Override
		public void writeSimpleProperty(String name, Object value) {
			props.put(name, value);
		}
		
		public WriteDestination createWriterFor(Class<?> entityClass) {
			return null;
		}

		public void done() {
		}
	}
	
	@Test
	public void testWriteNewSingleEntity() {
		Address address = new Address();
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("(none)");
			
		TestWriteDestination writer = new TestWriteDestination();
		new OM<Address>(Address.class)
			.writeEntity(address, writer );
		
		assertEquals(address.getCity(), writer.props.get("CITY"));
		assertEquals(address.getCountry(), writer.props.get("COUNTRY"));
		assertEquals(address.getStreet(), writer.props.get("STREET"));
		assertNull(writer.props.get("id"));
		assertTrue(writer.isNew);
	}

	@Test
	public void testWriteExistingSingleEntity() {
		Address address = new Address();
			address.setId(55);
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("(none)");
			
		TestWriteDestination writer = new TestWriteDestination();
		new OM<Address>(Address.class)
			.writeEntity(address, writer );
		
		assertEquals(address.getCity(), writer.props.get("CITY"));
		assertEquals(address.getCountry(), writer.props.get("COUNTRY"));
		assertEquals(address.getStreet(), writer.props.get("STREET"));
		assertEquals(address.getId(), writer.props.get("id"));
		assertFalse(writer.isNew);
	}

}
