package ceri.ci.build;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class JobBehavior {

	@Test
	public void shouldStoreEventsInDecreasingChronologicalOrder() {
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L);
		Event e2 = new Event(Event.Type.failure, 2L);
		Job job = new Job("test");
		job.events(e1, e0, e2);
		assertIterable(job.events, e2, e1, e0);
	}

	@Test
	public void shouldNotAllowModificationOfEventsField() {
		final Job job = new Job("test");
		assertTrue(job.events.isEmpty());
		assertException(() -> job.events.add(Event.success()));
	}

	@Test
	public void shouldPurgeBreakAndFixEventsUpToLatestSequences() {
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L, "a1");
		Event e2 = new Event(Event.Type.success, 2L, "b1", "b2");
		Event e3 = new Event(Event.Type.failure, 3L, "c1", "c2", "c3");
		Event e4 = new Event(Event.Type.failure, 4L);
		Event e5 = new Event(Event.Type.failure, 5L, "e1", "e2", "e3", "e4");
		Event e6 = new Event(Event.Type.success, 6L);
		Event e7 = new Event(Event.Type.success, 7L, "g1");
		Job job = new Job("test");
		job.events(e0, e1, e2, e3, e4, e5, e6, e7);
		Job job2 = new Job("test");
		job2.events(e3, e4, e5, e6, e7);
		job.purge();
		assertIterable(job.events, job2.events);
	}

	@Test
	public void shouldNotPurgeLatestSequencesOfBreakAndFixEvents() {
		Job job = new Job("test");
		job.events(Event.success("a"));
		job.events(Event.success("b1", "b2"));
		job.events(Event.failure("c1", "c2", "c3"));
		job.events(Event.failure());
		Job job2 = new Job(job);
		job.purge();
		assertIterable(job.events, job2.events);
	}

	@Test
	public void shouldClearEvents() {
		Job job = new Job("test");
		job.events(Event.success("a"));
		job.events(Event.failure("b1", "b2"));
		assertFalse(job.events.isEmpty());
		job.clear();
		assertTrue(job.events.isEmpty());
	}

	@Test
	public void shouldCopyAllFieldsWithCopyConstructor() {
		Job job1 = new Job("test");
		job1.events(Event.success("a1", "a2"));
		job1.events(Event.failure());
		Job job2 = new Job(job1);
		assertThat(job1.name, is(job2.name));
		assertIterable(job1.events, job2.events);
		assertTrue(job1.events != job2.events);
	}

	@Test
	public void shouldConformToEqualsContract() {
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L);
		Job job = new Job("test");
		assertFalse(job.equals(null));
		assertFalse(job.equals(new Job("Test")));
		assertTrue(job.equals(new Job("test")));
		assertTrue(job.equals(job));
		job.events(e0, e1);
		Job job2 = new Job(job);
		assertTrue(job.equals(job2));
		assertThat(job.hashCode(), is(job2.hashCode()));
		assertThat(job.toString(), is(job2.toString()));
	}

}
