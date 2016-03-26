package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.mapping.ISerializer;
import gugit.om.mapping.ISerializerFactory;
import gugit.om.mapping.ReadContext;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;
import gugit.om.test.model.SimpleAddress;
import gugit.om.utils.ArrayIterator;
import gugit.om.utils.IDataIterator;
import gugit.services.EntityServiceFacade;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MergeTest {

	@Test
	public void testGetId() {
		SimpleAddress address = new SimpleAddress();
		address.setId(999);
		Object id = new EntityServiceFacade().getSerializerFor(SimpleAddress.class).getID(address);
		
		assertEquals(id, address.getId());
	}

	
	@Test
	public void testGetProperty(){
		ISerializer<SimpleAddress> serializer = new EntityServiceFacade().getSerializerFor(SimpleAddress.class);
		int idIdx = serializer.getPropertyIndex("id");
		assertEquals(0, idIdx);
		
		int personIdx = serializer.getPropertyIndex("person");
		assertEquals(4, personIdx);
		
		int streetIdx = serializer.getPropertyIndex("street");
		assertEquals(3, streetIdx);
	}
	
	@Test
	public void testMergeSinglePojo(){
		ISerializer<Person> serializer = new EntityServiceFacade().getSerializerFor(Person.class);
		
		Person person = new Person();
			person.setId(33);
			
		Address currentAddress = new Address();
			currentAddress.setId(55);
			currentAddress.setCountry("Canada");
		
			person.setCurrentAddress(currentAddress);
		
		ISerializerFactory serializers = new EntityServiceFacade();
		int propIndex = serializer.getPropertyIndex("currentAddress");
		IDataIterator<Object> array = new ArrayIterator<Object>(new Object[]
				{33, 44, "New Zealand", "Nelson", "Elm st 142", 33});
		serializer.leftJoin(person, propIndex , array , 0, new ReadContext(serializers ));
		
		assertEquals(person.getCurrentAddress().getId().intValue(), 44);
		assertEquals(person.getCurrentAddress().getCity(), "Nelson");
		assertEquals(person.getCurrentAddress().getOwner(), person);
		assertEquals(currentAddress.getId().intValue(), 55);
	}
	
	@Test
	public void testMergePojoCollection(){
		ISerializer<Person> serializer = new EntityServiceFacade().getSerializerFor(Person.class);
		
		Person person = new Person();
			person.setId(33);
		
		ISerializerFactory serializers = new EntityServiceFacade();
		int propIndex = serializer.getPropertyIndex("previousAddresses");
		
		ArrayIterator<Object> array = new ArrayIterator<Object>();
			array.setData(new Object[]{33, 44, "New Zealand", "Nelson", "Elm st 142", 33});
			
		ReadContext readContext = new ReadContext(serializers);
		serializer.leftJoin(person, propIndex , array , 0, readContext);
		
		assertEquals(person.getPreviousAddresses().size(), 1);
		assertEquals(person.getPreviousAddresses().get(0).getId().intValue(), 44);
		assertEquals(person.getPreviousAddresses().get(0).getCity(), "Nelson");
		assertEquals(person.getPreviousAddresses().get(0).getOwner(), person);
		
		array.setData(new Object[]{33, 45, "New Zealand", "Wellington", "Ocean st 241", 33});
		serializer.leftJoin(person, propIndex , array , 0, readContext);
		
		assertEquals(person.getPreviousAddresses().size(), 2);
		assertEquals(person.getPreviousAddresses().get(1).getId().intValue(), 45);
		assertEquals(person.getPreviousAddresses().get(1).getCity(), "Wellington");
		assertEquals(person.getPreviousAddresses().get(1).getOwner(), person);

		array.setData(new Object[]{33, 45, "United Kingdom", "London", "Briston av 555", 33});
		serializer.leftJoin(person, propIndex , array , 0, readContext);

		assertEquals(person.getPreviousAddresses().size(), 2);
	}
	
	@Test
	public void testListMerging(){
		OM om = new OM();
		
		List<Object[]> rows = new ArrayList<Object[]>();
		rows.add(new Object[]{11, "John Johnson", null});
		rows.add(new Object[]{22, "Peter Peterson", null});
		rows.add(new Object[]{33, "Jim Jameson", null});
	
		List<Person> entities = om.readEntities(rows , Person.class);
		
		assertEquals(entities.size(), 3);

		
		List<Object[]> joinRows = new ArrayList<Object[]>();
		joinRows.add(new Object[]{11, 100, "New Zealand", "Wellington", "Cobbler st 925", 11});
		joinRows.add(new Object[]{11, 101, "New Zealand", "Nelson", "Elm st 42", 11});
		joinRows.add(new Object[]{33, 102, "Australia", "Darwin", "Ocean st 887", 33});
		
		om.leftJoin(entities, "previousAddresses", joinRows);
		
		assertEquals(entities.size(), 3);
		assertEquals(entities.get(0).getPreviousAddresses().size(), 2);
		assertTrue(entities.get(1).getPreviousAddresses().isEmpty());
		assertEquals(entities.get(2).getPreviousAddresses().size(), 1);
		assertEquals(entities.get(2).getPreviousAddresses().get(0).getCity(), "Darwin");
	}
}
