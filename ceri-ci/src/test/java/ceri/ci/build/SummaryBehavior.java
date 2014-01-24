package ceri.ci.build;

import static ceri.common.test.TestUtil.assertElements;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SummaryBehavior {
	private static final Event f0 = new Event(Event.Type.fixed, 0, Arrays.asList("f0"));
	private static final Event f1 = new Event(Event.Type.fixed, 1, Arrays.asList("f1"));
	private static final Event b2 = new Event(Event.Type.broken, 2, Arrays.asList("b2"));
	private static final Event f3 = new Event(Event.Type.fixed, 3, Arrays.asList("f3"));
	private static final Event b4 = new Event(Event.Type.broken, 4, Arrays.asList("b4"));
	private static final Event b5 = new Event(Event.Type.broken, 5, Arrays.asList("b5"));
	private static final Event f6 = new Event(Event.Type.fixed, 6, Arrays.asList("f6"));
	private static final Event b7 = new Event(Event.Type.broken, 7, Arrays.asList("b7"));
	private static final List<Event> ALL = Arrays.asList(f0, f1, b2, f3, b4, b5, f6, b7);

	@Test
	public void shouldUseEarliestTimeStampForAggregatedBreak() {
		Summary s = Summary.create(Arrays.asList(f0, f1, b2, b4, b5, f6));
		assertThat(s.broken, is(false));
		assertThat(s.lastFix, is(f6));
		assertElements(s.lastBreaks, b2, b4, b5);
		assertThat(s.lastBreakAggr.timeStamp, is(b2.timeStamp));
	}

	@Test
	public void shouldAggregateNamesFromBreakEvents() {
		Summary s = Summary.create(Arrays.asList(f0, f1, b2, f3, b4, b5, f6));
		assertThat(s.broken, is(false));
		assertThat(s.lastFix, is(f6));
		assertElements(s.lastBreaks, b4, b5);
		assertElements(s.lastBreakAggr.names, "b4", "b5");
	}

	@Test
	public void shouldBeFixedForNoEvents() {
		Summary s = Summary.create(Collections.<Event>emptyList());
		assertThat(s.broken, is(false));
		assertNull(s.lastFix);
		assertNull(s.lastBreakAggr);
		assertTrue(s.lastBreaks.isEmpty());
	}

	@Test
	public void shouldSortEventsByTime() {
		Summary s = Summary.create(Arrays.asList(f1, b4, b5, f0, b2, f3)); // 0..5
		assertThat(s.broken, is(true));
		assertThat(s.lastFix, is(f3));
		assertThat(s.lastBreakAggr.timeStamp, is(b4.timeStamp));
	}

	@Test
	public void shouldSetLastFixAsLastTransitionFromBroken() {
		Summary s = Summary.create(ALL);
		assertThat(s.lastFix, is(f6));
		assertThat(s.broken, is(true));
	}

	@Test
	public void shouldHaveLastFixAsNullIfNeverFixed() {
		Summary s = Summary.create(Arrays.asList(b2, b4, b5));
		assertNull(s.lastFix);
		assertThat(s.broken, is(true));
	}

	@Test
	public void shouldSetLastFixAsFirstFixIfNeverBroken() {
		Summary s = Summary.create(Arrays.asList(f0, f1, f3, f6));
		assertThat(s.lastFix, is(f0));
		assertThat(s.broken, is(false));
	}

	@Test
	public void shouldSetLastFixAsFirstFixIfNeverTransitionedFromBroken() {
		Summary s = Summary.create(Arrays.asList(f0, f1, b2, b4));
		assertThat(s.lastFix, is(f0));
		assertThat(s.broken, is(true));
	}

}
