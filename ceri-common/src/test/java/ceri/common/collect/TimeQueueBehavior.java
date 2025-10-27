package ceri.common.collect;

import static ceri.common.time.TimeSupplier.micros;
import static ceri.common.time.TimeSupplier.millis;
import static ceri.common.time.TimeSupplier.nanos;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class TimeQueueBehavior {

	@Test
	public void shouldOnlyAddItemsOnce() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		Assert.equal(tq.add("a"), true);
		Assert.equal(tq.add("a"), false);
		Assert.equal(tq.add("a", 1000), false);
		Assert.equal(tq.remove(), "a");
		Assert.equal(tq.add("a", 1000), true);
	}

	@Test
	public void shouldSetItems() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		Assert.equal(tq.setOffset("a", 10000), true);
		Assert.equal(tq.next(), null);
		Assert.equal(tq.set("a"), false);
		Assert.equal(tq.next(), "a");
		Assert.equal(tq.isEmpty(), true);
	}

	@Test
	public void shouldRemoveHeadElement() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		tq.add("a");
		tq.add("b");
		tq.add("c");
		Assert.equal(tq.remove(), "a");
		tq.clear();
		Assert.equal(tq.remove(), null);
		Assert.equal(tq.isEmpty(), true);
	}

	@Test
	public void shouldGetNextExpiredElements() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		long ms = tq.time();
		tq.add("a", ms);
		tq.add("b");
		tq.addOffset("c", 10000);
		Assert.equal(tq.next(ms - 5000), null);
		Assert.equal(tq.next(), "a");
		Assert.equal(tq.next(), "b");
		Assert.equal(tq.next(), null);
		Assert.equal(tq.next(ms + 20000), "c");
		Assert.equal(tq.next(ms + 20000), null);
	}

	@Test
	public void shouldGetNextTime() {
		TimeQueue<String> tq = TimeQueue.of(nanos);
		long ns = tq.time(0);
		Assert.equal(tq.nextTime(), 0L);
		tq.add("a", ns - 1000);
		tq.add("b", ns + 1000);
		Assert.equal(tq.nextTime(), ns - 1000L);
		tq.remove();
		Assert.equal(tq.nextTime(), ns + 1000L);
	}

	@Test
	public void shouldGetNextDelay() {
		TimeQueue<String> tq = TimeQueue.of(micros);
		long us = tq.time();
		Assert.equal(tq.nextDelay(), 0L);
		tq.add("a", us - 1000);
		tq.add("b", us + 1000);
		Assert.equal(tq.nextDelay(us), 0L);
		tq.remove();
		Assert.equal(tq.nextDelay(us), 1000L);
	}

	@Test
	public void shouldProvideExpiredElementsToConsumer() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor);
		captor.verify("a", "b");
	}

	@Test
	public void shouldProvideLimitedExpiredElementsToConsumer() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		tq.add("a");
		tq.add("b");
		tq.addOffset("c", 10000);
		Captor<String> captor = Captor.of();
		tq.forEachNext(captor, 1);
		captor.verify("a");
	}

	@Test
	public void shouldRetrieveInTimeOrder() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		tq.add("c", 3000);
		tq.add("b", 2000);
		tq.add("a", 1000);
		Assert.equal(tq.next(), "a");
		Assert.equal(tq.next(), "b");
		Assert.equal(tq.next(), "c");
	}

	@Test
	public void shouldRemoveItem() {
		TimeQueue<String> tq = TimeQueue.of(millis);
		tq.add("c", 3000);
		tq.add("b", 2000);
		tq.add("a", 1000);
		Assert.equal(tq.remove("b"), true);
		Assert.equal(tq.remove("d"), false);
		Assert.equal(tq.next(), "a");
		Assert.equal(tq.next(), "c");
	}

}
