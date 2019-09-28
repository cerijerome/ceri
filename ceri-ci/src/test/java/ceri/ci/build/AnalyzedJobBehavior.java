package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class AnalyzedJobBehavior {
	private static final Event b0 = new Event(Event.Type.failure, 0L);
	private static final Event b1 = new Event(Event.Type.failure, 1L, "b10", "b11", "b12");
	private static final Event f2 = new Event(Event.Type.success, 2L);
	private static final Event f3 = new Event(Event.Type.success, 3L, "f30", "f31", "f32");
	private static final Event b4 = new Event(Event.Type.failure, 4L, "b4");
	private static final Event b5 = new Event(Event.Type.failure, 5L);
	private static Build testBuild;

	@BeforeClass
	public static void createLatestBuild() {
		// Builds just be summarized builds, i.e. no more than one of each event type.
		testBuild = new Build("test");
		testBuild.job("j0"); // ok
		testBuild.job("j1"); // ok
		testBuild.job("j2").events(b0); // broken
		testBuild.job("j3").events(b1); // broken
		testBuild.job("j4").events(f2); // fixed
		testBuild.job("j5").events(f3); // fixed
		testBuild.job("j6").events(b0, f3); // fixed
		testBuild.job("j7").events(b1, f2); // fixed
		testBuild.job("j8").events(f2, b4); // broken
		testBuild.job("j9").events(f3, b5); // broken
	}

	@Test
	public void shouldNotAllowBuildsWithDifferentNames() {
		Build build0 = new Build("b0");
		Build build1 = new Build("b1");
		TestUtil.assertThrown(() -> new AnalyzedJob(build0, build1));
	}

	@Test
	public void shouldObeyEqualsContract() {
		AnalyzedJob analyzer = new AnalyzedJob(testBuild, new Build("test"));
		AnalyzedJob analyzer2 = new AnalyzedJob(testBuild, new Build("test"));
		assertEquals(analyzer, analyzer);
		assertNotEquals(null, analyzer);
		assertEquals(analyzer, analyzer2);
		assertEquals(analyzer.hashCode(), analyzer2.hashCode());
		assertEquals(analyzer.toString(), analyzer2.toString());
		AnalyzedJob analyzer3 = new AnalyzedJob(new Build("test0"), new Build("test0"));
		assertNotEquals(analyzer, analyzer3);
		testBuild.job("j10").events(b5);
		AnalyzedJob analyzer4 = new AnalyzedJob(testBuild, new Build("test"));
		assertNotEquals(analyzer, analyzer4);
	}

	@Test
	public void shouldIdentifyJobTransitionsBetweenNonEmptyBuilds() {
		Build previousBuild = new Build(testBuild.name);
		// Event times are not considered when comparing old and new builds.
		previousBuild.job("j0").events(b0); // broken
		previousBuild.job("j1").events(b1, f3); // fixed
		previousBuild.job("j2").events(f2, b4); // broken
		previousBuild.job("j3").events(f3); // fixed
		previousBuild.job("j4").events(b4); // broken
		previousBuild.job("j5").events(b0, f3); // fixed
		previousBuild.job("j6").events(f3, b5); // broken
		previousBuild.job("j7").events(f2); // fixed
		previousBuild.job("j8").events(b4); // broken
		previousBuild.job("j9").events(b1, f3); // fixed
		AnalyzedJob analyzer = new AnalyzedJob(testBuild, previousBuild);
		assertJobNames(analyzer.justBroken, "j3", "j9");
		assertJobNames(analyzer.stillBroken, "j2", "j8");
		assertJobNames(analyzer.justFixed, "j4", "j6");
	}

	@Test
	public void shouldIdentifyNoJobTransitionsToEmptyBuild() {
		Build latestBuild = new Build(testBuild.name);
		AnalyzedJob analyzer = new AnalyzedJob(latestBuild, testBuild);
		assertTrue(analyzer.justBroken.isEmpty());
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}

	@Test
	public void shouldIdentifyNoJobTransitionsBetweenEmptyBuilds() {
		Build previous = new Build("test");
		Build latest = new Build("test");
		AnalyzedJob analyzer = new AnalyzedJob(latest, previous);
		assertTrue(analyzer.justBroken.isEmpty());
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}

	@Test
	public void shouldIdentifyJobTransitionsFromEmptyBuild() {
		Build previousBuild = new Build(testBuild.name);
		AnalyzedJob analyzer = new AnalyzedJob(testBuild, previousBuild);
		assertJobNames(analyzer.justBroken, "j2", "j3", "j8", "j9");
		assertTrue(analyzer.stillBroken.isEmpty());
		assertTrue(analyzer.justFixed.isEmpty());
	}

}
