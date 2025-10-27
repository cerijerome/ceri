package ceri.common.event;

import java.util.function.Supplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.test.Assert;

public class EventTrackerBehavior {

	@Test
	public void shouldClearEvents() {
		EventTracker tracker = EventTracker.of(2, 100000);
		Assert.equal(tracker.events(), 0);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), false);
		Assert.equal(tracker.events(), 3);
		tracker.clear();
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.events(), 2);
	}

	@Test
	public void shouldCheckTheNumberOfEventsWithinTheWindow() {
		EventTracker tracker = EventTracker.of(5, 10);
		Assert.equal(tracker.add(1), true);
		Assert.equal(tracker.add(5), true);
		Assert.equal(tracker.add(5), true);
		Assert.equal(tracker.add(8), true);
		Assert.equal(tracker.add(9), true);
		Assert.equal(tracker.add(10), false);
		Assert.equal(tracker.add(15), false);
		Assert.equal(tracker.add(16), true);
	}

	@Test
	public void shouldUseCurrentTimeWhenNoTimestampIsGiven() {
		EventTracker tracker = tracker(5, 10L, 1, 5, 5, 8, 9, 10, 15, 16);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), true);
		Assert.equal(tracker.add(), false);
		Assert.equal(tracker.add(), false);
		Assert.equal(tracker.add(), true);
	}

	private EventTracker tracker(int maxEvents, Long windowMs, int... timeStamps) {
		Supplier<Integer> supplier = ArrayUtil.ints.list(timeStamps).iterator()::next;
		return tracker(maxEvents, windowMs, supplier);
	}

	private EventTracker tracker(int maxEvents, long windowMs,
		Supplier<? extends Number> supplier) {
		return new EventTracker(maxEvents, windowMs) {
			@Override
			long currentTimeMs() {
				return supplier.get().longValue();
			}
		};
	}
}
