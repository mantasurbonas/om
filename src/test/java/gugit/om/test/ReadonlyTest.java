package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import gugit.om.mapping.EntityWritePacket;
import gugit.om.mapping.WriteBatch;
import gugit.om.test.model.ReadonlyEntity;
import gugit.om.test.model.ReadonlyParentEntity;
import gugit.om.test.utils.TestUtils;

public class ReadonlyTest {

	@Test
	public void testNoWrites() {
		ReadonlyEntity entity = new ReadonlyEntity();
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(entity);
		
		EntityWritePacket insertData = (EntityWritePacket)batch.getNext();
		assertNull(insertData);
	}

	@Test
	public void testParentWriteableChildReadonly() {
		ReadonlyEntity entity = new ReadonlyEntity();
		
		ReadonlyParentEntity parent = new ReadonlyParentEntity();
			parent.setLabel("label");
			parent.setReadonly(entity);
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(parent);
		
		EntityWritePacket insertData = (EntityWritePacket)batch.getNext();
		assertNotNull(insertData);
		assertNull(insertData.getIdElement().value);
		
		assertEquals(insertData.getByColumnName("label").value, "label");

		insertData = (EntityWritePacket)batch.getNext();
		assertNull(insertData);
	}
	
}
