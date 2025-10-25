package ceri.common.concurrent;

import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import org.junit.Test;

public class BoolConditionBehavior {

	@Test
	public void shouldReturnSignalState() {
		BoolCondition flag = BoolCondition.of();
		assertTrue(flag.signal());
		assertFalse(flag.signal());
	}

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		BoolCondition flag = BoolCondition.of();
		assertFalse(flag.awaitPeek(0));
		assertFalse(flag.awaitPeek(1));
		flag.signal();
		flag.awaitPeek();
		assertTrue(flag.awaitPeek(0));
		assertTrue(flag.awaitPeek(1));
	}

	@Test
	public void shouldSetWithoutSignal() {
		BoolCondition flag = BoolCondition.of();
		assertFalse(flag.isSet());
		assertTrue(flag.set());
		assertFalse(flag.set());
		assertTrue(flag.isSet());
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		BoolCondition flag = BoolCondition.of();
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
