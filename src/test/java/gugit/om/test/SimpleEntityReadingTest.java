package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gugit.om.OM;
import gugit.om.annotations.Column;
import gugit.om.annotations.ID;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

public class SimpleEntityReadingTest {
	
	public static class Address{
		@ID(name="ID")
		public Integer id;
		
		@Column(name="COLUMN")
		public String country;
		
		@Column(name="CITY")
		public String city;
		
		@Column(name="STREET")
		public String street;
		
		@Column(name="PERSON")
		public String person;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getPerson() {
			return person;
		}

		public void setPerson(String person) {
			this.person = person;
		}
		
	}
	
	@Test
	public void testReadOneEntity() {
		Object row[] = new Object[]{
			123, "New Zealand", "Wellington", "London str 42B", null
		};
		
		Address address = new OM<Address>(Address.class).readEntity(row);
		
		assertEquals("failed deserializing id", 123, address.id.intValue());
		assertEquals("failed deserializing entity field", address.street, row[3]);
		assertNull(address.getPerson());
	}

	@Test
	public void testReadSeveralEntities(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
		
		LinkedList<Address> entities = new OM<Address>(Address.class).readEntities(resultset);
		
		assertEquals(entities.size(), 2);
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
	
	@Test
	public void testReadReadEntitiesWithIdenticalIDs(){
		ArrayList<Object[]> resultset = new ArrayList<Object[]>();
			resultset.add(new Object[]{ 123, "New Zealand", "Wellington", "London str 42B", null });
			resultset.add(new Object[]{ 456, "New Zealand", "Nelson", "Oakfield av 16/2", null });
			resultset.add(new Object[]{ 456, "Australia", "Darwin", "River str 94/5", null });
		
		LinkedList<Address> entities = new OM<Address>(Address.class).readEntities(resultset);

		assertEquals(2, entities.size());
		assertEquals(entities.get(0).id.intValue(), 123);
		assertEquals(entities.get(1).street, "Oakfield av 16/2");
		assertNull(entities.get(0).getPerson());
	}
}
