package ceri.ci.web;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class AnalyzedActorsBehavior {
	private static final Event e0 = BuildTestUtil.event(Event.Type.failure, 1, "n0");
	private static final Event e1 = BuildTestUtil.event(Event.Type.success, 2, "n1");
	private static final Event e2 = BuildTestUtil.event(Event.Type.failure, 3, "n2");
	private static final Event e3 = BuildTestUtil.event(Event.Type.success, 4, "n3");
	private static final Event e4 = BuildTestUtil.event(Event.Type.failure, 5, "n4");
	private static final Event e5 = BuildTestUtil.event(Event.Type.success, 6, "n5");

	@Test
	public void shouldTrackMultipleJobs() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e1);
		builds.build("b0").job("j0").events(e2);
		builds.build("b0").job("j1").events(e2);
		builds.build("b0").job("j1").events(e3);
		builds.build("b1").job("j0").events(e3);
		builds.build("b1").job("j0").events(e4);
		AnalyzedActors actors = new AnalyzedActors(builds);
		assertCollection(actors.heroes, new Actor("n3", "b0", "j1"));
		assertCollection(actors.villains, new Actor("n2", "b0", "j0"), new Actor("n4", "b1", "j0"));
	}

	@Test
	public void shouldIgnoreUnbrokenJobs() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e1);
		builds.build("b0").job("j0").events(e3);
		AnalyzedActors actors = new AnalyzedActors(builds);
		assertTrue(actors.heroes.isEmpty());
		assertTrue(actors.villains.isEmpty());
	}

	@Test
	public void shouldTrackOnlyTheFirstOfTheLatestFixes() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e1);
		builds.build("b0").job("j0").events(e2);
		builds.build("b0").job("j0").events(e3);
		builds.build("b0").job("j0").events(e5);
		AnalyzedActors actors = new AnalyzedActors(builds);
		assertCollection(actors.heroes, new Actor("n3", "b0", "j0"));
		assertTrue(actors.villains.isEmpty());
	}

	@Test
	public void shouldTrackTheLatestAggregatedBreaks() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0);
		builds.build("b0").job("j0").events(e1);
		builds.build("b0").job("j0").events(e2);
		builds.build("b0").job("j0").events(e4);
		AnalyzedActors actors = new AnalyzedActors(builds);
		assertTrue(actors.heroes.isEmpty());
		assertCollection(actors.villains, new Actor("n2", "b0", "j0"), new Actor("n4", "b0", "j0"));
	}

	@Test
	public void shouldConformToEqualsContract() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0);
		builds.build("b0").job("j1").events(e1);
		AnalyzedActors actors = new AnalyzedActors(builds);
		AnalyzedActors actors2 = new AnalyzedActors(builds);
		assertNotEquals(null, actors);
		assertEquals(actors, actors);
		assertEquals(actors, actors2);
		assertThat(actors.hashCode(), is(actors2.hashCode()));
		assertThat(actors.toString(), is(actors2.toString()));
	}

}
