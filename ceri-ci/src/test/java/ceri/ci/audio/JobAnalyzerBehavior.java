package ceri.ci.audio;

import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.ci.build.Build;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Event;

public class JobAnalyzerBehavior {
	private static final Event b0 = BuildTestUtil.event(Event.Type.broken, 0);
	private static final Event b1 = BuildTestUtil.event(Event.Type.broken, 1, "b10", "b11", "b12");
	private static final Event f2 = BuildTestUtil.event(Event.Type.fixed, 2);
	private static final Event f3 = BuildTestUtil.event(Event.Type.fixed, 3, "f30", "f31", "f32");
	private static final Event b4 = BuildTestUtil.event(Event.Type.broken, 4, "b4");
	private static final Event b5 = BuildTestUtil.event(Event.Type.broken, 5);
	private static Build testBuild;
	
	@BeforeClass
	public static void createLatestBuild() {
		// Builds just be summarized builds, i.e. no more than one of each event type.
		testBuild = new Build("test");
		testBuild.job("j0"); // ok
		testBuild.job("j1"); // ok
		testBuild.job("j2").event(b0); // broken
		testBuild.job("j3").event(b1); // broken
		testBuild.job("j4").event(f2); // fixed
		testBuild.job("j5").event(f3); // fixed
		testBuild.job("j6").event(b0, f3); // fixed
		testBuild.job("j7").event(b1, f2); // fixed
		testBuild.job("j8").event(f2, b4); // broken
		testBuild.job("j9").event(f3, b5); // broken
	}
	
	@Test
	public void shouldIdentifyJobTransitionsBetweenNonEmptyBuilds() {
		Build previousBuild = new Build(testBuild.name);
		// Event times are not considered when comparing old and new builds.
		previousBuild.job("j0").event(b0); // broken
		previousBuild.job("j1").event(b1, f3); // fixed
		previousBuild.job("j2").event(f2, b4); // broken
		previousBuild.job("j3").event(f3); // fixed
		previousBuild.job("j4").event(b4); // broken
		previousBuild.job("j5").event(b0, f3); // fixed
		previousBuild.job("j6").event(f3, b5); // broken
		previousBuild.job("j7").event(f2); // fixed
		previousBuild.job("j8").event(b4); // broken
		previousBuild.job("j9").event(b1, f3); // fixed
		JobAnalyzer analyzer = new JobAnalyzer(testBuild, previousBuild);
		assertJobNames(analyzer.justBroken, "j3", "j9");
		assertJobNames(analyzer.stillBroken, "j2", "j8");
		assertJobNames(analyzer.justFixed, "j4", "j6");
	}

	@Test
	public void shouldIdentifyNoJobTransitionsToEmptyBuild() {
		Build latestBuild = new Build(testBuild.name);
		JobAnalyzer analyzer = new JobAnalyzer(latestBuild, testBuild);
		assertTrue(analyzer.justBroken.isEmpty());
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}

	@Test
	public void shouldIdentifyNoJobTransitionsBetweenEmptyBuilds() {
		Build previous = new Build("test");
		Build latest = new Build("test");
		JobAnalyzer analyzer = new JobAnalyzer(latest, previous);
		assertTrue(analyzer.justBroken.isEmpty());
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}
	
	@Test
	public void shouldIdentifyJobTransitionsFromEmptyBuild() {
		Build previousBuild = new Build(testBuild.name);
		JobAnalyzer analyzer = new JobAnalyzer(testBuild, previousBuild);
		assertJobNames(analyzer.justBroken, "j2", "j3", "j8", "j9");
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}

}