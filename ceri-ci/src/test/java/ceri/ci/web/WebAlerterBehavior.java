package ceri.ci.web;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class WebAlerterBehavior {
	private static final Event e0 = BuildTestUtil.event(Event.Type.failure, 1, "n0");
	private static final Event e1 = BuildTestUtil.event(Event.Type.success, 2, "n1");
	private WebAlerter webAlerter = new WebAlerter();

	@Test
	public void shouldUpdateActorsFromNewBuilds() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0);
		builds.build("b0").job("j1").events(e0);
		builds.build("b1").job("j0").events(e0);
		builds.build("b1").job("j0").events(e1);
		webAlerter.update(builds);
		AnalyzedActors actors = webAlerter.actors();
		assertCollection(actors.heroes, new Actor("n1", "b1", "j0"));
		assertCollection(actors.villains, new Actor("n0", "b0", "j0"), new Actor("n0", "b0", "j1"));
	}

	@Test
	public void shouldHandleRemind() {
		webAlerter.remind();
	}

	@Test
	public void shouldStartWithNoActors() {
		AnalyzedActors actors = webAlerter.actors();
		assertTrue(actors.heroes.isEmpty());
		assertTrue(actors.villains.isEmpty());
	}

	@Test
	public void shouldHaveNoActorsForEmptyBuilds() {
		Builds builds = new Builds();
		webAlerter.update(builds);
		AnalyzedActors actors = webAlerter.actors();
		assertTrue(actors.heroes.isEmpty());
		assertTrue(actors.villains.isEmpty());
	}

	@Test
	public void shouldClearActors() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0);
		builds.build("b0").job("j1").events(e1);
		webAlerter.update(builds);
		webAlerter.clear();
		AnalyzedActors actors = webAlerter.actors();
		assertTrue(actors.heroes.isEmpty());
		assertTrue(actors.villains.isEmpty());
	}

}
