package ceri.ci.build;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility methods for testing build classes.
 */
public class BuildTestUtil {

	private BuildTestUtil() {}

	/**
	 * Create an event
	 */
	public static Event event(Event.Type type, long timeStamp, String... names) {
		return new Event(type, timeStamp, names);
	}

	/**
	 * Checks event fields except for time-stamp.
	 */
	public static void assertEvent(Event lhs, Event rhs) {
		assertEvent(lhs, rhs.type, rhs.names);
	}

	/**
	 * Checks event fields except for time-stamp.
	 */
	public static void assertEvent(Event event, Event.Type type, String... names) {
		assertEvent(event, type, Arrays.asList(names));
	}

	/**
	 * Checks event fields except for time-stamp.
	 */
	public static void assertEvent(Event event, Event.Type type, Collection<String> names) {
		assertThat(event.type, is(type));
		assertCollection(event.names, names);
	}

	/**
	 * Checks event fields except for time-stamp.
	 */
	public static void assertEvents(Collection<Event> lhs, Event... rhs) {
		assertEvents(lhs, Arrays.asList(rhs));
	}

	/**
	 * Checks event fields except for time-stamp.
	 */
	public static void assertEvents(Collection<Event> lhs, Collection<Event> rhs) {
		assertThat("Expected " + rhs.size() + " events but count is " + lhs.size(), lhs.size(),
			is(rhs.size()));
		Iterator<Event> iLhs = lhs.iterator();
		Iterator<Event> iRhs = rhs.iterator();
		int i = 0;
		while (iLhs.hasNext()) {
			try {
				assertEvent(iLhs.next(), iRhs.next());
				i++;
			} catch (AssertionError e) {
				throw new AssertionError("Unexpected value for event at index " + i, e);
			}
		}
	}

	/**
	 * Checks build names in order.
	 */
	public static void assertBuildNames(Iterable<Build> builds, String... names) {
		assertIterable(buildNames(builds), names);
	}

	/**
	 * Checks job names in order.
	 */
	public static void assertJobNames(Iterable<Job> jobs, String... names) {
		assertIterable(jobNames(jobs), names);
	}

	private static Collection<String> buildNames(Iterable<Build> builds) {
		Collection<String> names = new ArrayList<>();
		for (Build build : builds)
			names.add(build.name);
		return names;
	}

	private static Collection<String> jobNames(Iterable<Job> jobs) {
		Collection<String> names = new ArrayList<>();
		for (Job job : jobs)
			names.add(job.name);
		return names;
	}

}
