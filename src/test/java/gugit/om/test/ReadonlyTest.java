package gugit.om.test;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import gugit.om.mapping.EntityWritePacket;
import gugit.om.mapping.WriteBatch;
import gugit.om.test.model.ReadonlyEntity;
import gugit.om.test.utils.TestUtils;

public class ReadonlyTest {

	@Test
	public void testNoWrites() {
		ReadonlyEntity entity = new ReadonlyEntity();
		
		WriteBatch batch = TestUtils.createObjectMapper().writeEntity(entity);
		
		EntityWritePacket insertData = (EntityWritePacket)batch.getNext();
		assertNull(insertData);
	}

}
