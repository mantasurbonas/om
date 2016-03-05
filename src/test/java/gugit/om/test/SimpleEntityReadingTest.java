package gugit.om.test;

import static org.junit.Assert.assertEquals;
import gugit.om.OM;
import gugit.om.test.model.Address;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

public class SimpleEntityReadingTest {

	@Test
	public void testReadOneEntity() {
		Object row[] = new Object[]{
			123, "New Zealand", "Wellington", "London str 42B"
		};
		
		Address address = new OM<Address>(Address.class).readEntity(row);
		
		assertEquals("failed deserializing id", 123, address.id.intValue());
		assertEquals("failed deserializing entity field", address.street, row[3]);
	}

	@Test
	public void testReadSeveralEntities(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B" });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2" });
		
		LinkedList<Address> entities = new OM<Address>(Address.class).readEntities(resultset);
		
		assertEquals(entities.size(), 2);
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
	}
	
	@Test
	public void testReadReadEntitiesWithIdenticalIDs(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B" });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2" });
			resultset.add(new Object[]{ 456, "Australia", "Darwin", "River str 94/5" });
		
		LinkedList<Address> entities = new OM<Address>(Address.class).readEntities(resultset);

		assertEquals(entities.size(), 2);
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
	}
}
