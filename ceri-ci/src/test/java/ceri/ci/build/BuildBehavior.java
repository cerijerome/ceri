package ceri.ci.build;

import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static ceri.common.test.TestUtil.assertIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class BuildBehavior {
	private static final Event e0 = new Event(Event.Type.failure, 0L);
	private static final Event e1 = new Event(Event.Type.success, 1L, "a1");
	private static final Event e2 = new Event(Event.Type.success, 2L, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.failure, 3L, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.failure, 4L);
	private static final Event e5 = new Event(Event.Type.failure, 5L, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.success, 6L);
	private static final Event e7 = new Event(Event.Type.success, 7L, "g1");

	@Test
	public void shouldDeleteJobs() {
		Build build = new Build("test");
		build.job("job1").events(e0, e1);
		build.job("job2").events(e0, e2);
		Build build2 = new Build(build);
		build.delete("job2");
		BuildTestUtil.assertJobNames(build, "job1");
		BuildTestUtil.assertJobNames(build2, "job1", "job2");
	}

	@Test
	public void shouldCopyAllJobs() {
		Build build = new Build("test");
		build.job("job1").events(e0, e1, e2, e3, e4, e5, e6, e7);
		build.job("job2").events(e0, e2);
		Build build2 = new Build(build);
		assertIterable(build2.job("job1").events, e7, e6, e5, e4, e3, e2, e1, e0);
		assertIterable(build2.job("job2").events, e2, e0);
		build.job("job2").events(e1);
		assertIterable(build.job("job2").events, e2, e1, e0);
		assertIterable(build2.job("job2").events, e2, e0);
	}

	@Test
	public void shouldPurgeJobs() {
		Build build = new Build("test");
		build.job("job1").events(e0, e1, e2, e3, e4, e5, e6, e7);
		build.job("job2").events(e0, e2, e3, e4, e5);
		build.job("job3").events(e1, e2, e4, e6, e7);
		build.purge();
		assertIterable(build.job("job1").events, e7, e6, e5, e4, e3);
		assertIterable(build.job("job2").events, e5, e4, e3, e2);
		assertIterable(build.job("job3").events, e7, e6, e4);
	}

	@Test
	public void shouldClearJobEvents() {
		Build build = new Build("test");
		build.job("a").events(e0);
		build.job("a").events(e1);
		build.job("b").events(e2);
		assertIterable(build.job("a").events, e1, e0);
		assertIterable(build.job("b").events, e2);
		build.clear();
		assertJobNames(build.jobs, "a", "b");
		assertTrue(build.job("a").events.isEmpty());
		assertTrue(build.job("b").events.isEmpty());
	}

	@Test
	public void shouldStoreJobsInAlphabeticalOrder() {
		Build build = new Build("test");
		build.job("bbb");
		build.job("aaa");
		build.job("ccc");
		assertJobNames(build.jobs, "aaa", "bbb", "ccc");
	}

	@Test
	public void shouldNotAllowModificationOfJobsField() {
		final Build build = new Build("build");
		assertTrue(build.jobs.isEmpty());
		TestUtil.assertThrown(() -> build.jobs.add(new Job("job")));
	}

	@Test
	public void shouldAddJobsWhenAccessed() {
		Build build = new Build("test");
		assertTrue(build.jobs.isEmpty());
		build.job("job1");
		build.job("job2");
		assertJobNames(build.jobs, "job1", "job2");
	}

	@Test
	public void shouldConformToEqualsContract() {
		Build build = new Build("test");
		assertNotEquals(null, build);
		assertNotEquals(build, new Build("Test"));
		assertEquals(build, new Build("test"));
		assertEquals(build, build);
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L);
		build.job("j0").events(e0);
		build.job("j1").events(e0, e1);
		Build build2 = new Build(build);
		assertEquals(build, build2);
		assertThat(build.hashCode(), is(build2.hashCode()));
		assertThat(build.toString(), is(build2.toString()));
	}

}
