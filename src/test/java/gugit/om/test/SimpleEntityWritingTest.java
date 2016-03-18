package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacket;
import gugit.om.mapping.WritePacketElement;
import gugit.om.test.model.Address;

import java.util.List;

import org.junit.Test;

public class SimpleEntityWritingTest {

	@Test
	public void testInserts() {
		Address address = new Address();
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("Ocean st 6-66");
		
		WriteBatch batch = new OM<Address>(Address.class).writeEntity(address);
		
		WritePacket insertData = batch.getNext();
		assertNotNull(insertData);
		assertNull(insertData.getIdElement().value);		
		
		List<WritePacketElement> insertTokens = insertData.getElements();
		
		String city = (String)getValueByName(insertTokens, "CITY");
		assertEquals(city, "Wellington");
		
		assertNull(getValueByName(insertTokens, "PERSON_ID"));
		
		insertData = batch.getNext();
		assertNull(insertData);
	}

	@Test
	public void testUpdates() {
		Address address = new Address();
			address.setId(789);
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("Ocean st 6-66");
		
		WriteBatch batch = new OM<Address>(Address.class).writeEntity(address);
		
		WritePacket updateData = batch.getNext();

		assertEquals(address.getId(), updateData.getIdElement().value);
		assertEquals("Wellington", (String)getValueByName(updateData.getElements(), "CITY"));
		
		updateData = batch.getNext();
		assertNull(updateData);
	}

	private Object getValueByName(List<WritePacketElement> insertTokens, final String columnName) {
		for (WritePacketElement e: insertTokens)
			if (e.columnName.equalsIgnoreCase(columnName))
				return e.value;
		return null;
	}
	
}
