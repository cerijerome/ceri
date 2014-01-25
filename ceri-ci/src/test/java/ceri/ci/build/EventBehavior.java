package ceri.ci.build;

import static ceri.common.test.TestUtil.assertRange;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.Test;

public class EventBehavior {
	
	@Test(expected=Exception.class)
	public void shouldBeImmutable() {
		Event event = new Event(Event.Type.broken, 100, "a", "b", "c");
		event.names.add("d");
	}

	@Test
	public void shouldBeCreatedWithCurrentTime() {
		long t0 = System.currentTimeMillis();
		Event event1 = Event.fixed("ceri");
		Event event2 = Event.broken("ceri");
		long t1 = System.currentTimeMillis();
		assertRange(event1.timeStamp, t0, t1);
		assertThat(event1.type, is(Event.Type.fixed));
		assertRange(event2.timeStamp, t0, t1);
		assertThat(event2.type, is(Event.Type.broken));
	}

	@Test
	public void shouldAllowEmptyNames() throws Exception {
		Event event = Event.fixed(Collections.<String>emptyList());
		assertTrue(event.names.isEmpty());
		assertThat(event.type, is(Event.Type.fixed));
	}

	@Test
	public void shouldCopyAllFieldsWithCopyConstructor() {
		Event event1 = Event.fixed("a", "b");
		Event event2 = new Event(event1);
		assertThat(event1, is(event2));
		assertThat(event1.names, is(event2.names));
		assertThat(event1.timeStamp, is(event2.timeStamp));
		assertThat(event1.type, is(event2.type));
	}

	@Test
	public void shouldConformToEqualsContract() {
		Event event = Event.fixed("a", "b");
		assertFalse(event.equals(null));
		assertFalse(event.equals(Event.broken("a", "b")));
		assertTrue(event.equals(event));
		Event event2 = new Event(event);
		assertTrue(event.equals(event2));
		assertThat(event.hashCode(), is(event2.hashCode()));
		assertThat(event.toString(), is(event2.toString()));
		assertThat(event.compareTo(event2), is(0));
	}

}
