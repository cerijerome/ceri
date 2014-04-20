package ceri.ci.common;

import ceri.ci.build.Builds;

/**
 * Interface for alerter components.
 */
public interface Alerter {

	/**
	 * Called when a new build event occurs. The builds are summarized and only contain the last
	 * success and fail events per job.
	 */
	void update(Builds builds);

	/**
	 * Called to clear all state of the alerter.
	 */
	void clear();

	/**
	 * Called after a time period if no updates have occurred.
	 */
	void remind();

}
