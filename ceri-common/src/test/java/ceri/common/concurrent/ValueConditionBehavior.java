package ceri.common.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ValueConditionBehavior {

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		ValueCondition<Integer> count = ValueCondition.of(this::merge);
		assertThat(count.awaitPeek(0), is((Integer) null));
		assertThat(count.awaitPeek(1), is((Integer) null));
		count.signal(1);
		assertThat(count.awaitPeek(), is(1));
		assertThat(count.awaitPeek(0), is(1));
		assertThat(count.awaitPeek(1), is(1));
		assertThat(count.awaitPeek((Integer) 1), is(1));
		count.signal(2);
		assertThat(count.awaitPeek(), is(3));
		assertThat(count.awaitPeek(0), is(3));
		assertThat(count.awaitPeek(1), is(3));
		assertThat(count.awaitPeek(0, 2), is(3));
	}

	private Integer merge(Integer i, Integer j) {
		if (i == null) return j;
		if (j == null) return i;
		return i + j;
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		assertThat(flag.value(), is((Integer) null));
		assertThat(flag.awaitTimeout(0), is((Integer) null));
		assertThat(flag.awaitTimeout(1), is((Integer) null));
		flag.signal(1);
		assertThat(flag.value(), is(1));
		assertThat(flag.awaitTimeout(1), is(1));
		flag.signal(2);
		flag.await();
		assertThat(flag.value(), is((Integer) null));
		flag.signal(3);
		flag.clear();
		assertThat(flag.awaitTimeout(0), is((Integer) null));
		flag.signal(4);
		assertThat(flag.awaitTimeout(0, 4), is(4));
	}

	@Test
	public void shouldWaitForValue() throws InterruptedException {
		ValueCondition<Integer> flag = ValueCondition.of();
		flag.signal(2);
		assertThat(flag.await(Integer.valueOf(2)), is(2));
	}

}
