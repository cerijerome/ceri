package ceri.ci.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Keeps track of builds, and analyzes the changes to jobs after an update. Not thread-safe.
 */
public class BuildAnalyzer {
	private Builds summarizedBuilds;
	private Collection<AnalyzedJob> analyzedJobs;

	public BuildAnalyzer() {
		clear();
	}

	public Collection<AnalyzedJob> update() {
		return update(null);
	}

	/**
	 * Updates the build state and returns the analyzed job information. For each build, it checks
	 * which jobs are just broken, still broken and just fixed.
	 */
	public Collection<AnalyzedJob> update(Builds builds) {
		if (builds == null) builds = this.summarizedBuilds;
		else builds = BuildUtil.summarize(builds);
		Builds previousBuilds = this.summarizedBuilds;
		this.summarizedBuilds = new Builds(builds);
		analyzedJobs =
			Collections.unmodifiableCollection(analyzeJobs(this.summarizedBuilds, previousBuilds));
		return analyzedJobs;
	}

	/**
	 * Returns the analyzed jobs from the latest update.
	 */
	public Collection<AnalyzedJob> analyzedJobs() {
		return analyzedJobs;
	}

	/**
	 * Clears build state and analyzed jobs.
	 */
	public void clear() {
		summarizedBuilds = new Builds();
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
