package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gugit.om.InsertData;
import gugit.om.OM;
import gugit.om.PersistInfoRegistry;
import gugit.om.UpdateData;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;

import java.util.List;

import org.junit.Test;

public class MasterDetailWritingTest {
	
	@Test
	public void testInsertIncompleteMaster() {
		Person person = new Person();
		person.setName("John Smith");
		
		PersistInfoRegistry repository = new PersistInfoRegistry();
		new OM<Person>(Person.class).writeEntity(person, repository);
		
		List<InsertData<?>> personInserts = repository.getInserts(Person.class);
		assertEquals(personInserts.size(), 1);
		assertEquals(personInserts.get(0).get("NAME"), person.getName());
		
		List<InsertData<?>> addressInserts = repository.getInserts(Address.class);
		assertNull(addressInserts);
	}

	@Test
	public void testUpdateIncompleteMaster(){
		Person person = new Person();
		person.setId(456);
		
		PersistInfoRegistry registry = new PersistInfoRegistry();
		new OM<Person>(Person.class).writeEntity(person, registry);
		
		List<UpdateData<?>> updates = registry.getUpdates(Person.class);
		assertEquals(updates.size(), 1);
		assertNull(updates.get(0).get("NAME"));
		assertEquals(person.getId(), updates.get(0).getIdValue());
	}
	
	@Test
	public void testInsertMasterDetail(){
		Address address = new Address();
			address.setCountry("Antarctica");

		Person person = new Person();
			person.setName("John Smith");
			person.setCurrentAddress(address);
		
		PersistInfoRegistry registry = new PersistInfoRegistry();
		new OM<Person>(Person.class).writeEntity(person, registry );
		
		List<InsertData<?>> personInserts = registry.getInserts(Person.class);
		assertEquals(personInserts.size(), 1);
		
		List<InsertData<?>> addressInserts = registry.getInserts(Address.class);
		assertEquals(addressInserts.size(), 1);
		assertEquals(addressInserts.get(0).get("COUNTRY"), address.getCountry());
		assertNull(addressInserts.get(0).get("CITY"));
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
		
		
		PersistInfoRegistry registry = new PersistInfoRegistry();
		new OM<Person>(Person.class)
			.writeEntity(person, registry );
		
		assertEquals(registry.getInserts(Person.class).size(), 1); // 1 new person
		assertEquals(registry.getInserts(Address.class).size(), 3); // 3 new addresses
		assertEquals(registry.getInserts(Address.class).get(0).get("STREET"), address1.getStreet());
		assertEquals(registry.getInserts(Address.class).get(2).get("STREET"), address4.getStreet());

		assertNull(registry.getUpdates(Person.class)); // no existing person

		assertEquals(registry.getUpdates(Address.class).size(), 1); // 1 existing address
		assertEquals(registry.getUpdates(Address.class).get(0).getIdValue(), address3.getId());
		assertEquals(registry.getUpdates(Address.class).get(0).get("STREET"), address3.getStreet());
	}
	
	private static Address makeAddress(String street){
		Address rez = new Address();
			rez.setCity("Wellington");
			rez.setCountry("New Zealand");
			rez.setStreet(street);
		return rez;
	}	

}
