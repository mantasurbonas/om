package gugit.om.test;

import static org.junit.Assert.*;

import java.util.List;

import gugit.om.InsertData;
import gugit.om.PersistInfoRegistry;
import gugit.om.UpdateData;
import gugit.om.OM;
import gugit.om.test.model.Address;

import org.junit.Test;

public class SimpleEntityWritingTest {

	@Test
	public void testInserts() {
		Address address = new Address();
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("Ocean st 6-66");
		
		PersistInfoRegistry registry = new PersistInfoRegistry();
		new OM<Address>(Address.class).writeEntity(address, registry);
		
		List<InsertData<?>> inserts = registry.getInserts(Address.class);
		assertEquals(inserts.size(), 1);
		assertEquals(inserts.get(0).get("CITY"), "Wellington");
	}

	@Test
	public void testUpdates() {
		Address address = new Address();
			address.setId(789);
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("Ocean st 6-66");
		
		PersistInfoRegistry registry = new PersistInfoRegistry();
		new OM<Address>(Address.class).writeEntity(address, registry);
		
		List<InsertData<?>> inserts = registry.getInserts(Address.class);
		assertNull(inserts);
		
		List<UpdateData<?>> updates = registry.getUpdates(Address.class);
		assertEquals(updates.size(), 1);
		assertEquals(updates.get(0).get("CITY"), "Wellington");
	}

	
}
