package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertEvents;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
		assertEvents(builds.build("build").job("job").events, e1, e0);
	}

	@Test
	public void shouldConformToEqualsContract() {
		assertFalse(b0.equals(null));
		assertTrue(b0.equals(b0));
		assertFalse(b0.equals(new BuildEvent("build", "job", e1)));
		assertFalse(b0.equals(new BuildEvent("build", "job0", e0)));
		assertFalse(b0.equals(new BuildEvent("build0", "job", e0)));
		assertFalse(b0.equals(b1));
		assertFalse(b1.equals(b0));
		BuildEvent b2 = new BuildEvent("build", "job", e0);
		assertTrue(b0.equals(b2));
		assertTrue(b2.equals(b0));
		assertThat(b0.hashCode(), is(b2.hashCode()));
		assertThat(b0.toString(), is(b2.toString()));
	}

}
