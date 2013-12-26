package ceri.ci.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.event.EventListener;
import ceri.common.event.EventListenerSupport;
import ceri.common.util.ToStringHelper;

/**
 * Stateful class tracking jobs.
 */
public class JobCentral implements JobService {
	private final Map<String, Job> jobs = new TreeMap<>();
	private final EventListenerSupport<Collection<Job>> listeners;

	public static class Builder {
		final EventListenerSupport.Builder<Collection<Job>> listeners = EventListenerSupport.builder();

		Builder() {}

		public Builder listener(EventListener<Collection<Job>> listener) {
			listeners.listener(listener);
			return this;
		}

		public JobCentral build() {
			return new JobCentral(this);
		}
	}

	JobCentral(Builder builder) {
		listeners = builder.listeners.build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public Collection<Job> jobs() {
		return new LinkedHashSet<>(jobs.values());
	}
	
	@Override
	public void broken(String jobName, Collection<String> responsible) {
		event(jobName, Event.Type.broken, responsible);
	}

	@Override
	public void fixed(String jobName, Collection<String> responsible) {
		event(jobName, Event.Type.fixed, responsible);
	}

	@Override
	public void clear() {
		for (String name : jobs.keySet())
			put(Job.create(name));
		listeners.event(jobs());
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, jobs).toString();
	}
	
	private void event(String jobName, Event.Type type, Collection<String> responsible) {
		Event event = Event.builder(type).responsible(responsible).build();
		Job job = jobs.get(jobName);
		if (job == null) job = Job.create(jobName, event);
		else job = job.createUpdate(event);
		put(job);
		listeners.event(jobs());
	}

	private void put(Job job) {
		jobs.put(job.name, job);
	}

	public static void main(String[] args) {
		EventListener<Collection<Job>> listener1 = new EventListener<Collection<Job>>() {
			@Override
			public void event(Collection<Job> jobs) {
				System.out.println("listener1: " + jobs);
			}
		};
		EventListener<Collection<Job>> listener2 = new EventListener<Collection<Job>>() {
			@Override
			public void event(Collection<Job> jobs) {
				System.out.println("listener2: " + jobs);
			}
		};
		JobCentral jc = JobCentral.builder().listener(listener1).listener(listener2).build();
		jc.clear();
		jc.broken("a", Arrays.asList("ceri1a"));
		jc.broken("c", Arrays.asList("ceri3a", "ceri3b"));
		jc.fixed("a", Arrays.asList("ceri1b"));
	}
	
}
