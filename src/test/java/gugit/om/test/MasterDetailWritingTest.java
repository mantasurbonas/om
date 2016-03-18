package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.mapping.NullWriteValue;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacket;
import gugit.om.mapping.WritePacketElement;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;

import java.util.List;

import org.junit.Test;

public class MasterDetailWritingTest {
	
	@Test
	public void testInsertIncompleteMaster() {
		Person person = new Person();
		person.setName("John Smith");
		
		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		WritePacket insertData = batch.getNext();
		assertEquals(person.getName(), getValueByName(insertData.getElements(), "NAME"));
		
		insertData = batch.getNext();
		assertNull(insertData);
	}

	@Test
	public void testUpdateIncompleteMaster(){
		Person person = new Person();
		person.setId(456);

		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		WritePacket updateData = batch.getNext();
		assertEquals(NullWriteValue.getInstance(), getValueByName(updateData.getElements(), "NAME"));
		assertEquals(person.getId(), updateData.getIdElement().value);
		
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
			
		person.setCurrentAddress(address);
		
		
		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		WritePacket insertData = batch.getNext();
		assertEquals(insertData.getEntity(), person); // person must be persisted before person so that it could use person's ID 
		
		insertData = batch.getNext();
		assertEquals(insertData.getEntity(), address); //
		assertEquals(address.getOwner().getId(), getValueByName(insertData.getElements(), "OWNER_ID"));
		
		insertData = batch.getNext();
		assertNull(insertData);
	}
	
	@Test
	public void testMasterDetailWriting() {
		
		Person person = new Person();
			person.setName("John Smith");
		
		Address address1 = makeAddress("Willis st 72");
			address1.setId(5);
			address1.setOwner(person);
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
				
		WriteBatch batch = new OM<Address>(Address.class).writeEntity(address4);
		
		WritePacket personWrite = batch.getNext();
		assertEquals(person, personWrite.getEntity());
		
		personWrite.updateIDValue(77);
		assertEquals(77, person.getId().intValue());
		
		WritePacket addressWrite = batch.getNext();
		assertEquals(Address.class, addressWrite.getEntity().getClass());
		assertEquals(77, getValueByName(addressWrite.getElements(), "OWNER_ID"));
		
		assertNotNull(batch.getNext());
		
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
	
	private Object getValueByName(List<WritePacketElement> writePackElements, final String columnName) {
		for (WritePacketElement e: writePackElements)
			if (e.columnName.equalsIgnoreCase(columnName))
				return e.value;
		return null;
	}

}
