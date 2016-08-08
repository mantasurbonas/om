package gugit.om.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gugit.om.test.model.Farmer;
import gugit.om.test.utils.TestUtils;

public class MasterDetailWithNullsReadingTest {

	@Test
	public void testCarthesianProduct() {
		List<Object[]> resultset = new ArrayList<Object[]>();
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1002, "Third Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1003, "Fourth Duck"});

		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1002, "Third Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1003, "Fourth Duck"});

		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1002, "Third Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1003, "Fourth Duck"});

		List<Farmer> farmers = TestUtils.createObjectMapper().readEntities(resultset, Farmer.class);
		
		assertEquals(1, farmers.size());
		
		assertEquals(1, farmers.get(0).getCows().size());
		
		assertEquals(3, farmers.get(0).getPigs().size());
		
		assertEquals(4, farmers.get(0).getDucks().size());
		
	}

	@Test
	public void testCarthesianProductWithGaps() {
		List<Object[]> resultset = new ArrayList<Object[]>();
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, null, null,       100, "First Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1002, "Third Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  100, "First Pig", 1003, "Fourth Duck"});

		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  null, null,        1002, "Third Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  101, "Second Pig", 1003, "Fourth Duck"});

		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1000, "First Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", 1001, "Second Duck"});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", null, null});
		resultset.add(new Object[]{ 1, 10, "First Cow",  102, "Third Pig", null, null});

		List<Farmer> farmers = TestUtils.createObjectMapper().readEntities(resultset, Farmer.class);
		
		assertEquals(1, farmers.size());

		assertEquals(1, farmers.get(0).getCows().size());
		assertEquals("First Cow", farmers.get(0).getCows().get(0).getName());
		
		assertEquals(3, farmers.get(0).getPigs().size());
		assertEquals("Third Pig", farmers.get(0).getPigs().get(2).getName());
		
		assertEquals(4, farmers.get(0).getDucks().size());
		assertEquals("Fourth Duck", farmers.get(0).getDucks().get(3).getName());
		
	}

	
}
