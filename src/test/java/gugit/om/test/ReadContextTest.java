package gugit.om.test;

import static org.junit.Assert.*;
import gugit.om.mapping.ReadContext;

import org.junit.Test;

public class ReadContextTest {

	@Test
	public void test() {
		ReadContext context = new ReadContext();
		context.entityIsBeingRead("Labas", 1);
		context.entityIsBeingRead("Atia", 2);
		
		assertEquals("Labas", context.findEntity(String.class, 1));
		assertEquals("Atia", context.findEntity(String.class, 2));
		assertNull(context.findEntity(String.class, 3));
		
		context.entityReadingFinished();
		
		assertEquals("Labas", context.findEntity(String.class, 1));
		assertNull(context.findEntity(String.class, 2));
		
		context.entityReadingFinished();
		
		assertNull(context.findEntity(String.class, 1));
	}
	
	@Test
	public void performanceTest(){
		long started = System.currentTimeMillis();
		
		ReadContext context = new ReadContext();
		for (int i=0; i<1000000; i++){
			context.entityIsBeingRead("Labas", 10);
			context.entityIsBeingRead("Atia", 20);
			context.entityIsBeingRead("Sveiki", 15);
			context.entityReadingFinished();
			context.entityReadingFinished();
			context.entityReadingFinished();
		}
		
		long finished = System.currentTimeMillis();
		
		System.out.println((finished-started)+"msec");
	}

}
