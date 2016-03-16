package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.OM;
import gugit.om.test.model.Address;
import gugit.om.test.model.Person;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class AlternativePerformanceTest {

	@Test
	public void testSingleEntityReadPerformance() {
		List<Object[]> resultset = new LinkedList<Object[]>();
		
		int resultsetSize = 1000000;
		addAddressEntities(resultset, resultsetSize);
		
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testAddressMappingPerformance(resultset);
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
		
		int testCount = 50;
		long totalTime = 0;
		long min = Integer.MAX_VALUE;
		long max = 0;
		long t;
		for (int i=0; i<testCount; i++){
			t = testPersonMappingPerformance(resultset);
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
	
	private static void addAddressEntities(List<Object[]> resultset, int n) {
		for (int i=0; i<n; i++)
			resultset.add(new Object[]{i, "country"+i, "city"+i, "street"+i, null});
	}
	
	private static long testAddressMappingPerformance(List<Object[]> resultset) {
		long started = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		LinkedList entities = manuallyMapAddressEntities(resultset);
		long finished = System.currentTimeMillis();
		entities.clear();
		return finished - started;
	}
	
	private static LinkedList manuallyMapAddressEntities(List<Object[]> resultset) {
		LinkedList rez = new LinkedList();
		for (Object row[]: resultset){
			Address address = new Address();
				address.setId((Integer)row[0]);
				address.setCountry((String)row[1]);
				address.setCity((String)row[2]);
				address.setStreet((String)row[3]);
				
				Integer pid = (Integer)row[4];
				if (pid != null && pid.equals(14))
					address.setOwner(new Person());
				
			rez.add(address);
		}
		return rez ;
	}


	private void addPersonEntities(List<Object[]> resultset, int n) {
		int j=0;
		for (int i=0; i<n; i++){
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
			resultset.add(new Object[]{i, "Name"+i, n+i, "country"+i, "city"+i, "street"+i, i, ++j, "Country"+j, "City"+j, "Street"+j, i});
		}
	}
	
	private static long testPersonMappingPerformance(List<Object[]> resultset) {
		long started = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		LinkedList entities = manuallyMapPersonEntities(resultset);
		long finished = System.currentTimeMillis();
		if (entities.size()!=resultset.size()/2)
			throw new RuntimeException("manual mapping error");
		entities.clear();
		return finished - started;
	}
	
	private static LinkedList manuallyMapPersonEntities(List<Object[]> resultset) {
		LinkedList rez = new LinkedList();
		
		Address lastAddress = null;
		Person lastPerson = null;
		
		for (Object row[]: resultset){
			int colIndex = 0;
			
			Person person = new Person();
				person.setId((Integer)row[colIndex++]);
				person.setName((String)row[colIndex++]);
			
			if (lastPerson!= null && person.getId().equals(lastPerson.getId()))
				;
			else{
				rez.add(person);
				lastPerson = person;
			}
				
			Address address = new Address();
				address.setId((Integer)row[colIndex++]);
				address.setCountry((String)row[colIndex++]);
				address.setCity((String)row[colIndex++]);
				address.setStreet((String)row[colIndex++]);
				
				Integer pid = (Integer)row[colIndex++]; 
				if (pid != null && lastPerson!= null && pid.equals(lastPerson.getId()))
					address.setOwner(lastPerson);
				
			person.setCurrentAddress(address);
			
			address = new Address();
				address.setId((Integer)row[colIndex++]);
				address.setCountry((String)row[colIndex++]);
				address.setCity((String)row[colIndex++]);
				address.setStreet((String)row[colIndex++]);
				
				pid = (Integer)row[colIndex++];
				if (pid != null && lastPerson != null & pid.equals(lastPerson.getId()))
					address.setOwner(lastPerson);
				
			if (lastAddress != null && address.getId().equals(lastAddress.getId()))
				address = lastAddress;
			else
				lastAddress = address;
			
			person.getPreviousAddresses().add(address);
		}
		return rez ;
	}

}
