package ceri.common.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BooleanConditionBehavior {

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		BooleanCondition flag = BooleanCondition.of();
		assertFalse(flag.awaitPeek(0));
		assertFalse(flag.awaitPeek(1));
		flag.signal();
		assertTrue(flag.awaitPeek());
		assertTrue(flag.awaitPeek(0));
		assertTrue(flag.awaitPeek(1));
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		BooleanCondition flag = BooleanCondition.of();
		assertFalse(flag.isSet());
		flag.signal();
		assertTrue(flag.isSet());
		flag.await();
		assertFalse(flag.isSet());
		flag.signal();
		assertTrue(flag.await(0));
		flag.clear();
		assertFalse(flag.await(0));
	}

}
