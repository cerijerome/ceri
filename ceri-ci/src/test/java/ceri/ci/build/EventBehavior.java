package ceri.ci.build;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertRange;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Collections;
import org.junit.Test;

public class EventBehavior {

	@Test(expected = Exception.class)
	public void shouldBeImmutable() {
		Event event = new Event(Event.Type.failure, 100L, "a", "b", "c");
		event.names.add("d");
	}

	@Test
	public void shouldBeCreatedWithCurrentTime() {
		long t0 = System.currentTimeMillis();
		Event event1 = Event.success("ceri");
		Event event2 = Event.failure("ceri");
		long t1 = System.currentTimeMillis();
		assertRange(event1.timeStamp, t0, t1);
		assertEquals(event1.type, Event.Type.success);
		assertRange(event2.timeStamp, t0, t1);
		assertEquals(event2.type, Event.Type.failure);
	}

	@Test
	public void shouldAllowEmptyNames() {
		Event event = new Event(Event.Type.success, null, Collections.emptyList());
		assertTrue(event.names.isEmpty());
		assertEquals(event.type, Event.Type.success);
	}

	@Test
	public void shouldCopyAllFieldsWithCopyConstructor() {
		Event event1 = Event.success("a", "b");
		Event event2 = new Event(event1);
		assertEquals(event1, event2);
		assertEquals(event1.names, event2.names);
		assertEquals(event1.timeStamp, event2.timeStamp);
		assertEquals(event1.type, event2.type);
	}

	@Test
	public void shouldConformToEqualsContract() {
		Event event = Event.success("a", "b");
		assertNotEquals(null, event);
		assertNotEquals(event, Event.failure("a", "b"));
		assertEquals(event, event);
		Event event2 = new Event(event);
		assertEquals(event, event2);
		assertEquals(event.hashCode(), event2.hashCode());
		assertEquals(event.toString(), event2.toString());
		assertEquals(event.compareTo(event2), 0);
	}

}
