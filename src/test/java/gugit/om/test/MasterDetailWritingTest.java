package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import gugit.om.OM;
import gugit.om.test.helpers.WriteDestinationImpl;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;

import org.junit.Test;

public class MasterDetailWritingTest {
	
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
		
		WriteDestinationImpl destination = new WriteDestinationImpl(Person.class);
		WriteDestinationImpl.reset();
		
		new OM<Person>(Person.class)
			.writeEntity(person, destination );
		
		assertEquals(getInsertsRegistry(Person.class).size(), 1); // 1 new person
		assertEquals(getInsertsRegistry(Address.class).size(), 3); // 3 new addresses
		assertEquals(getUpdatesRegistry(Address.class).size(), 1); // 1 existing address
		assertNull(getUpdatesRegistry(Person.class)); // no existing person
		
		assertEquals(getUpdatesRegistry(Address.class).get(0).get("STREET"), "Featherston st 49");
		
		//describe(destination);
	}

	private static List<Map<String, Object>> getInsertsRegistry(Class<?> type) {
		return WriteDestinationImpl.insertsRegistry.get(type);
	}

	private List<Map<String, Object>> getUpdatesRegistry(Class<?> type) {
		return WriteDestinationImpl.updatesRegistry.get(type);
	}
	
	private static Address makeAddress(String street){
		Address rez = new Address();
			rez.setCity("Wellington");
			rez.setCountry("New Zealand");
			rez.setStreet(street);
		return rez;
	}	

	
//	private void describe(WriteDestinationImpl destination) {
//		
//		for (Class<?> clazz: WriteDestinationImpl.insertsRegistry.keySet()){
//			System.out.println("insert "+clazz.getSimpleName()+":");
//			System.out.println(WriteDestinationImpl.insertsRegistry.get(clazz));
//		}
//		
//		for (Class<?> clazz: WriteDestinationImpl.updatesRegistry.keySet()){
//			System.out.println("update "+clazz.getSimpleName()+":");
//			System.out.println(WriteDestinationImpl.updatesRegistry.get(clazz));
//		}
//	}

}
