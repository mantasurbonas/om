package gugit.om.test;

import gugit.om.OM;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;
import gugit.om.test.utils.TestUtils;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;


/***
 * Notes on performance as of 2016-03-13:
 * 
 * 1 000 000 x trivial entities mapped in 62 msec
 *   500 000 x 1:master vs 3:detail entities mapped in 141
 *   
 * @author urbonman
 *
 */
public class PerformanceTest {

	@Test
	public void testSingleEntityReadPerformance() {
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 1000000;
		createAddressResultset(resultset, resultsetSize);
		
		OM om = TestUtils.createObjectMapper();
		
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testPerformance(om, resultset, Address.class);
			totalTime += t;
			min = Math.min(t, min);
			max = Math.max(t, max);
		}
		
		System.out.println(resultsetSize
								+" trivial entities mapped in"
								+ " avg"+(totalTime/testCount)+" msec"
								+ " min"+min
								+ " max"+max);
	}

	@Test
	public void testMasterDetailEntityReadPerformance(){
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 500000;
		createPersonResultset(resultset, resultsetSize);
		
		OM om = TestUtils.createObjectMapper();
				
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testPerformance(om, resultset, Person.class);
			totalTime += t;
			min = Math.min(t, min);
			max = Math.max(t, max);
		}
		
		System.out.println(resultsetSize
								+" complex entities mapped in"
								+ " avg"+(totalTime/testCount)+" msec"
								+ " min"+min
								+ " max"+max);

	}
	
	
	private long testPerformance(OM om, List<Object[]> resultset, Class<?> claz) {
		long started = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		List entities = om.readEntities(resultset, claz);
		long finished = System.currentTimeMillis();
		entities.clear();
		return finished - started;
	}
	
	private void createPersonResultset(List<Object[]> resultset, int n) {
		int j=0;
		for (int i=0; i<n; i++){
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
		}
	}

	private void createAddressResultset(List<Object[]> resultset, int n) {
		for (int i=0; i<n; i++)
			resultset.add(new Object[]{i, "country"+i, "city"+i, "street"+i, null});
	}

}
