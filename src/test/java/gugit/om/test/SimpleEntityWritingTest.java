package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacket;
import gugit.om.test.model.Address;
import gugit.om.test.utils.TestUtils;

import org.junit.Test;

public class SimpleEntityWritingTest {
	
	@Test
	public void testInserts() {
		Address address = new Address();
			address.setCountry("New Zealand");
			address.setCity("Wellington");
			address.setStreet("Ocean st 6-66");
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(address);
		
		WritePacket insertData = batch.getNext();
		assertNotNull(insertData);
		assertNull(insertData.getIdElement().value);		
		
		assertEquals(insertData.getByColumnName("CITY").value, "Wellington");
		
		assertNull(insertData.getByColumnName("PERSON_ID"));
		
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
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(address);
		
		WritePacket updateData = batch.getNext();

		assertEquals(address.getId(), updateData.getIdElement().value);
		assertEquals("Wellington", updateData.getByColumnName("CITY").value);
		
		updateData = batch.getNext();
		assertNull(updateData);
	}

}
