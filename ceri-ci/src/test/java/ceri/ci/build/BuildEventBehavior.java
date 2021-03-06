package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import org.junit.Test;

public class BuildEventBehavior {
	private static Event e0 = new Event(Event.Type.failure, 0L, "test");
	private static Event e1 = new Event(Event.Type.success, 1L, "test");
	private static BuildEvent b0 = new BuildEvent("build", "job", e0);
	private static BuildEvent b1 = new BuildEvent("build", "job", e1);

	@Test
	public void shouldApplyToBuilds() {
		Builds builds = new Builds();
		b0.applyTo(builds);
		b1.applyTo(builds);
		assertBuildNames(builds, "build");
		assertJobNames(builds.build("build"), "job");
		assertCollection(builds.build("build").job("job").events, e1, e0);
	}

	@Test
	public void shouldConformToEqualsContract() {
		assertNotEquals(null, b0);
		assertEquals(b0, b0);
		assertNotEquals(b0, new BuildEvent("build", "job", e1));
		assertNotEquals(b0, new BuildEvent("build", "job0", e0));
		assertNotEquals(b0, new BuildEvent("build0", "job", e0));
		assertNotEquals(b0, b1);
		assertNotEquals(b1, b0);
		BuildEvent b2 = new BuildEvent("build", "job", e0);
		assertEquals(b0, b2);
		assertEquals(b2, b0);
		assertEquals(b0.hashCode(), b2.hashCode());
		assertEquals(b0.toString(), b2.toString());
	}

}
