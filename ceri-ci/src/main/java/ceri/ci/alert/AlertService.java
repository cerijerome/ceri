package ceri.ci.alert;

import ceri.ci.build.Build;
import ceri.ci.build.BuildEventProcessor;
import ceri.ci.build.Builds;
import ceri.ci.build.Job;

/**
 * Service that manages the state of builds, and calls update, remind, and clear on the alerter
 * group.
 */
public interface AlertService extends BuildEventProcessor {

	/**
	 * Purges older events from the builds. TODO: put on timer, or based on event count
	 */
	void purge();

	/**
	 * Returns a copy of the state of all builds.
	 */
	Builds builds();

	/**
	 * Returns a copy of the state of the specified build.
	 */
	Build build(String build);

	/**
	 * Returns a copy of the state of the specified job.
	 */
	Job job(String build, String job);

	/**
	 * Clears events from jobs. If build is null, all events are cleared. If job is null, all events
	 * for the build are cleared. Otherwise only events for job are cleared.
	 */
	void clear(String build, String job);

	/**
	 * Deletes builds and/or jobs. If build is null, all builds are deleted. If job is null, all
	 * jobs for the build are deleted. Otherwise only the job specified is deleted.
	 */
	void delete(String build, String job);

}
