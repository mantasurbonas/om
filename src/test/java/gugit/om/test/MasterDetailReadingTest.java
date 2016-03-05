package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.test.model.Person;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class MasterDetailReadingTest {

	@Test
	public void testOneToOneMapping() {
		List<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{14, "John Smith", 123, "New Zealand", "Nelson", "Ocean str 147/13", null, null, null, null });
			
		LinkedList<Person> persons = new OM<Person>(Person.class).readEntities(resultset );
		
		assertEquals(1, persons.size());
		assertEquals(14, persons.get(0).id.intValue());
		assertEquals(123, persons.get(0).currentAddress.id.intValue());
		assertEquals(0, persons.get(0).previousAddresses.size());
	}

	@Test
	public void testOneToManyMapping(){
		List<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{14, "John Smith",    null, null, null, null, 123, "New Zealand", "Nelson", "Ocean str 147/13"});
			resultset.add(new Object[]{14, "John Smith",    null, null, null, null, 124, "New Zealand", "Nelson", "Crimson av 56b"});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, 125, "UK", "Birmingham", "Wookie rd 48"});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, 126, "Australia", "Darwin", "Queensland rd 87/3"});
			resultset.add(new Object[]{15, "Peter Jameson", null, null, null, null, 127, "New Zealand", "Nelson", "Ocean str 147/13"});
			
		LinkedList<Person> persons = new OM<Person>(Person.class).readEntities(resultset );
		
		assertEquals(2, persons.size());
		assertEquals(14, persons.get(0).id.intValue());
		assertNull(persons.get(0).currentAddress);
		assertEquals(2, persons.get(0).previousAddresses.size());
		
		assertEquals(3, persons.get(1).previousAddresses.size());		
	}
}
