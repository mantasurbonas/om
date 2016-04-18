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
import gugit.om.mapping.M2MWritePacket;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacketElement;
import gugit.om.test.model.A;
import gugit.om.test.model.B;
import gugit.om.wrapping.EntityMarkingHelper;
import gugit.services.EntityServiceFacade;

public class ManyToManyTest {

	private static class AIdAccessor implements IPropertyAccessor<A, Integer>{
		public void setValue(A entity, Integer value) {  entity.setId(value);		}
		public Integer getValue(A entity) {  return entity.getId();		} 
	};
	
	private static class BIdAccessor implements IPropertyAccessor<B, Integer>{
		public void setValue(B entity, Integer value) {  entity.setId(value); }
		public Integer getValue(B entity) {  return entity.getId();		}
	};
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDependencyResolution() {
		A aEntity = new A();
		
		IPropertyAccessor<A, Integer> aIdAccessor = new AIdAccessor();
		
		IPropertyAccessor<B, Integer> bIdAccessor = new BIdAccessor();
		
		M2MWritePacket writePacket = new M2MWritePacket("A_TO_B");
		writePacket.setLeftSideDependency("A_ID", "a", aEntity, aIdAccessor);
		writePacket.setRightSideDependency("B_ID", "b", null, bIdAccessor, "B_TABLE", "ID"); 
		
		assertTrue(writePacket.trySolveDependencies());
		
		Collection<B> bEntities = new LinkedList<B>();
		writePacket.setRightSideDependency("B_ID", "b", bEntities, bIdAccessor, "B_TABLE", "ID");
		
		assertTrue(writePacket.trySolveDependencies());

		B b1 = new B();
		B b2 = new B();
		bEntities.add(b1);
		bEntities.add(b2);
		
		assertFalse(writePacket.trySolveDependencies());
		
		aEntity.setId(1);
		assertFalse(writePacket.trySolveDependencies());
		
		b1.setId(2);
		assertFalse(writePacket.trySolveDependencies());
		
		b2.setId(3);
		assertTrue(writePacket.trySolveDependencies());
		
		WritePacketElement aElement = writePacket.getByFieldName("a");
		assertEquals(aElement.value, aEntity.getId());
		
		WritePacketElement bElement = writePacket.getByFieldName("b");
		
		Collection<Integer> bElementValues = (Collection<Integer>)bElement.value;
		assertEquals(bElementValues.size(), bEntities.size());
		
		assertEquals(bElementValues.iterator().next(), b1.getId());
	}
	
	@Test
	public void testManyToManyReading(){
		OM om = new OM();
		A a = om.readEntity(new Object[]{1, 2}, A.class);
		assertEquals(a.getId().intValue(), 1);
		
		assertEquals(a.getBees().size(), 1);
	}

	@SuppressWarnings("unchecked")
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
		M2MWritePacket m2mWrite = (M2MWritePacket)batch.getNext();
		assertNotNull(m2mWrite);
		
		WritePacketElement bindingToA = m2mWrite.getByFieldName("A");
		assertEquals(bindingToA.value, a.getId());
		
		WritePacketElement bindingsToB = m2mWrite.getByFieldName("B");
		Collection<Integer> bValues = (Collection<Integer>)bindingsToB.value;
		assertEquals(bValues.size(), 2);
		
		// lastly, write batch should be empty by now
		assertNull(batch.getNext());
		
		// saving the already-persisted, unmodified object graph should cause zero writes to the database
		batch = om.writeEntity(a);
		assertNull(batch.getNext());
		
		// modifying any object down the graph should cause just a single write to the database
		b.touch();
		
		batch = om.writeEntity(a);
		packet = (EntityWritePacket)batch.getNext();
		assertNotNull(packet);
		assertEquals(packet.getEntity(), b);
		
		assertNull(batch.getNext());
	}
	
}
