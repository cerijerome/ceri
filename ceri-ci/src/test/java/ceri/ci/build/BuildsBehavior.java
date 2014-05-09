package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertEvents;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BuildsBehavior {
	private static final Event e0 = new Event(Event.Type.failure, 0L);
	private static final Event e1 = new Event(Event.Type.success, 1L, "a1");
	private static final Event e2 = new Event(Event.Type.success, 2L, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.failure, 3L, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.failure, 4L);
	private static final Event e5 = new Event(Event.Type.failure, 5L, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.success, 6L);
	private static final Event e7 = new Event(Event.Type.success, 7L, "g1");

	@Test
	public void shouldDeleteBuilds() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1);
		builds.build("b0").job("j1").events(e0, e2);
		builds.build("b1").job("j0").events(e3);
		builds.delete("b0");
		assertBuildNames(builds, "b1");
		assertJobNames(builds.build("b1"), "j0");
		assertEvents(builds.build("b1").job("j0").events, e3);
		builds.delete();
		assertThat(builds, is(new Builds()));
	}

	@Test
	public void shouldProcessBuildEvents() {
		BuildEvent b0 = new BuildEvent("b0", "j0", e0);
		BuildEvent b1 = new BuildEvent("b0", "j1", e1);
		Builds builds = new Builds();
		builds.process(b0, b1);
		assertBuildNames(builds, "b0");
		assertJobNames(builds.build("b0"), "j0", "j1");
		assertEvents(builds.build("b0").job("j0").events, e0);
		assertEvents(builds.build("b0").job("j1").events, e1);
	}

	@Test
	public void shouldPurgeJobEvents() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1, e2, e3, e4);
		builds.build("b0").job("j1").events(e0, e2, e4, e5, e6);
		builds.build("b1").job("j0").events();
		builds.purge();
		assertBuildNames(builds, "b0", "b1");
		assertJobNames(builds.build("b0"), "j0", "j1");
		assertJobNames(builds.build("b1"), "j0");
		assertEvents(builds.build("b0").job("j0").events, e4, e3, e2, e1);
		assertEvents(builds.build("b0").job("j1").events, e6, e5, e4);
		assertTrue(builds.build("b1").job("j0").events.isEmpty());
	}

	@Test
	public void shouldClearEventsFromJobs() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0, e1);
		builds.build("b0").job("j1").events(e2, e3);
		builds.build("b1").job("j0").events(e4);
		builds.build("b1").job("j1").events(e5, e6, e7);
		builds.clear();
		assertBuildNames(builds, "b0", "b1");
		assertJobNames(builds.build("b0"), "j0", "j1");
		assertJobNames(builds.build("b1"), "j0", "j1");
		assertTrue(builds.build("b0").job("j0").events.isEmpty());
		assertTrue(builds.build("b0").job("j1").events.isEmpty());
		assertTrue(builds.build("b1").job("j0").events.isEmpty());
		assertTrue(builds.build("b1").job("j1").events.isEmpty());
	}

	@Test
	public void shouldCopyAllBuilds() {
		Builds builds = new Builds();
		builds.build("b1").job("j1").events(e0, e1);
		builds.build("b1").job("j2").events(e3);
		builds.build("b2").job("j1").events(e4);
		builds.build("b2").job("j2").events(e5, e6, e7);
		Builds builds2 = new Builds(builds);
		assertEvents(builds2.build("b1").job("j1").events, e1, e0);
		assertEvents(builds2.build("b1").job("j2").events, e3);
		assertEvents(builds2.build("b2").job("j1").events, e4);
		assertEvents(builds2.build("b2").job("j2").events, e7, e6, e5);
		builds.build("b1").job("j1").events(e7);
		builds.build("b1").job("j2").events(e7);
		builds.build("b2").job("j1").events(e7);
		assertEvents(builds2.build("b1").job("j1").events, e1, e0);
		assertEvents(builds2.build("b1").job("j2").events, e3);
		assertEvents(builds2.build("b2").job("j1").events, e4);
		assertEvents(builds2.build("b2").job("j2").events, e7, e6, e5);
	}

	@Test
	public void shouldConformToEqualsContract() {
		Builds builds = new Builds();
		assertFalse(builds.equals(null));
		assertTrue(builds.equals(new Builds()));
		assertTrue(builds.equals(builds));
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L);
		builds.build("b0").job("j0").events(e0);
		builds.build("b0").job("j1").events();
		builds.build("b1").job("j0").events(e0, e1);
		Builds builds2 = new Builds(builds);
		assertTrue(builds.equals(builds2));
		assertThat(builds.hashCode(), is(builds2.hashCode()));
		assertThat(builds.toString(), is(builds2.toString()));
	}

}
