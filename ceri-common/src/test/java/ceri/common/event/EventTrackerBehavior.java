package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.function.Supplier;
import org.junit.Test;
import ceri.common.event.EventTracker.State;
import ceri.common.test.TestUtil;
import ceri.common.util.PrimitiveUtil;

public class EventTrackerBehavior {

	@Test
	public void codeCoverage() {
		TestUtil.exerciseEnum(EventTracker.State.class);
	}
	
	@Test
	public void shouldClearEvents() {
		EventTracker tracker = new EventTracker(2, 100000);
		assertThat(tracker.events(), is(0));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.exceeded));
		assertThat(tracker.events(), is(3));
		tracker.clear();
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.events(), is(2));
	}
	
	@Test
	public void shouldCheckTheNumberOfEventsWithinTheWindow() {
		EventTracker tracker = new EventTracker(5, 10);
		assertThat(tracker.addEvent(1), is(State.ok));
		assertThat(tracker.addEvent(5), is(State.ok));
		assertThat(tracker.addEvent(5), is(State.ok));
		assertThat(tracker.addEvent(8), is(State.ok));
		assertThat(tracker.addEvent(9), is(State.ok));
		assertThat(tracker.addEvent(10), is(State.exceeded));
		assertThat(tracker.addEvent(15), is(State.exceeded));
		assertThat(tracker.addEvent(16), is(State.ok));
	}

	@Test
	public void shouldUseCurrentTimeWhenNoTimestampIsGiven() {
		EventTracker tracker = tracker(5, 10L, 1, 5, 5, 8, 9, 10, 15, 16);
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.ok));
		assertThat(tracker.addEvent(), is(State.exceeded));
		assertThat(tracker.addEvent(), is(State.exceeded));
		assertThat(tracker.addEvent(), is(State.ok));
	}

	private EventTracker tracker(int maxEvents, Long windowMs, int... timeStamps) {
		Supplier<Integer> supplier = PrimitiveUtil.asList(timeStamps).iterator()::next;
		return tracker(maxEvents, windowMs, supplier);
	}

	private EventTracker tracker(int maxEvents, long windowMs, Supplier<? extends Number> supplier) {
		return new EventTracker(maxEvents, windowMs) {
			@Override
			long currentTimeMs() {
				return supplier.get().longValue();
			}
		};
	}

}
