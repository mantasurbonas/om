package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import gugit.om.OM;
import gugit.om.mapping.EntityWritePacket;
import gugit.om.mapping.IPropertyAccessor;
import gugit.om.mapping.IWritePacket;
import gugit.om.mapping.M2MWritePacket;
import gugit.om.mapping.M2MWritePacketElement;
import gugit.om.mapping.WriteBatch;
import gugit.om.test.model.A;
import gugit.om.test.model.B;
import gugit.om.wrapping.EntityMarkingHelper;
import gugit.services.EntityServiceFacade;

public class ManyToManyTest {

	@Test
	public void testDependencyResolution() {
		A a = new A();
		
		IPropertyAccessor<A, Integer> aIdAccessor = new IPropertyAccessor<A, Integer>(){
			public void setValue(A entity, Integer value) {
				entity.setId(value);
			}
			public Integer getValue(A entity) {
				return entity.getId();
			} 
		};
		
		IPropertyAccessor<B, Integer> bIdAccessor = new IPropertyAccessor<B, Integer>() {
			public void setValue(B entity, Integer value) {
				entity.setId(value);
			}
			public Integer getValue(B entity) {
				return entity.getId();
			}
		};
		
		M2MWritePacket writePacket = new M2MWritePacket("A_TO_B");
		writePacket.setLeftSideDependency("A_ID", "a", a, aIdAccessor);
		writePacket.setRightSideDependency("B_ID", "b", null, bIdAccessor); 
		
		assertTrue(writePacket.trySolveDependencies());
		
		Collection<B> entities = new LinkedList<B>();
		writePacket.setRightSideDependency("B_ID", "b", entities, bIdAccessor);
		
		assertTrue(writePacket.trySolveDependencies());

		B b1 = new B();
		B b2 = new B();
		entities.add(b1);
		entities.add(b2);
		
		assertFalse(writePacket.trySolveDependencies());
		
		a.setId(1);
		assertFalse(writePacket.trySolveDependencies());
		
		b1.setId(2);
		assertFalse(writePacket.trySolveDependencies());
		
		b2.setId(3);
		assertTrue(writePacket.trySolveDependencies());
		
		M2MWritePacketElement element = writePacket.getElement();
		assertEquals(element.rightSideValues.size(), entities.size());
		
		assertEquals(element.rightSideValues.iterator().next(), b1.getId());
	}
	
	@Test
	public void testManyToManyReading(){
		OM om = new OM();
		A a = om.readEntity(new Object[]{1, 2}, A.class);
		assertEquals(a.getId().intValue(), 1);
		
		assertEquals(a.getBees().size(), 1);
	}

	@Test
	public void testManyToManyWriting(){
		EntityServiceFacade entityService = new EntityServiceFacade();
		OM om = new OM();
		A a = om.readEntity(new Object[]{1, 2}, A.class);
		
		// writing an unmodified object graph back to database should cause zero writes to DB
		WriteBatch batch = om.writeEntity(a);
		assertNull(batch.getNext());
		
		// modifying a collection should cause DB writes
		B b = entityService.create(B.class);
		a.getBees().add(b);
		
		batch = om.writeEntity(a);
		EntityWritePacket packet = (EntityWritePacket)batch.getNext();
		
		// first to write should be the root entity
		assertNotNull(packet);
		assertEquals(packet.getEntity(), a);
		
		// second to write should be the newly added detail entity
		packet = (EntityWritePacket)batch.getNext();
		assertEquals(packet.getEntity(), b);
		
		packet.updateIDValue(16);
		assertEquals(b.getId().intValue(), 16);
		EntityMarkingHelper.setDirty(b, false);
		
		// finally, a many-to-many table should be updated with correct entries
		M2MWritePacket m2mPac = (M2MWritePacket)batch.getNext();
		assertNotNull(m2mPac);
		
		M2MWritePacketElement element = m2mPac.getElement();
		assertEquals(element.leftSideValue, a.getId());
		assertEquals(element.rightSideValues.size(), 2);
		
		assertNull(batch.getNext());
		
		// saving the already-persisted, unmodified object graph should cause zero writes to the database
		batch = om.writeEntity(a);
		assertNull(batch.getNext());
		
		// modifying any object down the graph should cause a single write to the database
		b.touch();
		
		batch = om.writeEntity(a);
		packet = (EntityWritePacket)batch.getNext();
		assertNotNull(packet);
		assertEquals(packet.getEntity(), b);
		
		assertNull(batch.getNext());
	}
	
}
