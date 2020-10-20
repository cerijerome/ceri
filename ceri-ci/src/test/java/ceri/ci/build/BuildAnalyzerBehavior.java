package ceri.ci.build;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Collection;
import org.junit.Test;

public class BuildAnalyzerBehavior {
	private static Event e0 = new Event(Event.Type.failure, 0L, "test");
	private static Event e1 = new Event(Event.Type.success, 1L, "test");

	@Test
	public void shouldStartEmpty() {
		BuildAnalyzer ba = new BuildAnalyzer();
		assertTrue(ba.analyzedJobs().isEmpty());
	}

	@Test
	public void shouldStayEmptyAfterUpdatesWithNoBuildSpecified() {
		BuildAnalyzer ba = new BuildAnalyzer();
		ba.update();
		assertTrue(ba.analyzedJobs().isEmpty());
		ba.update();
		assertTrue(ba.analyzedJobs().isEmpty());
	}

	@Test
	public void shouldUpdateWithCurrentBuildsIfNoBuildsSpecified() {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(e0);
		BuildAnalyzer ba = new BuildAnalyzer();
		ba.update(builds);
		ba.update();
		Collection<AnalyzedJob> ajs = ba.analyzedJobs();
		assertEquals(ajs.size(), 1);
		AnalyzedJob aj = ajs.iterator().next();
		assertTrue(aj.justBroken.isEmpty());
		assertTrue(aj.justFixed.isEmpty());
		assertEquals(aj.stillBroken.size(), 1);
		Job job = aj.stillBroken.iterator().next();
		Job j0 = new Job("j0");
		j0.events(e0);
		assertEquals(job, j0);
	}

	@Test
	public void shouldUpdateAnalyzedJobs() {
		Builds builds = new Builds();
		BuildAnalyzer ba = new BuildAnalyzer();
		builds.build("b0").job("j0").events(e0);
		ba.update(builds);
		assertCollection(ba.analyzedJobs(), new AnalyzedJob(builds.build("b0"), new Build("b0")));
		Builds builds2 = new Builds(builds);
		builds2.build("b0").job("j0").events(e1);
		ba.update(builds2);
		assertCollection(ba.analyzedJobs(),
			new AnalyzedJob(builds2.build("b0"), builds.build("b0")));
	}

}
