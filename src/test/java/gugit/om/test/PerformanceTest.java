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

	private long testPerformance(OM<?> om, List<Object[]> resultset) {
		long started = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		LinkedList entities = om.readEntities(resultset);
		long finished = System.currentTimeMillis();
		entities.clear();
		return finished - started;
	}
	
	@Test
	public void testSingleEntityReadPerformance() {
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 1000000;
		addAddressEntities(resultset, resultsetSize);
		
		OM<Address> om = new OM<Address>(Address.class);
		
		int testCount = 20;
		long time = 0;
		for (int i=0; i<testCount; i++){
			time += testPerformance(om, resultset);
		}
		
		System.out.println(resultsetSize+" trivial entities mapped in avg"+(time/testCount)+" msec");
	}

	@Test
	public void testMasterDetailEntityReadPerformance(){
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 100000;
		addPersonEntities(resultset, resultsetSize);
		
		OM<Person> om = new OM<Person>(Person.class);
		
		int testCount = 20;
		long time = 0;
		for (int i=0; i<testCount; i++){
			time += testPerformance(om, resultset);
		}
		
		System.out.println(resultsetSize+" x 1:master vs 3:detail entities mapped in "+(time/testCount)+" msec");
	}
	
	private void addPersonEntities(List<Object[]> resultset, int n) {
		int j=0;
		for (int i=0; i<n; i++){
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, ++j, "Country"+j, "City"+j, "Street"+j});
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, ++j, "Country"+j, "City"+j, "Street"+j});
		}
	}

	private void addAddressEntities(List<Object[]> resultset, int n) {
		for (int i=0; i<n; i++)
			resultset.add(new Object[]{i, "country"+i, "city"+i, "street"+i});
	}

}
