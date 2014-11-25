package ceri.common.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BooleanConditionBehavior {

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		BooleanCondition flag = new BooleanCondition();
		assertFalse(flag.isSet());
		flag.signal();
		assertTrue(flag.isSet());
		flag.await();
		assertFalse(flag.isSet());
	}

}
