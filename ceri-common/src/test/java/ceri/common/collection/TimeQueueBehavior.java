package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.test.Captor;

public class TimeQueueBehavior {

	@Test
	public void shouldRemoveElements() {
		TimeQueue<String> tq = TimeQueue.millis();
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
		TimeQueue<String> tq = TimeQueue.millis();
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
		TimeQueue<String> tq = TimeQueue.nanos();
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
		TimeQueue<String> tq = TimeQueue.micros();
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
		TimeQueue<String> tq = TimeQueue.millis();
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor);
		captor.verify("a", "b");
	}

	@Test
	public void shouldProvideLimitedExpiredElementsToConsumer() {
		TimeQueue<String> tq = TimeQueue.millis();
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor, 1);
		captor.verify("a");
	}

}
