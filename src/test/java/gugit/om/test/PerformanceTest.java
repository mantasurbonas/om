package gugit.om.test;

import gugit.om.OM;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;


/***
 * Notes on performance as of 2016-03-13:
 * 
 * 1 000 000 x trivial entities mapped in avg 168-159 msec
 *   100 000 x 1:master vs 3:detail entities mapped in avg 81-76 msec
 *   
 * @author urbonman
 *
 */
public class PerformanceTest {

	@Test
	public void testSingleEntityReadPerformance() {
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 1000000;
		addAddressEntities(resultset, resultsetSize);
		
		OM<Address> om = new OM<Address>(Address.class);
		
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testPerformance(om, resultset);
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
		addPersonEntities(resultset, resultsetSize);
		
		OM<Person> om = new OM<Person>(Person.class);
				
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testPerformance(om, resultset);
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
	
	
	private long testPerformance(OM<?> om, List<Object[]> resultset) {
		long started = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		LinkedList entities = om.readEntities(resultset);
		long finished = System.currentTimeMillis();
		entities.clear();
		return finished - started;
	}
	
	private void addPersonEntities(List<Object[]> resultset, int n) {
		int j=0;
		for (int i=0; i<n; i++){
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
		}
	}

	private void addAddressEntities(List<Object[]> resultset, int n) {
		for (int i=0; i<n; i++)
			resultset.add(new Object[]{i, "country"+i, "city"+i, "street"+i, null});
	}

}
