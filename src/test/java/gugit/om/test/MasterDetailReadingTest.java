package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gugit.om.mapping.IReader;
import gugit.om.mapping.ReadContext;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;
import gugit.om.test.utils.TestUtils;
import gugit.om.utils.IDataIterator;

public class MasterDetailReadingTest {

	@Test
	public void testOneToOneMapping() {
		List<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 14,					// Person::id 
										"John Smith", 		// Person::name
										
										123, 				// Person::currentAddress::id
										"New Zealand", 		// Person::currentAddress::country
										"Nelson", 			// Person::currentAddress::city
										"Ocean str 147/13", // Person::currentAddress::street
										14, 				// Person::currentAddress::owner
										
										null,				// Person::previousAddresses[0]::id 
										null,				// Person::previousAddresses[0]::country
										null, 				// Person::previousAddresses[0]::city
										null,				// Person::previousAddresses[0]::street 
										null				// Person::previousAddresses[0]::owner
										});
			
		List<Person> persons = TestUtils.createObjectMapper().readEntities(resultset, Person.class);
		
		assertEquals(1, persons.size());
		assertEquals(14, persons.get(0).getId().intValue());
		assertEquals(123, persons.get(0).getCurrentAddress().getId().intValue());
		assertEquals(0, persons.get(0).getPreviousAddresses().size());
		assertEquals(persons.get(0), persons.get(0).getCurrentAddress().getOwner());
	}

	@Test
	public void testOneToManyMapping(){
		List<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{14, "John Smith",    null, null, null, null, null, 123, "New Zealand", "Nelson", "Ocean str 147/13", 14});
			resultset.add(new Object[]{14, "John Smith",    null, null, null, null, null, 124, "New Zealand", "Nelson", "Crimson av 56b" , 14});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, null, 125, "UK", "Birmingham", "Wookie rd 48", 15});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, null, 126, "Australia", "Darwin", "Queensland rd 87/3", 15});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, null, 127, "New Zealand", "Nelson", "Ocean str 147/13", 15});
			
		List<Person> persons = TestUtils.createObjectMapper().readEntities(resultset, Person.class);
		
		assertEquals(2, persons.size());
		assertEquals(14, persons.get(0).getId().intValue());
		assertNull(persons.get(0).getCurrentAddress());
		assertEquals(2, persons.get(0).getPreviousAddresses().size());
		assertEquals(persons.get(0), persons.get(0).getPreviousAddresses().get(0).getOwner());
		
		assertEquals(3, persons.get(1).getPreviousAddresses().size());		
	}
	
	@Test
	public void testResetingDetailObject(){
		List<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{14, "John Smith",    null, null, null, null, null, 123, "New Zealand", "Nelson", "Ocean str 147/13", 14});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, null, 123, "UK", "Birmingham", "Wookie rd 48", 15});
			resultset.add(new Object[]{16, "Zack Bradigan", null, null, null, null, null, 123, "New Zealand", "Nelson", "Ocean str 147/13", 15});
		
		List<Person> persons = TestUtils.createObjectMapper().readEntities(resultset, Person.class);
		
		assertEquals(3, persons.size());
		
		Address address1 = persons.get(0).getPreviousAddresses().get(0);
		Address address2 = persons.get(1).getPreviousAddresses().get(0);
		
		assertNotEquals(address1, address2);
	}
	
}
