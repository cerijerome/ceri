package ceri.common.event;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.function.Supplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.event.EventTracker.State;
import ceri.common.test.TestUtil;

public class EventTrackerBehavior {

	@Test
	public void codeCoverage() {
		TestUtil.exerciseEnum(EventTracker.State.class);
	}

	@Test
	public void shouldClearEvents() {
		EventTracker tracker = new EventTracker(2, 100000);
		assertEquals(tracker.events(), 0);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.exceeded);
		assertEquals(tracker.events(), 3);
		tracker.clear();
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.events(), 2);
	}

	@Test
	public void shouldCheckTheNumberOfEventsWithinTheWindow() {
		EventTracker tracker = new EventTracker(5, 10);
		assertEquals(tracker.addEvent(1), State.ok);
		assertEquals(tracker.addEvent(5), State.ok);
		assertEquals(tracker.addEvent(5), State.ok);
		assertEquals(tracker.addEvent(8), State.ok);
		assertEquals(tracker.addEvent(9), State.ok);
		assertEquals(tracker.addEvent(10), State.exceeded);
		assertEquals(tracker.addEvent(15), State.exceeded);
		assertEquals(tracker.addEvent(16), State.ok);
	}

	@Test
	public void shouldUseCurrentTimeWhenNoTimestampIsGiven() {
		EventTracker tracker = tracker(5, 10L, 1, 5, 5, 8, 9, 10, 15, 16);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.ok);
		assertEquals(tracker.addEvent(), State.exceeded);
		assertEquals(tracker.addEvent(), State.exceeded);
		assertEquals(tracker.addEvent(), State.ok);
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
