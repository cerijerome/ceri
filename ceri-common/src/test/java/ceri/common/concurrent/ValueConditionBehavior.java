package ceri.common.concurrent;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.TestUtil.threadCall;
import org.junit.Test;

public class ValueConditionBehavior {

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		ValueCondition<Integer> count = ValueCondition.of(this::merge);
		assertEquals(count.awaitPeek(0), (Integer) null);
		assertEquals(count.awaitPeek(1), (Integer) null);
		count.signal(1);
		assertEquals(count.awaitPeek(), 1);
		assertEquals(count.awaitPeek(0), 1);
		assertEquals(count.awaitPeek(1), 1);
		assertEquals(count.awaitPeek((Integer) 1), 1);
		count.signal(2);
		assertEquals(count.awaitPeek(), 3);
		assertEquals(count.awaitPeek(0), 3);
		assertEquals(count.awaitPeek(1), 3);
		assertEquals(count.awaitPeek(0, 2), 3);
	}

	private Integer merge(Integer i, Integer j) {
		if (i == null) return j;
		if (j == null) return i;
		return i + j;
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		assertEquals(flag.value(), (Integer) null);
		assertEquals(flag.awaitTimeout(0), (Integer) null);
		assertEquals(flag.awaitTimeout(1), (Integer) null);
		flag.signal(1);
		assertEquals(flag.value(), 1);
		assertEquals(flag.awaitTimeout(1), 1);
		flag.signal(2);
		flag.await();
		assertEquals(flag.value(), (Integer) null);
		flag.signal(3);
		flag.clear();
		assertEquals(flag.awaitTimeout(0), (Integer) null);
		flag.signal(4);
		assertEquals(flag.awaitTimeout(0, 4), 4);
	}

	@Test
	public void shouldWaitForValue() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		flag.signal(2);
		assertEquals(flag.await(Integer.valueOf(2)), 2);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		ValueCondition<Integer> flag = ValueCondition.of();
		assertEquals(flag.toString(), "[null];hold=0;queue=0");
		Concurrent.lockedRun(flag.lock, () -> {
			assertEquals(flag.toString(), "[null];hold=1;queue=0");
			try (var exec = threadCall(() -> flag.toString())) {
				assertEquals(exec.get(), "empty;hold=0;queue=0");
			}
		});
	}

}
