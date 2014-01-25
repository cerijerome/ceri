package ceri.ci.build;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;

public class BuildBehavior {
	private static final Event e0 = new Event(Event.Type.broken, 0);
	private static final Event e1 = new Event(Event.Type.fixed, 1, "a1");
	private static final Event e2 = new Event(Event.Type.fixed, 2, "b1", "b2");
	private static final Event e3 = new Event(Event.Type.broken, 3, "c1", "c2", "c3");
	private static final Event e4 = new Event(Event.Type.broken, 4);
	private static final Event e5 = new Event(Event.Type.broken, 5, "e1", "e2", "e3", "e4");
	private static final Event e6 = new Event(Event.Type.fixed, 6);
	private static final Event e7 = new Event(Event.Type.fixed, 7, "g1");
	
	@Test
	public void shouldCopyAllJobs() {
		Build build = new Build("test");
		build.job("job1").event(e0, e1, e2, e3, e4, e5, e6, e7);
		build.job("job2").event(e0, e2);
		Build build2 = new Build(build);
		assertElements(build2.job("job1").events, e7, e6, e5, e4, e3, e2, e1, e0);
		assertElements(build2.job("job2").events, e2, e0);
		build.job("job2").event(e1);
		assertElements(build.job("job2").events, e2, e1, e0);
		assertElements(build2.job("job2").events, e2, e0);
	}

	@Test
	public void shouldPurgeJobs() {
		Build build = new Build("test");
		build.job("job1").event(e0, e1, e2, e3, e4, e5, e6, e7);
		build.job("job2").event(e0, e2, e3, e4, e5);
		build.job("job3").event(e1, e2, e4, e6, e7);
		build.purge();
		assertElements(build.job("job1").events, e7, e6, e5, e4, e3);
		assertElements(build.job("job2").events, e5, e4, e3, e2);
		assertElements(build.job("job3").events, e7, e6, e4);
	}

	@Test
	public void shouldClearJobEvents() {
		Build build = new Build("test");
		build.job("a").event(e0);
		build.job("a").event(e1);
		build.job("b").event(e2);
		assertElements(build.job("a").events, e1, e0);
		assertElements(build.job("b").events, e2);
		build.clear();
		assertElements(names(build.jobs), "a", "b");
		assertTrue(build.job("a").events.isEmpty());
		assertTrue(build.job("b").events.isEmpty());
	}

	@Test
	public void shouldStoreJobsInAlphabeticalOrder() {
		Build build = new Build("test");
		build.job("bbb");
		build.job("aaa");
		build.job("ccc");
		assertElements(names(build.jobs), "aaa", "bbb", "ccc");
	}

	@Test
	public void shouldNotAllowModificationOfJobsField() {
		final Build build = new Build("build");
		assertTrue(build.jobs.isEmpty());
		assertException(new Runnable() {
			@Override
			public void run() {
				build.jobs.add(new Job("job"));
			}
		});
	}

	@Test
	public void shouldAddJobsWhenAccessed() {
		Build build = new Build("test");
		assertTrue(build.jobs.isEmpty());
		build.job("job1");
		build.job("job2");
		assertElements(names(build.jobs), "job1", "job2");
	}

	private Collection<String> names(Collection<Job> jobs) {
		Collection<String> names = new ArrayList<>();
		for (Job job : jobs) names.add(job.name);
		return names;
	}
	
}
