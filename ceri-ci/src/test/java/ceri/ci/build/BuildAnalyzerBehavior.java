package ceri.ci.build;

import static ceri.common.test.TestUtil.assertCollection;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BuildAnalyzerBehavior {
	private static Event e0 = new Event(Event.Type.failure, 0L, "test");
	private static Event e1 = new Event(Event.Type.success, 1L, "test");

	@Test
	public void shouldStartEmpty() {
		BuildAnalyzer ba = new BuildAnalyzer();
		assertTrue(ba.analyzedJobs().isEmpty());
		assertTrue(ba.stillBrokenJobs().isEmpty());
		assertTrue(ba.stillBrokenJobs().isEmpty());
	}

	@Test
	public void shouldTrackStillBrokenBuilds() {
		Builds builds = new Builds();
		BuildAnalyzer ba = new BuildAnalyzer();
		builds.build("b0").job("j0").event(e0);
		ba.update(builds);
		assertTrue(ba.stillBrokenJobs().isEmpty());
		ba.update(builds);
		assertCollection(ba.stillBrokenJobs(), new AnalyzedJob(builds.build("b0"), builds
			.build("b0")));
	}

	@Test
	public void shouldUpdateAnalyzedJobs() {
		Builds builds = new Builds();
		BuildAnalyzer ba = new BuildAnalyzer();
		builds.build("b0").job("j0").event(e0);
		ba.update(builds);
		assertCollection(ba.analyzedJobs(), new AnalyzedJob(builds.build("b0"), new Build("b0")));
		Builds builds2 = new Builds(builds);
		builds2.build("b0").job("j0").event(e1);
		ba.update(builds2);
		assertCollection(ba.analyzedJobs(),
			new AnalyzedJob(builds2.build("b0"), builds.build("b0")));
	}

}
