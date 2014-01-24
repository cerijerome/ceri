package ceri.ci.build;

import static ceri.common.test.TestUtil.assertRange;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class EventBehavior {

	@Test
	public void shouldBeCreatedWithCurrentTime() {
		long t0 = System.currentTimeMillis();
		Event event1 = Event.fixed(Arrays.asList("ceri"));
		Event event2 = Event.broken(Arrays.asList("ceri"));
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

}
