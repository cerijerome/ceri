package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class ValueConditionBehavior {

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		ValueCondition<Integer> count = ValueCondition.of(this::merge);
		Assert.equal(count.awaitPeek(0), (Integer) null);
		Assert.equal(count.awaitPeek(1), (Integer) null);
		count.signal(1);
		Assert.equal(count.awaitPeek(), 1);
		Assert.equal(count.awaitPeek(0), 1);
		Assert.equal(count.awaitPeek(1), 1);
		Assert.equal(count.awaitPeek((Integer) 1), 1);
		count.signal(2);
		Assert.equal(count.awaitPeek(), 3);
		Assert.equal(count.awaitPeek(0), 3);
		Assert.equal(count.awaitPeek(1), 3);
		Assert.equal(count.awaitPeek(0, 2), 3);
	}

	private Integer merge(Integer i, Integer j) {
		if (i == null) return j;
		if (j == null) return i;
		return i + j;
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		Assert.equal(flag.value(), (Integer) null);
		Assert.equal(flag.awaitTimeout(0), (Integer) null);
		Assert.equal(flag.awaitTimeout(1), (Integer) null);
		flag.signal(1);
		Assert.equal(flag.value(), 1);
		Assert.equal(flag.awaitTimeout(1), 1);
		flag.signal(2);
		flag.await();
		Assert.equal(flag.value(), (Integer) null);
		flag.signal(3);
		flag.clear();
		Assert.equal(flag.awaitTimeout(0), (Integer) null);
		flag.signal(4);
		Assert.equal(flag.awaitTimeout(0, 4), 4);
	}

	@Test
	public void shouldWaitForValue() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		flag.signal(2);
		Assert.equal(flag.await(Integer.valueOf(2)), 2);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		ValueCondition<Integer> flag = ValueCondition.of();
		Assert.equal(flag.toString(), "[null];hold=0;queue=0");
		Concurrent.lockedRun(flag.lock, () -> {
			Assert.equal(flag.toString(), "[null];hold=1;queue=0");
			try (var exec = Testing.threadCall(() -> flag.toString())) {
				Assert.equal(exec.get(), "empty;hold=0;queue=0");
			}
		});
	}
}
