package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.Build;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.concurrent.BooleanCondition;

public class AlertServiceImplBehavior {
	private static final Event e0 = new Event(Event.Type.failure, 0L, "test");
	private static final Event e1 = new Event(Event.Type.success, 1L, "test");
	private static final Event e2 = new Event(Event.Type.failure, 2L, "test");
	AlerterGroup alerters;
	BooleanCondition sync;
	AlertServiceImpl service;

	@Before
	public void init() {
		alerters = mock(AlerterGroup.class);
		sync = new BooleanCondition();
		service = new AlertServiceImpl(createAlerters(), 0, 1000, 100000);
	}

	@After
	public void close() {
		service.close();
	}

	@Test
	public void shouldRemind() throws InterruptedException {
		try (AlerterGroup remindAlerters = new AlerterGroup(AlerterGroup.builder()) {
			@Override
			public void remind() {
				alerters.remind();
				sync.signal();
			}
		}) {
			service = new AlertServiceImpl(remindAlerters, 1, 1000, 100000);
			service.clear(null, null);
			sync.await();
			verify(alerters).remind();
		}
	}

	@Test
	public void shouldReturnSnapshotOfBuilds() {
		service.process(new BuildEvent("build0", "job0", e0));
		service.process(new BuildEvent("build0", "job0", e1));
		Builds builds = service.builds();
		service.process(new BuildEvent("build1", "job1", e2));
		Builds refBuilds = new Builds();
		refBuilds.build("build0").job("job0").events(e0, e1);
		assertThat(builds, is(refBuilds));
	}

	@Test
	public void shouldReturnSnapshotOfBuild() {
		service.process(new BuildEvent("build0", "job0", e0));
		service.process(new BuildEvent("build0", "job0", e1));
		Build build = service.build("build0");
		service.process(new BuildEvent("build0", "job1", e2));
		Build refBuild = new Build("build0");
		refBuild.job("job0").events(e0, e1);
		assertThat(build, is(refBuild));
	}

	@Test
	public void shouldReturnJob() {
		service.process(new BuildEvent("build0", "job0", e0));
		service.process(new BuildEvent("build0", "job0", e1));
		Job job = service.job("build0", "job0");
		service.process(new BuildEvent("build0", "job0", e2));
		Job refJob = new Job("job0");
		refJob.events(e0, e1);
		assertThat(job, is(refJob));
	}

	@Test
	public void shouldDeleteBuild() {
		service.process(new BuildEvent("build0", "job0", e0));
		service.process(new BuildEvent("build0", "job0", e1));
		service.delete("build0", null);
		Builds builds = service.builds();
		assertThat(builds, is(new Builds()));
	}

	@Test
	public void shouldDeleteJob() {
		service.process(new BuildEvent("build0", "job0", e0));
		service.process(new BuildEvent("build0", "job0", e1));
		service.delete("build0", "job0");
		Build build = service.build("build0");
		assertThat(build, is(new Build("build0")));
	}

	@Test
	public void shouldClearAll() throws InterruptedException {
		service.clear(null, null);
		sync.await();
		verify(alerters).clear();
	}

	@Test
	public void shouldClearBuild() throws InterruptedException {
		service.clear("b0", null);
		sync.await();
		verify(alerters).update((Builds) any());
	}

	@Test
	public void shouldClearJob() throws InterruptedException {
		service.clear("b0", "j0");
		sync.await();
		verify(alerters).update((Builds) any());
	}

	@Test
	public void shouldBreak() throws InterruptedException {
		service.process(new BuildEvent("build0", "job0", e0));
		sync.await();
		verify(alerters).update((Builds) any());
	}

	@Test
	public void shouldPurge() {
		service.purge();
		verifyZeroInteractions(alerters);
	}

	private AlerterGroup createAlerters() {
		return new AlerterGroup(AlerterGroup.builder()) {
			@Override
			public void update(Builds builds) {
				alerters.update(builds);
				sync.signal();
			}

			@Override
			public void clear() {
				alerters.clear();
				sync.signal();
			}

			@Override
			public void remind() {
				alerters.remind();
				sync.signal();
			}
		};
	}

}
