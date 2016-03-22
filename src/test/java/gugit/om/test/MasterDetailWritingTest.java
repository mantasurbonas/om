package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gugit.om.mapping.NullWriteValue;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacket;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;
import gugit.om.test.utils.TestUtils;

import org.junit.Test;

public class MasterDetailWritingTest {
	
	@Test
	public void testInsertIncompleteMaster() {
		Person person = new Person();
		person.setName("John Smith");
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(person);
		
		WritePacket insertData = batch.getNext();
		assertEquals(person.getName(), insertData.getByColumnName("NAME").value);
		assertEquals(NullWriteValue.getInstance(), insertData.getByColumnName("CURRENT_ADDRESS_ID").value);
		
		insertData = batch.getNext();
		assertNull(insertData);
	}

	@Test
	public void testUpdateIncompleteMaster(){
		Person person = new Person();
		person.setId(456);

		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(person);
		
		WritePacket updateData = batch.getNext();
		assertEquals(NullWriteValue.getInstance(), updateData.getByColumnName("NAME").value);
		assertEquals(person.getId(), updateData.getIdElement().value);
		assertEquals(NullWriteValue.getInstance(), updateData.getByColumnName("CURRENT_ADDRESS_ID").value);
		
		updateData = batch.getNext();
		assertNull(updateData);
	}
	
	@Test
	public void testInsertMasterDetail(){
		Person person = new Person();
			person.setName("John Smith");
			person.setId(777);
			
		Address address = new Address();
			address.setCountry("Antarctica");
			address.setOwner(person);
			
		person.getPreviousAddresses().add(address);
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(person);
		
		WritePacket insertData = batch.getNext();
		assertEquals(insertData.getEntity(), person); // person gets inserted first but both entities have all they needs.  
		
		insertData = batch.getNext();
		assertEquals(insertData.getEntity(), address); //
		assertEquals(address.getOwner().getId(), insertData.getByColumnName("\"OWNER_ID\"").value);
		
		insertData = batch.getNext();
		assertNull(insertData);
	}
	
	@Test
	public void testMasterDetailWriting() {
		
		Person person = new Person();
			person.setName("John Smith");
		
		Address address1 = makeAddress("Willis st 72");
			address1.setOwner(null);
			person.setCurrentAddress(address1);
		
		Address address2 = makeAddress("Mulgrave st 24");
			address2.setOwner(person);
			person.getPreviousAddresses().add(address2);
			
		Address address3 = makeAddress("Featherston st 49");
			address3.setOwner(person);
			person.getPreviousAddresses().add(address3);
			
		Address address4 = makeAddress("Woodward st 49");
			address4.setOwner(person);	
			person.getPreviousAddresses().add(address4);
				
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(address4);
		
		WritePacket firstWrite = batch.getNext();
		assertEquals(address1, firstWrite.getEntity());
		
		firstWrite.updateIDValue(99);
		assertEquals(99, address1.getId().intValue());
		
		WritePacket secondWrite = batch.getNext();
		assertEquals(person, secondWrite.getEntity());
		secondWrite.updateIDValue(77);
		
		assertEquals(77, person.getId().intValue());
		
		WritePacket thirdWrite = batch.getNext();
		assertNotNull(thirdWrite);
		assertEquals(77, thirdWrite.getByColumnName("\"OWNER_ID\"").value);
		
		assertNotNull(batch.getNext());
		
		assertNotNull(batch.getNext());
		
		assertNull(batch.getNext());
	}
	
	private static Address makeAddress(String street){
		Address rez = new Address();
			rez.setCity("Wellington");
			rez.setCountry("New Zealand");
			rez.setStreet(street);
		return rez;
	}	
	
}
