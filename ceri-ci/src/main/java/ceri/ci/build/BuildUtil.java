package ceri.ci.build;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class BuildUtil {

	private BuildUtil() {}

	/**
	 * Counts the total number of events stored by all builds.
	 */
	public static int countEvents(Builds builds) {
		int count = 0;
		for (Build build : builds.builds) count += countEvents(build);
		return count;
	}
	
	/**
	 * Counts the total number of events stored by the build.
	 */
	public static int countEvents(Build build) {
		int count = 0;
		for (Job job : build.jobs) count += job.events.size();
		return count;
	}
	
	/**
	 * Copies the summarized last break names of all builds into given
	 * collection. Given Builds should already be summarized.
	 */
	public static Collection<String> summarizedBreakNames(Builds summarizedBuilds) {
		Collection<String> names = new HashSet<>();
		for (Build build : summarizedBuilds.builds) {
			breakNames(build, names);
		}
		return names;
	}

	/**
	 * Copies the summarized last break names of all jobs in the build into
	 * given collection.
	 */
	private static void breakNames(Build summarizedBuild, Collection<String> names) {
		for (Job job : summarizedBuild.jobs)
			breakNames(job, names);
	}

	/**
	 * Copies the summarized last break names of the job into given collection.
	 */
	private static void breakNames(Job summarizedJob, Collection<String> names) {
		if (summarizedJob.events.isEmpty()) return;
		Event event = latestEvent(summarizedJob);
		if (event.type == Event.Type.failure) names.addAll(event.names);
	}

	/**
	 * Summarizes all build job events into last fix and last break events. 
	 */
	public static Builds summarize(Builds fromBuilds) {
		Builds toBuilds = new Builds();
		for (Build build : fromBuilds.builds)
			summarize(build, toBuilds.build(build.name));
		return toBuilds;
	}

	/**
	 * Summarizes all jobs for the build into toBuild, which is expected to be
	 * empty.
	 */
	private static void summarize(Build fromBuild, Build toBuild) {
		for (Job job : fromBuild.jobs)
			summarize(job, toBuild.job(job.name));
	}

	/**
	 * Summarizes job events into the last break and last fix events. The break
	 * event is the earliest event of the last break sequence, with an
	 * aggregation of all names. The fix event is the earliest event of the last
	 * fix sequence. Events are added to toJob, which is expected to be empty.
	 */
	private static void summarize(Job fromJob, Job toJob) {
		Job job = new Job(fromJob);
		job.purge();
		Event lastBreak = aggregate(Event.Type.failure, job.events);
		Event lastFix = earliest(Event.Type.success, job.events);
		if (lastBreak != null) toJob.event(lastBreak);
		if (lastFix != null) toJob.event(lastFix);
	}

	/**
	 * Finds the earliest event of given type from the events.
	 */
	public static Event earliest(Event.Type type, Event... events) {
		return earliest(type, Arrays.asList(events));
	}

	/**
	 * Finds the earliest event of given type from the events.
	 */
	public static Event earliest(Event.Type type, Collection<Event> events) {
		long t = Long.MAX_VALUE;
		Event earliest = null;
		for (Event event : events) {
			if (event.type != type) continue;
			if (event.timeStamp > t) continue;
			t = event.timeStamp;
			earliest = event;
		}
		return earliest;
	}

	/**
	 * Returns the latest event from given job, or null if no events.
	 */
	public static Event latestEvent(Job job) {
		if (job.events.isEmpty()) return null;
		return job.events.iterator().next();
	}

	/**
	 * Finds the latest event of given type from the events.
	 */
	public static Event latest(Event.Type type, Event... events) {
		return latest(type, Arrays.asList(events));
	}

	/**
	 * Finds the latest event of given type from the events.
	 */
	public static Event latest(Event.Type type, Collection<Event> events) {
		long t = Long.MIN_VALUE;
		Event latest = null;
		for (Event event : events) {
			if (event.type != type) continue;
			if (event.timeStamp < t) continue;
			t = event.timeStamp;
			latest = event;
		}
		return latest;
	}

	/**
	 * Aggregates the events of given type into a single event. The earliest
	 * time-stamp is used, and all unique names are combined. If no events of
	 * the given type are found null is returned.
	 */
	public static Event aggregate(Event.Type type, Event... events) {
		return aggregate(type, Arrays.asList(events));
	}

	/**
	 * Aggregates the events of given type into a single event. The earliest
	 * time-stamp is used, and all unique names are combined. If no events of
	 * the given type are found null is returned.
	 */
	public static Event aggregate(Event.Type type, Collection<Event> events) {
		boolean found = false;
		long t = Long.MAX_VALUE;
		Collection<String> names = new LinkedHashSet<>();
		for (Event event : events) {
			if (event.type != type) continue;
			found = true;
			if (event.timeStamp < t) t = event.timeStamp;
			names.addAll(event.names);
		}
		if (!found) return null;
		return new Event(type, t, names);
	}

}
