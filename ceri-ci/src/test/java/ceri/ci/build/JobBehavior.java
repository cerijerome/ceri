package ceri.ci.build;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class JobBehavior {
	
	@Test
	public void shouldStoreEventsInDecreasingChronologicalOrder() {
		Event e0 = new Event(Event.Type.broken, 0);
		Event e1 = new Event(Event.Type.fixed, 1);
		Event e2 = new Event(Event.Type.broken, 2);
		Job job = new Job("test");
		job.event(e1, e0, e2);
		assertElements(job.events, e2, e1, e0);
	}

	@Test
	public void shouldNotAllowModificationOfEventsField() {
		final Job job = new Job("test");
		assertTrue(job.events.isEmpty());
		assertException(new Runnable() {
			@Override
			public void run() {
				job.events.add(Event.fixed());
			}
		});
	}

	@Test
	public void shouldPurgeBreakAndFixEventsUpToLatestSequences() {
		Event e0 = new Event(Event.Type.broken, 0);
		Event e1 = new Event(Event.Type.fixed, 1, "a1");
		Event e2 = new Event(Event.Type.fixed, 2, "b1", "b2");
		Event e3 = new Event(Event.Type.broken, 3, "c1", "c2", "c3");
		Event e4 = new Event(Event.Type.broken, 4);
		Event e5 = new Event(Event.Type.broken, 5, "e1", "e2", "e3", "e4");
		Event e6 = new Event(Event.Type.fixed, 6);
		Event e7 = new Event(Event.Type.fixed, 7, "g1");
		
		Job job = new Job("test");
		job.event(e0, e1, e2, e3, e4, e5, e6, e7);
		Job job2 = new Job("test");
		job2.event(e3, e4, e5, e6, e7);
		job.purge();
		assertElements(job.events, job2.events);
	}

	@Test
	public void shouldNotPurgeLatestSequencesOfBreakAndFixEvents() {
		Job job = new Job("test");
		job.event(Event.fixed("a"));
		job.event(Event.fixed("b1", "b2"));
		job.event(Event.broken("c1", "c2", "c3"));
		job.event(Event.broken());
		Job job2 = new Job(job);
		job.purge();
		assertElements(job.events, job2.events);
	}

	@Test
	public void shouldClearEvents() {
		Job job = new Job("test");
		job.event(Event.fixed("a"));
		job.event(Event.broken("b1", "b2"));
		assertFalse(job.events.isEmpty());
		job.clear();
		assertTrue(job.events.isEmpty());
	}

	@Test
	public void shouldCopyAllFieldsWithCopyConstructor() {
		Job job1 = new Job("test");
		job1.event(Event.fixed("a1", "a2"));
		job1.event(Event.broken());
		Job job2 = new Job(job1);
		assertThat(job1.name, is(job2.name));
		assertElements(job1.events, job2.events);
		assertTrue(job1.events != job2.events);
	}
	
}
