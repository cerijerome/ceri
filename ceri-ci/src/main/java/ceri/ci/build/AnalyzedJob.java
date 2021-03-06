package ceri.ci.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ceri.common.text.ToString;

/**
 * Checks a build for jobs that have just been broken, are still broken, or have just been fixed.
 * Builds should be summarized before using this class.
 */
public class AnalyzedJob {
	public final String build;
	public final Collection<Job> justBroken;
	public final Collection<Job> stillBroken;
	public final Collection<Job> justFixed;

	/**
	 * Analyzes current and previous summarized builds.
	 */
	public AnalyzedJob(Build latestBuild, Build previousBuild) {
		build = latestBuild.name;
		if (!Objects.equals(build, previousBuild.name)) throw new IllegalArgumentException(
			"Build names do not match: " + build + ", " + previousBuild.name);
		List<Job> justBroken = new ArrayList<>();
		List<Job> stillBroken = new ArrayList<>();
		List<Job> justFixed = new ArrayList<>();
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
		this.justBroken = Collections.unmodifiableList(justBroken);
		this.stillBroken = Collections.unmodifiableList(stillBroken);
		this.justFixed = Collections.unmodifiableList(justFixed);
	}

	public boolean isEmpty() {
		return justBroken.isEmpty() && stillBroken.isEmpty() && justFixed.isEmpty();
	}

	@Override
	public int hashCode() {
		return Objects.hash(build, justBroken, stillBroken, justFixed);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AnalyzedJob)) return false;
		AnalyzedJob other = (AnalyzedJob) obj;
		if (!Objects.equals(build, other.build)) return false;
		if (!Objects.equals(justBroken, other.justBroken)) return false;
		if (!Objects.equals(stillBroken, other.stillBroken)) return false;
		if (!Objects.equals(justFixed, other.justFixed)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.ofClass(this, build).children(justBroken, stillBroken, justFixed)
			.toString();
	}

	private Event.Type type(Event event) {
		if (event == null) return Event.Type.success;
		return event.type;
	}

	private boolean justBroken(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.failure && previousType == Event.Type.success;
	}

	private boolean stillBroken(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.failure && previousType == Event.Type.failure;
	}

	private boolean justFixed(Event.Type latestType, Event.Type previousType) {
		return latestType == Event.Type.success && previousType == Event.Type.failure;
	}

}
