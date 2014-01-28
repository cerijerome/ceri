package ceri.ci.audio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Event;
import ceri.ci.build.Job;

/**
 * Checks a build for jobs that have just been broken, are still broken, or have just been fixed.
 * Builds should be summarized before using this class.
 */
public class JobAnalyzer {
	public final Collection<Job> justBroken;
	public final Collection<Job> stillBroken;
	public final Collection<Job> justFixed;
	
	/**
	 * Analyzes current and previous summarized builds.
	 */
	public JobAnalyzer(Build latestBuild, Build previousBuild) {
		Collection<Job> justBroken = new ArrayList<>();
		Collection<Job> stillBroken = new ArrayList<>();
		Collection<Job> justFixed = new ArrayList<>();
		for (Job latestJob : latestBuild.jobs) {
			Job previousJob = previousBuild.job(latestJob.name);
			Event latestEvent = BuildUtil.latestEvent(latestJob);
			if (latestEvent == null) continue;
			Event.Type latestType = type(latestEvent);
			Event.Type previousType = type(BuildUtil.latestEvent(previousJob));
			if (justBroken(latestType, previousType)) justBroken.add(latestJob);
			else if (stillBroken(latestType, previousType)) stillBroken.add(latestJob);
			else if (justFixed(latestType, previousType)) justFixed.add(latestJob);
		}
		this.justBroken = Collections.unmodifiableCollection(justBroken);
		this.stillBroken = Collections.unmodifiableCollection(stillBroken);
		this.justFixed = Collections.unmodifiableCollection(justFixed);
	}

	private Event.Type type(Event event) {
		if (event == null) return Event.Type.fixed;
		return event.type;
	}

	private boolean justBroken(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.broken && previousType == Event.Type.fixed;
	}
	
	private boolean stillBroken(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.broken && previousType == Event.Type.broken;
	}
	
	private boolean justFixed(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.fixed && previousType == Event.Type.broken;
	}
	
}
