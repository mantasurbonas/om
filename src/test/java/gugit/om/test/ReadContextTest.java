package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.mapping.ReadContext;

import org.junit.Test;

public class ReadContextTest {

	@Test
	public void test() {
		ReadContext context = new ReadContext(null);
		context.entityIsBeingRead("Labas", 1);
		context.entityIsBeingRead("Atia", 2);
		
		assertEquals("Labas", context.findMasterEntity(String.class, 1));
		assertEquals("Atia", context.findMasterEntity(String.class, 2));
		assertNull(context.findMasterEntity(String.class, 3));
		
		context.entityReadingFinished();
		
		assertEquals("Labas", context.findMasterEntity(String.class, 1));
		assertNull(context.findMasterEntity(String.class, 2));
		
		context.entityReadingFinished();
		
		assertNull(context.findMasterEntity(String.class, 1));
	}
	
	@Test
	public void performanceTest(){
		long started = System.currentTimeMillis();
		
		ReadContext context = new ReadContext(null);
		int n = 1000000;
		for (int i=0; i<n; i++){
			context.entityIsBeingRead("Labas", 10);
			context.entityIsBeingRead("Atia", 20);
			context.entityIsBeingRead("Sveiki", 15);
			context.entityReadingFinished();
			context.entityReadingFinished();
			context.entityReadingFinished();
		}
		
		long finished = System.currentTimeMillis();
		
		System.out.println(n+"x3 ReadContext invocations took "+(finished-started)+"msec");
	}

}
