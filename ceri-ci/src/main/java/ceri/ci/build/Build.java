package ceri.ci.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Build {
	private final Map<String, Job> mutableJobs = new TreeMap<>();
	public final Collection<Job> jobs = Collections.unmodifiableCollection(mutableJobs.values());
	public final String name;

	public Build(String name) {
		this.name = name;
	}

	public Build(Build build) {
		this(build.name);
		for (Job job : build.jobs)
			add(new Job(job));
	}

	public Job job(String name) {
		Job job = mutableJobs.get(name);
		if (job == null) {
			job = new Job(name);
			add(job);
		}
		return job;
	}

	public void clear() {
		for (Job job : jobs)
			job.clear();
	}

	public void purge() {
		for (Job job : jobs)
			job.purge();
	}

	private void add(Job job) {
		mutableJobs.put(job.name, job);
	}

}
