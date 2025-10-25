package ceri.common.event;

import static ceri.common.test.Assert.assertEquals;
import java.util.function.Supplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;

public class EventTrackerBehavior {

	@Test
	public void shouldClearEvents() {
		EventTracker tracker = EventTracker.of(2, 100000);
		assertEquals(tracker.events(), 0);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), false);
		assertEquals(tracker.events(), 3);
		tracker.clear();
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.events(), 2);
	}

	@Test
	public void shouldCheckTheNumberOfEventsWithinTheWindow() {
		EventTracker tracker = EventTracker.of(5, 10);
		assertEquals(tracker.add(1), true);
		assertEquals(tracker.add(5), true);
		assertEquals(tracker.add(5), true);
		assertEquals(tracker.add(8), true);
		assertEquals(tracker.add(9), true);
		assertEquals(tracker.add(10), false);
		assertEquals(tracker.add(15), false);
		assertEquals(tracker.add(16), true);
	}

	@Test
	public void shouldUseCurrentTimeWhenNoTimestampIsGiven() {
		EventTracker tracker = tracker(5, 10L, 1, 5, 5, 8, 9, 10, 15, 16);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), true);
		assertEquals(tracker.add(), false);
		assertEquals(tracker.add(), false);
		assertEquals(tracker.add(), true);
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
