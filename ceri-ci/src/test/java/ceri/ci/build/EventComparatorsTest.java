package ceri.ci.build;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class EventComparatorsTest {
	private static final Event e0 = new Event(Event.Type.failure, 0L, "0");
	private static final Event e1 = new Event(Event.Type.success, 1L, "1");
	private static final Event e2 = new Event(Event.Type.failure, 2L, "2");
	private static final Event e3 = new Event(Event.Type.success, 3L, "3");
	private static final Event e4 = new Event(Event.Type.failure, 4L, "4");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EventComparators.class);
	}
	
	@Test
	public void testSortByType() {
		List<Event> events = new ArrayList<>();
		Collections.addAll(events, e0, e1, e2, e3, e4);
		events.sort(EventComparators.TYPE);
		assertIterable(events, e1, e3, e0, e2, e4);
	}

	@Test
	public void testSortByTimestamp() {
		List<Event> events = new ArrayList<>();
		Collections.addAll(events, e4, e0, e3, e2, e1);
		events.sort(EventComparators.TIMESTAMP);
		assertIterable(events, e0, e1, e2, e3, e4);
	}

}
