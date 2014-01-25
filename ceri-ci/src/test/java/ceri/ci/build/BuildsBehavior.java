package ceri.ci.build;

import static ceri.common.test.TestUtil.assertElements;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;

public class BuildsBehavior {
	private static final Event e0 = new Event(Event.Type.broken, 0);
	private static final Event e1 = new Event(Event.Type.fixed, 1, "a1");
	private static final Event e2 = new Event(Event.Type.fixed, 2, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.broken, 3, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.broken, 4);
	private static final Event e5 = new Event(Event.Type.broken, 5, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.fixed, 6);
	private static final Event e7 = new Event(Event.Type.fixed, 7, "g1");
	
	@Test
	public void shouldPurgeJobEvents() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(e0, e1, e2, e3, e4);
		builds.build("b0").job("j1").event(e0, e2, e4, e5, e6);
		builds.build("b1").job("j0").event();
		builds.purge();
		assertElements(names(builds.builds), "b0", "b1");
		assertElements(jobNames(builds.build("b0").jobs), "j0", "j1");
		assertElements(jobNames(builds.build("b1").jobs), "j0");
		assertElements(builds.build("b0").job("j0").events, e4, e3, e2, e1);
		assertElements(builds.build("b0").job("j1").events, e6, e5, e4);
		assertTrue(builds.build("b1").job("j0").events.isEmpty());
	}

	@Test
	public void shouldClearEventsFromJobs() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(e0, e1);
		builds.build("b0").job("j1").event(e2, e3);
		builds.build("b1").job("j0").event(e4);
		builds.build("b1").job("j1").event(e5, e6, e7);
		builds.clear();
		assertElements(names(builds.builds), "b0", "b1");
		assertElements(jobNames(builds.build("b0").jobs), "j0", "j1");
		assertElements(jobNames(builds.build("b1").jobs), "j0", "j1");
		assertTrue(builds.build("b0").job("j0").events.isEmpty());
		assertTrue(builds.build("b0").job("j1").events.isEmpty());
		assertTrue(builds.build("b1").job("j0").events.isEmpty());
		assertTrue(builds.build("b1").job("j1").events.isEmpty());
	}

	@Test
	public void shouldCopyAllBuilds() {
		Builds builds = new Builds();
		builds.build("b1").job("j1").event(e0, e1);
		builds.build("b1").job("j2").event(e3);
		builds.build("b2").job("j1").event(e4);
		builds.build("b2").job("j2").event(e5, e6, e7);
		Builds builds2 = new Builds(builds);
		assertElements(builds2.build("b1").job("j1").events, e1, e0);
		assertElements(builds2.build("b1").job("j2").events, e3);
		assertElements(builds2.build("b2").job("j1").events, e4);
		assertElements(builds2.build("b2").job("j2").events, e7, e6, e5);
		builds.build("b1").job("j1").event(e7);
		builds.build("b1").job("j2").event(e7);
		builds.build("b2").job("j1").event(e7);
		assertElements(builds2.build("b1").job("j1").events, e1, e0);
		assertElements(builds2.build("b1").job("j2").events, e3);
		assertElements(builds2.build("b2").job("j1").events, e4);
		assertElements(builds2.build("b2").job("j2").events, e7, e6, e5);
	}

	private Collection<String> names(Collection<Build> builds) {
		Collection<String> names = new ArrayList<>();
		for (Build build : builds) names.add(build.name);
		return names;
	}
	
	private Collection<String> jobNames(Collection<Job> jobs) {
		Collection<String> names = new ArrayList<>();
		for (Job job : jobs) names.add(job.name);
		return names;
	}
	
}
