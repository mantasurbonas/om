package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gugit.om.InsertData;
import gugit.om.OM;
import gugit.om.WriteBatch;
import gugit.om.UpdateData;
import gugit.om.mapping.NullWriteValue;
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
		
		List<InsertData<?>> personInserts = batch.getInserts(Person.class);
		assertEquals(personInserts.size(), 1);
		assertEquals(personInserts.get(0).get("NAME"), person.getName());
		
		List<InsertData<?>> addressInserts = batch.getInserts(Address.class);
		assertNull(addressInserts);
	}

	@Test
	public void testUpdateIncompleteMaster(){
		Person person = new Person();
		person.setId(456);
		
		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		List<UpdateData<?>> updates = batch.getUpdates(Person.class);
		assertEquals(updates.size(), 1);
		assertEquals(updates.get(0).get("NAME"), NullWriteValue.instance());
		assertEquals(person.getId(), updates.get(0).getIdValue());
	}
	
	@Test
	public void testInsertMasterDetail(){
		Address address = new Address();
			address.setCountry("Antarctica");

		Person person = new Person();
			person.setName("John Smith");
			person.setCurrentAddress(address);
		
		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		List<InsertData<?>> personInserts = batch.getInserts(Person.class);
		assertEquals(personInserts.size(), 1);
		
		List<InsertData<?>> addressInserts = batch.getInserts(Address.class);
		assertEquals(addressInserts.size(), 1);
		assertEquals(addressInserts.get(0).get("COUNTRY"), address.getCountry());
		assertEquals(addressInserts.get(0).get("CITY"), NullWriteValue.instance());
	}
	
	@Test
	public void testMasterDetailWriting() {
		
		Address address1 = makeAddress("Willis st 72");
		Address address2 = makeAddress("Mulgrave st 24");
		Address address3 = makeAddress("Featherston st 49");
			address3.setId(5);
		Address address4 = makeAddress("Woodward st 49");
		
		Person person = new Person();
			person.setName("John Smith");
			person.setCurrentAddress(address1);
			person.getPreviousAddresses().add(address2);
			person.getPreviousAddresses().add(address3);
			person.getPreviousAddresses().add(address4);
		
		
		WriteBatch batch = new OM<Person>(Person.class).writeEntity(person);
		
		assertEquals(batch.getInserts(Person.class).size(), 1); // 1 new person
		assertEquals(batch.getInserts(Address.class).size(), 3); // 3 new addresses
		assertEquals(batch.getInserts(Address.class).get(0).get("STREET"), address1.getStreet());
		assertEquals(batch.getInserts(Address.class).get(2).get("STREET"), address4.getStreet());

		assertNull(batch.getUpdates(Person.class)); // no existing person

		assertEquals(batch.getUpdates(Address.class).size(), 1); // 1 existing address
		assertEquals(batch.getUpdates(Address.class).get(0).getIdValue(), address3.getId());
		assertEquals(batch.getUpdates(Address.class).get(0).get("STREET"), address3.getStreet());
	}
	
	private static Address makeAddress(String street){
		Address rez = new Address();
			rez.setCity("Wellington");
			rez.setCountry("New Zealand");
			rez.setStreet(street);
		return rez;
	}	

}
