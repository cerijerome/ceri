package ceri.ci.build;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class EventComparatorsTest {
	private static final Event e0 = new Event(Event.Type.broken, 0, "0");
	private static final Event e1 = new Event(Event.Type.fixed, 1, "1");
	private static final Event e2 = new Event(Event.Type.broken, 2, "2");
	private static final Event e3 = new Event(Event.Type.fixed, 3, "3");
	private static final Event e4 = new Event(Event.Type.broken, 4, "4");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EventComparators.class);
	}
	
	@Test
	public void testSortByType() {
		List<Event> events = new ArrayList<>();
		Collections.addAll(events, e0, e1, e2, e3, e4);
		Collections.sort(events, EventComparators.TYPE);
		assertElements(events, e1, e3, e0, e2, e4);
	}

	@Test
	public void testSortByTimestamp() {
		List<Event> events = new ArrayList<>();
		Collections.addAll(events, e4, e0, e3, e2, e1);
		Collections.sort(events, EventComparators.TIMESTAMP);
		assertElements(events, e0, e1, e2, e3, e4);
	}

}
