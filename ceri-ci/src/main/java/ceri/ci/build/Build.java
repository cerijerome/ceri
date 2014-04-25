package ceri.ci.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.util.BasicUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class Build implements Iterable<Job> {
	private transient final Map<String, Job> mutableJobs = new TreeMap<>();
	public final Collection<Job> jobs = Collections.unmodifiableCollection(mutableJobs.values());
	public final String name;

	public Build(String name) {
		if (BasicUtil.isEmpty(name)) throw new IllegalArgumentException("Name cannot be empty: " +
			name);
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

	public void delete(String name) {
		mutableJobs.remove(name);
	}

	public void purge() {
		for (Job job : jobs)
			job.purge();
	}

	private void add(Job job) {
		mutableJobs.put(job.name, job);
	}

	@Override
	public Iterator<Job> iterator() {
		return jobs.iterator();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, mutableJobs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Build)) return false;
		Build build = (Build) obj;
		return name.equals(build.name) && mutableJobs.equals(build.mutableJobs);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name).childrens(jobs).toString();
	}

}
