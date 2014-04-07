package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.Build;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.concurrent.BooleanCondition;

public class AlertServiceBehavior {
	Alerters alerters;
	BooleanCondition sync;
	AlertService service;
	
	@Before
	public void init() {
		alerters = mock(Alerters.class);
		sync = new BooleanCondition();
		service = new AlertService(createAlerters(), 0);
	}
	
	@After
	public void close() throws IOException {
		service.close();
	}
	
	@Test
	public void shouldReturnSnapshotOfBuilds() {
		Event broken = service.fixed("build0", "job0", "test");
		Event fixed = service.fixed("build0", "job0", "test");
		Builds builds = service.builds();
		service.broken("build1",  "job1", "test");
		Builds refBuilds = new Builds();
		refBuilds.build("build0").job("job0").event(broken, fixed);
		assertThat(builds, is(refBuilds));
	}

	@Test
	public void shouldReturnSnapshotOfBuild() {
		Event broken = service.fixed("build0", "job0", "test");
		Event fixed = service.fixed("build0", "job0", "test");
		Build build = service.build("build0");
		service.broken("build0",  "job1", "test");
		Build refBuild = new Build("build0");
		refBuild.job("job0").event(broken, fixed);
		assertThat(build, is(refBuild));
	}

	@Test
	public void shouldReturnJob() {
		Event broken = service.fixed("build0", "job0", "test");
		Event fixed = service.fixed("build0", "job0", "test");
		Job job = service.job("build0", "job0");
		service.broken("build0",  "job0", "test");
		Job refJob = new Job("job0");
		refJob.event(broken, fixed);
		assertThat(job, is(refJob));
	}

	@Test
	public void shouldDeleteBuild() {
		service.fixed("build0", "job0", "test");
		service.fixed("build0", "job0", "test");
		service.delete("build0", null);
		Builds builds = service.builds();
		assertThat(builds, is(new Builds()));
	}
	
	@Test
	public void shouldDeleteJob() {
		service.fixed("build0", "job0", "test");
		service.fixed("build0", "job0", "test");
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
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldClearJob() throws InterruptedException  {
		service.clear("b0", "j0");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldBreak() throws InterruptedException  {
		service.broken("b0", "j0", "n000");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldFix() throws InterruptedException  {
		service.fixed("b0", "j0", "n000");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldPurge()  {
		service.purge();
		verifyZeroInteractions(alerters);
	}

	private Alerters createAlerters() {
		return new TestAlerters(Alerters.builder()) {
			@Override
			public void alert(Builds builds) {
				alerters.alert(builds);
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
