package ceri.ci.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Keeps track of builds, and analyzes the changes to jobs after an update. Not thread-safe.
 */
public class BuildAnalyzer {
	private Builds builds;
	private Collection<AnalyzedJob> analyzedJobs;

	public BuildAnalyzer() {
		clear();
	}

	/**
	 * Updates the build state and returns the analyzed job information. For each build, it checks
	 * which jobs are just broken, still broken and just fixed.
	 */
	public Collection<AnalyzedJob> update(Builds builds) {
		Builds previousBuilds = this.builds;
		this.builds = new Builds(builds);
		analyzedJobs = Collections.unmodifiableCollection(analyzeJobs(this.builds, previousBuilds));
		return analyzedJobs;
	}

	/**
	 * Returns the analyzed jobs from the latest update.
	 */
	public Collection<AnalyzedJob> analyzedJobs() {
		return analyzedJobs;
	}

	/**
	 * Returns a subset of the analyzed jobs that are still broken.
	 */
	public Collection<AnalyzedJob> stillBrokenJobs() {
		Collection<AnalyzedJob> stillBrokenJobs = new ArrayList<>();
		for (AnalyzedJob analyzedJob : analyzedJobs) {
			if (analyzedJob.stillBroken.isEmpty()) continue;
			stillBrokenJobs.add(analyzedJob);
		}
		return stillBrokenJobs;
	}

	/**
	 * Clears build state and analyzed jobs.
	 */
	public void clear() {
		builds = new Builds();
		analyzedJobs = Collections.emptySet();
	}

	private Collection<AnalyzedJob> analyzeJobs(Builds builds, Builds previousBuilds) {
		Collection<AnalyzedJob> analyzers = new ArrayList<>();
		for (Build build : builds) {
			Build previousBuild = previousBuilds.build(build.name);
			AnalyzedJob analyzer = new AnalyzedJob(build, previousBuild);
			if (analyzer.isEmpty()) continue;
			analyzers.add(analyzer);
		}
		return analyzers;
	}

}
