package gugit.om.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gugit.om.InsertData;
import gugit.om.OM;
import gugit.om.WriteBatch;
import gugit.om.annotations.Column;
import gugit.om.annotations.DetailEntity;
import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class RecursiveTest {

	@Entity(name="RECURSIVE")
	public static class Recursive{
		@ID(name="ID")
		public Integer id;
		
		@Column(name="LABEL")
		public String label;
		
		@DetailEntity(myProperty="id", detailColumn="PARENT_ID")
		public Recursive recursive;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Recursive getRecursive() {
			return recursive;
		}

		public void setRecursive(Recursive recursive) {
			this.recursive = recursive;
		}
		
		public String toString(){
			return "Recursive #"+getId()
						+" '"+getLabel()+"' "
						+getRecursive();
		}
	}
	
	@Test
	public void testWritingRecursiveEntities() {
		Recursive rec1 = new Recursive();
			rec1.setLabel("label1");
			
		Recursive rec2 = new Recursive();
			rec2.setLabel("label2");
			rec2.setRecursive(rec1);
			
		Recursive rec3 = new Recursive();
			rec3.setLabel("label3");
			rec3.setRecursive(rec2);
			
		WriteBatch batch = new OM<Recursive>(Recursive.class).writeEntity(rec3);
		
		List<InsertData<?>> inserts = batch.getAllInserts(Recursive.class);
		
		assertEquals(3, inserts.size());
		assertEquals(rec3.getLabel(), inserts.get(2).get("LABEL"));
	}
	
	@Test
	public void testReadingRecursiveEntities(){
		Object[] resultset = new Object[]{1, "rec1", 2, "rec2", 3, "rec3"};
		
		Recursive entity = new OM<Recursive>(Recursive.class).readEntity(resultset);
		
		assertEquals(resultset[0], entity.getId().intValue());
		assertEquals(resultset[1], entity.getLabel());
		assertNotNull(entity.getRecursive());
		
		assertEquals(resultset[2], entity.getRecursive().getId());
		assertEquals(resultset[3], entity.getRecursive().getLabel());
		assertNotNull(entity.getRecursive().getRecursive());
		
		assertEquals(resultset[4], entity.getRecursive().getRecursive().getId());
		assertEquals(resultset[5], entity.getRecursive().getRecursive().getLabel());
		assertNull(entity.getRecursive().getRecursive().getRecursive());
	}

	@Test
	public void testReadingMultipleRecursiveEntities(){
		List<Object[]> resultset = new LinkedList<Object[]>();
			resultset.add(new Object[]{1, "rec1", 2, "rec2", 3, "rec3"});
			resultset.add(new Object[]{1, "rec4", 5, "rec5", 6, "rec6"});
			resultset.add(new Object[]{7, "rec7", 8, "rec8", 9, "rec9"});
			resultset.add(new Object[]{10, "rec10", 11, "rec11", 9, "rec9"});
			
		LinkedList<Recursive> entities = new OM<Recursive>(Recursive.class).readEntities(resultset);
		
		assertEquals(3, entities.size());
		
		assertEquals(1, entities.get(0).getId().intValue());
		assertEquals("rec3", entities.get(0).getRecursive().getRecursive().getLabel());
		
		assertEquals(7, entities.get(1).getId().intValue());
		assertEquals("rec9", entities.get(1).getRecursive().getRecursive().getLabel());
		
		assertEquals(10, entities.get(2).getId().intValue());
		assertEquals("rec9", entities.get(2).getRecursive().getRecursive().getLabel());
		assertTrue(entities.get(1).getRecursive().getRecursive() == entities.get(2).getRecursive().getRecursive());
	}
	
}
