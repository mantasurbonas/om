package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gugit.om.OM;
import gugit.om.mapping.EntityWritePacket;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WriteContext;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;
import gugit.om.wrapping.EntityFactoryImpl;
import gugit.om.wrapping.EntityMarkingHelper;
import gugit.om.wrapping.IEntityFactory;
import gugit.services.EntityServiceFacade;

public class DirtyMarkingTest {

	@Test
	public void testEntityMarkingHelper() {
		Person person = new Person();
		assertTrue(EntityMarkingHelper.isDirty(person));
		EntityMarkingHelper.setDirty(person, false);
		assertTrue(EntityMarkingHelper.isDirty(person));
		
		IEntityFactory entityFactory = new EntityFactoryImpl();
		Person person2 = entityFactory.create(Person.class);
		assertTrue(EntityMarkingHelper.isDirty(person2));
		EntityMarkingHelper.setDirty(person2, false);
		assertFalse(EntityMarkingHelper.isDirty(person2));		
	}
	
	@Test
	public void testWriteBatchMarksDirty(){
		EntityServiceFacade service = new EntityServiceFacade();
				
		Person person = service.create(Person.class);
		person.setId(789);

		WriteBatch writeBatch = new WriteBatch(service);
		WriteContext writeContext = new WriteContext(service);
		service.getSerializerFor(Person.class).write(person, writeBatch, writeContext);
		
		assertEquals(((EntityWritePacket)writeBatch.getNext()).getEntity(), person);
		
		writeBatch = new WriteBatch(service);
		service.getSerializerFor(Person.class).write(person, writeBatch, writeContext);
		
		assertNull(writeBatch.getNext());
	}
	
	@Test
	public void testReadEntitiesAreNotDirty(){
		OM om = new OM();
		
		Person person = om.readEntity(new Object[]{14, "John Smith"}, Person.class);
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.getName();
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.setName("Whatever");
		assertTrue(EntityMarkingHelper.isDirty(person));
	}
	
	@Test
	public void testReadWriteLifecycle(){
		OM om = new OM();
		
		Person person1 = om.readEntity(new Object[]{14, "John Smith"}, Person.class);
		Person person2 = om.readEntity(new Object[]{15, "Samuel Johnson"}, Person.class);
		
		person2.setName("Samuel J. Johnson");
		
		WriteBatch batch = om.writeEntity(person1);
		om.writeEntity(person2, batch);
		
		assertEquals(((EntityWritePacket)batch.getNext()).getEntity(), person2);
		assertNull(batch.getNext());
		
		person2.setName("John Dilbert");
		
		batch = om.writeEntity(person2);
		assertEquals(((EntityWritePacket)batch.getNext()).getEntity(), person2);
		
		batch = om.writeEntity(person2);
		assertNull(batch.getNext());		
	}
	
	@Test
	public void testWritingCompositeEntities(){
		EntityServiceFacade service = new EntityServiceFacade();

		Address address = service.create(Address.class);
		address.setCity("Darwin");
		
		Person person = service.create(Person.class);
		person.setId(789);
		person.getPreviousAddresses().add(address);
		address.setOwner(person);

		WriteBatch writeBatch = new WriteBatch(service);
		WriteContext writeContext = new WriteContext(service);
		service.getSerializerFor(Person.class).write(person, writeBatch, writeContext);
		
		assertEquals(((EntityWritePacket)writeBatch.getNext()).getEntity(), person);
		assertEquals(((EntityWritePacket)writeBatch.getNext()).getEntity(), address);
		assertNull(writeBatch.getNext());
		
		writeBatch = new WriteBatch(service);
		service.getSerializerFor(Person.class).write(person, writeBatch, writeContext);
		
		assertNull(writeBatch.getNext());
	}
	
	@Test
	public void testMasterRef(){
		
		OM om = new OM();
		EntityServiceFacade service = new EntityServiceFacade();
		Person person = om.readEntity(new Object[]{14, "John Smith"}, Person.class);
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		Address address = service.create(Address.class);
		address.setOwner(person);
		assertTrue(EntityMarkingHelper.isDirty(address));
		
		WriteBatch batch = om.writeEntity(person);
		assertNull(batch.getNext());
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		batch = om.writeEntity(address);
		assertEquals(((EntityWritePacket)batch.getNext()).getEntity(), address);
		Object obj = batch.getNext();
		assertNull(obj);
	}

}
