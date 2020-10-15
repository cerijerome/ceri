package ceri.ci.build;

import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertNotSame;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.assertThrown(() -> job.events.add(Event.success()));
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
		assertNotSame(job1.events, job2.events);
	}

	@Test
	public void shouldConformToEqualsContract() {
		Event e0 = new Event(Event.Type.failure, 0L);
		Event e1 = new Event(Event.Type.success, 1L);
		Job job = new Job("test");
		assertNotEquals(null, job);
		assertNotEquals(job, new Job("Test"));
		assertEquals(job, new Job("test"));
		assertEquals(job, job);
		job.events(e0, e1);
		Job job2 = new Job(job);
		assertEquals(job, job2);
		assertThat(job.hashCode(), is(job2.hashCode()));
		assertThat(job.toString(), is(job2.toString()));
	}

}
