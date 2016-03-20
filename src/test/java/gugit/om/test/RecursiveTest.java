package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gugit.om.OM;
import gugit.om.mapping.WriteBatch;
import gugit.om.mapping.WritePacket;
import gugit.om.metadata.EntityMetadataService;
import gugit.om.test.model.Recursive;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class RecursiveTest {

	
	@Test
	public void testWritingRecursiveEntities() {
		Recursive rec1 = new Recursive();
			rec1.setLabel("label1");
			
		Recursive rec2 = new Recursive();
			rec2.setLabel("label2");
			rec2.setParent(rec1);
			
			rec1.setChild(rec2);
			
		Recursive rec3 = new Recursive();
			rec3.setLabel("label3");
			rec3.setParent(rec2);
			
			rec2.setChild(rec3);
			
			
		EntityMetadataService metadataService = new EntityMetadataService();
		WriteBatch batch = new OM<Recursive>(metadataService, Recursive.class).writeEntity(rec3);
		
		WritePacket writePacket = batch.getNext();
		
		assertNotNull(writePacket);
		assertEquals(rec1.getLabel(), writePacket.getByColumnName("LABEL").value);
		
		writePacket.updateIDValue(100);
		
		writePacket = batch.getNext();
		assertNotNull(writePacket);
		assertEquals(rec2.getLabel(), writePacket.getByColumnName("LABEL").value);
		assertEquals(rec2.getParent().getId(), writePacket.getByColumnName("PARENT_ID").value);

		writePacket.updateIDValue(101);
		
		writePacket = batch.getNext();
		assertNotNull(writePacket);
		assertEquals(rec3.getLabel(), writePacket.getByColumnName("LABEL").value);
		assertEquals(rec3.getParent().getId(), writePacket.getByColumnName("PARENT_ID").value);
		
		assertNull(batch.getNext());
	}
	
	@Test
	public void testReadingRecursiveEntities(){
		Object[] resultset = new Object[]{1, "rec1", null, 2, "rec2", 1, 3, "rec3", 2};
		
		EntityMetadataService metadataService = new EntityMetadataService();
		Recursive entity = new OM<Recursive>(metadataService, Recursive.class).readEntity(resultset);
		
		assertEquals(resultset[0], entity.getId().intValue());
		assertEquals(resultset[1], entity.getLabel());
		assertNotNull(entity.getChild());
		assertNull(entity.getParent());
		
		assertEquals(resultset[3], entity.getChild().getId());
		assertEquals(resultset[4], entity.getChild().getLabel());
		assertNotNull(entity.getChild().getChild());
		assertNotNull(entity.getChild().getParent());
		assertEquals(entity, entity.getChild().getParent());
		
		assertEquals(resultset[6], entity.getChild().getChild().getId());
		assertEquals(resultset[7], entity.getChild().getChild().getLabel());
		assertNull(entity.getChild().getChild().getChild());
	}

	@Test
	public void testReadingMultipleRecursiveEntities(){
		List<Object[]> resultset = new LinkedList<Object[]>();
			resultset.add(new Object[]{1, "rec1", null, 2, "rec2", 1, 3, "rec3", 2});
			resultset.add(new Object[]{1, "rec4", null, 5, "rec5", 1, 6, "rec6", 5});
			resultset.add(new Object[]{7, "rec7", null, 8, "rec8", 7, 9, "rec9", 8});
			resultset.add(new Object[]{10,"rec10",null, 11,"rec11",10,9, "rec9", 8});
			
			
		EntityMetadataService metadataService = new EntityMetadataService();
		LinkedList<Recursive> entities = new OM<Recursive>(metadataService, Recursive.class).readEntities(resultset);
		
		assertEquals(3, entities.size());
		
		assertEquals(1, entities.get(0).getId().intValue());
		assertEquals("rec3", entities.get(0).getChild().getChild().getLabel());
		
		assertEquals(7, entities.get(1).getId().intValue());
		assertEquals("rec9", entities.get(1).getChild().getChild().getLabel());
		
		assertEquals(10, entities.get(2).getId().intValue());
		assertEquals("rec9", entities.get(2).getChild().getChild().getLabel());
		assertEquals(entities.get(1).getChild().getChild(), entities.get(2).getChild().getChild());
	}
	
}
