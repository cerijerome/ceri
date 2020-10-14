package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.junit.Test;

public class BuildUtilTest {
	private static final Event e0 = new Event(Event.Type.failure, 0L);
	private static final Event e1 = new Event(Event.Type.success, 1L, "a1");
	private static final Event e2 = new Event(Event.Type.success, 2L, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.failure, 3L, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.failure, 4L, "d1");
	private static final Event e5 = new Event(Event.Type.failure, 5L, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.success, 6L);
	private static final Event e7 = new Event(Event.Type.success, 7L, "g1");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BuildUtil.class);
	}

	@Test
	public void testCountEvents() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1);
		assertThat(BuildUtil.countEvents(builds), is(2));
		builds.build("b0").job("j1").events(e2);
		assertThat(BuildUtil.countEvents(builds), is(3));
		builds.build("b1").job("j0").events(e0);
		assertThat(BuildUtil.countEvents(builds), is(4));
	}

	@Test
	public void testBreakNames() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1, e2, e3, e4, e5, e6, e7);
		builds.build("b0").job("j1").events();
		builds.build("b1").job("j0").events(e0, e3, e4);
		builds = BuildUtil.summarize(builds);
		Collection<String> names = BuildUtil.summarizedBreakNames(builds);
		assertCollection(names, "c1", "c2", "c3", "d1");
	}

	@Test
	public void testSummarize() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1, e2, e3, e4, e5, e6, e7);
		builds.build("b0").job("j1").events();
		builds.build("b1").job("j0").events(e0, e3, e4);
		builds = BuildUtil.summarize(builds);
		assertBuildNames(builds.builds, "b0", "b1");
		assertJobNames(builds.build("b0").jobs, "j0", "j1");
		assertJobNames(builds.build("b1").jobs, "j0");
		assertIterable(builds.build("b0").job("j0").events, new Event(Event.Type.success, 6L),
			new Event(Event.Type.failure, 3L, "c1", "c2", "c3", "d1", "e1", "e2", "e3", "e4"));
		assertTrue(builds.build("b0").job("j1").events.isEmpty());
		assertIterable(builds.build("b1").job("j0").events,
			new Event(Event.Type.failure, 0L, "c1", "c2", "c3", "d1"));
	}

	@Test
	public void testLatest() {
		Event event = BuildUtil.latest(Event.Type.failure, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event, is(e5));
		event = BuildUtil.latest(Event.Type.success, e0, e3, e4);
		assertNull(event);
		event = BuildUtil.latest(Event.Type.failure);
		assertNull(event);
	}

	@Test
	public void testEarliest() {
		Event event = BuildUtil.earliest(Event.Type.success, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event, is(e1));
		event = BuildUtil.earliest(Event.Type.failure, e1, e2, e6);
		assertNull(event);
		event = BuildUtil.earliest(Event.Type.success);
		assertNull(event);
	}

	@Test
	public void testEmptyAggregate() {
		Event event = BuildUtil.aggregate(Event.Type.failure);
		assertNull(event);
		event = BuildUtil.aggregate(Event.Type.failure, e1, e2);
		assertNull(event);
		event = BuildUtil.aggregate(Event.Type.success, e0, e3, e4, e5);
		assertNull(event);
	}

	@Test
	public void testAggregate() {
		Event event = BuildUtil.aggregate(Event.Type.failure, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event.type, is(Event.Type.failure));
		assertThat(event.timeStamp, is(0L));
		assertCollection(event.names, "c1", "c2", "c3", "d1", "e1", "e2", "e3", "e4");
		event = BuildUtil.aggregate(Event.Type.success, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event.type, is(Event.Type.success));
		assertThat(event.timeStamp, is(1L));
		assertCollection(event.names, "a1", "b1", "b2", "g1");
	}

}
