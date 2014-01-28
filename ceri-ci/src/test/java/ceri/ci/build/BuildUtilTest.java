package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertElements;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.junit.Test;

public class BuildUtilTest {
	private static final Event e0 = new Event(Event.Type.broken, 0);
	private static final Event e1 = new Event(Event.Type.fixed, 1, "a1");
	private static final Event e2 = new Event(Event.Type.fixed, 2, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.broken, 3, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.broken, 4);
	private static final Event e5 = new Event(Event.Type.broken, 5, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.fixed, 6);
	private static final Event e7 = new Event(Event.Type.fixed, 7, "g1");

	@Test
	public void testBreakNames() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(e0, e1, e2, e3, e4, e5, e6, e7);
		builds.build("b0").job("j1").event();
		builds.build("b1").job("j0").event(e0, e3, e4);
		builds = BuildUtil.summarize(builds);
		Collection<String> names = BuildUtil.summarizedBreakNames(builds);
		assertCollection(names, "c1", "c2", "c3");
	}

	@Test
	public void testSummarize() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(e0, e1, e2, e3, e4, e5, e6, e7);
		builds.build("b0").job("j1").event();
		builds.build("b1").job("j0").event(e0, e3, e4);
		builds = BuildUtil.summarize(builds);
		assertBuildNames(builds.builds, "b0", "b1");
		assertJobNames(builds.build("b0").jobs, "j0", "j1");
		assertJobNames(builds.build("b1").jobs, "j0");
		assertElements(builds.build("b0").job("j0").events, new Event(Event.Type.fixed, 6),
			new Event(Event.Type.broken, 3, "c1", "c2", "c3", "e1", "e2", "e3", "e4"));
		assertTrue(builds.build("b0").job("j1").events.isEmpty());
		assertElements(builds.build("b1").job("j0").events, new Event(Event.Type.broken, 0, "c1",
			"c2", "c3"));
	}

	@Test
	public void testLatest() {
		Event event = BuildUtil.latest(Event.Type.broken, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event, is(e5));
		event = BuildUtil.latest(Event.Type.fixed, e0, e3, e4);
		assertNull(event);
		event = BuildUtil.latest(Event.Type.broken);
		assertNull(event);
	}

	@Test
	public void testEarliest() {
		Event event = BuildUtil.earliest(Event.Type.fixed, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event, is(e1));
		event = BuildUtil.earliest(Event.Type.broken, e1, e2, e6);
		assertNull(event);
		event = BuildUtil.earliest(Event.Type.fixed);
		assertNull(event);
	}

	@Test
	public void testEmptyAggregate() {
		Event event = BuildUtil.aggregate(Event.Type.broken);
		assertNull(event);
		event = BuildUtil.aggregate(Event.Type.broken, e1, e2);
		assertNull(event);
		event = BuildUtil.aggregate(Event.Type.fixed, e0, e3, e4, e5);
		assertNull(event);
	}

	@Test
	public void testAggregate() {
		Event event = BuildUtil.aggregate(Event.Type.broken, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event.type, is(Event.Type.broken));
		assertThat(event.timeStamp, is(0L));
		assertCollection(event.names, "c1", "c2", "c3", "e1", "e2", "e3", "e4");
		event = BuildUtil.aggregate(Event.Type.fixed, e0, e1, e2, e3, e4, e5, e6, e7);
		assertThat(event.type, is(Event.Type.fixed));
		assertThat(event.timeStamp, is(1L));
		assertCollection(event.names, "a1", "b1", "b2", "g1");
	}

}
