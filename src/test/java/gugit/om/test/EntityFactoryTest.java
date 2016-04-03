package gugit.om.test;

import static org.junit.Assert.*;

import org.junit.Test;

import gugit.om.test.model.Person;
import gugit.om.wrapping.EntityFactoryImpl;
import gugit.om.wrapping.EntityMarkingHelper;
import gugit.om.wrapping.IWrappedEntity;

public class EntityFactoryTest {

	@Test
	public void testEntityUnwrapping() {
		EntityFactoryImpl factory = new EntityFactoryImpl();
		Person person = factory.create(Person.class);
		assertTrue(person instanceof IWrappedEntity);
		
		assertNotEquals(person.getClass(), Person.class);
		assertEquals(EntityMarkingHelper.getEntityClass(person.getClass()), Person.class);
	}

	@Test
	public void testManagedEntityDirtyFlags(){
		EntityFactoryImpl factory = new EntityFactoryImpl();
		
		Person person = factory.create(Person.class);
		assertTrue(EntityMarkingHelper.isDirty(person));
		
		EntityMarkingHelper.setDirty(person, false);
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.getName();
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.getId();
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.setName("nnn");
		assertTrue(EntityMarkingHelper.isDirty(person));
		
		EntityMarkingHelper.setDirty(person, false);
		assertFalse(EntityMarkingHelper.isDirty(person));
		
		person.getPreviousAddresses();
		assertTrue(EntityMarkingHelper.isDirty(person));
	}
}
