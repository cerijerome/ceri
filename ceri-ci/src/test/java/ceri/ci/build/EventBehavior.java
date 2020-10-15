package ceri.ci.build;

import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(event1.type, is(Event.Type.success));
		assertRange(event2.timeStamp, t0, t1);
		assertThat(event2.type, is(Event.Type.failure));
	}

	@Test
	public void shouldAllowEmptyNames() {
		Event event = new Event(Event.Type.success, null, Collections.emptyList());
		assertTrue(event.names.isEmpty());
		assertThat(event.type, is(Event.Type.success));
	}

	@Test
	public void shouldCopyAllFieldsWithCopyConstructor() {
		Event event1 = Event.success("a", "b");
		Event event2 = new Event(event1);
		assertThat(event1, is(event2));
		assertThat(event1.names, is(event2.names));
		assertThat(event1.timeStamp, is(event2.timeStamp));
		assertThat(event1.type, is(event2.type));
	}

	@Test
	public void shouldConformToEqualsContract() {
		Event event = Event.success("a", "b");
		assertNotEquals(null, event);
		assertNotEquals(event, Event.failure("a", "b"));
		assertEquals(event, event);
		Event event2 = new Event(event);
		assertEquals(event, event2);
		assertThat(event.hashCode(), is(event2.hashCode()));
		assertThat(event.toString(), is(event2.toString()));
		assertThat(event.compareTo(event2), is(0));
	}

}
