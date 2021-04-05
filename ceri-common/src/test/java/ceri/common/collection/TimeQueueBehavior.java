package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import org.junit.Test;
import ceri.common.test.Captor;

public class TimeQueueBehavior {

	@Test
	public void shouldOnlyAddItemsOnce() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		assertEquals(tq.add("a"), true);
		assertEquals(tq.add("a"), false);
		assertEquals(tq.add("a", 1000), false);
		assertEquals(tq.remove(), "a");
		assertEquals(tq.add("a", 1000), true);
	}

	@Test
	public void shouldRemoveHeadElement() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		tq.add("a");
		tq.add("b");
		tq.add("c");
		assertEquals(tq.remove(), "a");
		tq.clear();
		assertEquals(tq.remove(), null);
		assertEquals(tq.isEmpty(), true);
	}

	@Test
	public void shouldGetNextExpiredElements() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		long ms = tq.time();
		tq.add("a", ms);
		tq.add("b");
		tq.addOffset("c", 10000);
		assertEquals(tq.next(ms - 5000), null);
		assertEquals(tq.next(), "a");
		assertEquals(tq.next(), "b");
		assertEquals(tq.next(), null);
		assertEquals(tq.next(ms + 20000), "c");
		assertEquals(tq.next(ms + 20000), null);
	}

	@Test
	public void shouldGetNextTime() {
		TimeQueue<String> tq = TimeQueue.of(NANOSECONDS);
		long ns = tq.time();
		assertEquals(tq.nextTime(), 0L);
		tq.add("a", ns - 1000);
		tq.add("b", ns + 1000);
		assertEquals(tq.nextTime(), ns - 1000L);
		tq.remove();
		assertEquals(tq.nextTime(), ns + 1000L);
	}

	@Test
	public void shouldGetNextDelay() {
		TimeQueue<String> tq = TimeQueue.of(MICROSECONDS);
		long us = tq.time();
		assertEquals(tq.nextDelay(), 0L);
		tq.add("a", us - 1000);
		tq.add("b", us + 1000);
		assertEquals(tq.nextDelay(us), 0L);
		tq.remove();
		assertEquals(tq.nextDelay(us), 1000L);
	}

	@Test
	public void shouldProvideExpiredElementsToConsumer() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor);
		captor.verify("a", "b");
	}

	@Test
	public void shouldProvideLimitedExpiredElementsToConsumer() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor, 1);
		captor.verify("a");
	}

	@Test
	public void shouldRetrieveInTimeOrder() {
		TimeQueue<String> tq = TimeQueue.of(MILLISECONDS);
		tq.add("c", 3000);
		tq.add("b", 2000);
		tq.add("a", 1000);
		assertEquals(tq.next(), "a");
		assertEquals(tq.next(), "b");
		assertEquals(tq.next(), "c");
	}

}
