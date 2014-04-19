package ceri.ci.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.log.LogUtil;

public class AudioAlerter {
	private static final Logger logger = LogManager.getLogger();
	private final AudioMessage message;
	private volatile Builds summarizedBuilds;

	public AudioAlerter(AudioMessage message) {
		this.message = message;
		clear();
	}

	/**
	 * Interrupts AudioMessage processing. This is needed as InterruptedExceptions thrown after a
	 * thread.interrupt() are swallowed by audio library code.
	 */
	public void interrupt() {
		message.interrupt();
	}

	/**
	 * For each build, check which jobs are just broken, still broken and just fixed. Gives an audio
	 * message for each of these cases. Grouped by build.
	 */
	public void alert(Builds summarizedBuilds) {
		logger.debug("alert: {}", LogUtil.compact(summarizedBuilds));
		Builds previousBuilds = this.summarizedBuilds;
		this.summarizedBuilds = new Builds(summarizedBuilds);
		try {
			Collection<JobAnalyzer> analyzers = analyzeJobs(this.summarizedBuilds, previousBuilds);
			if (analyzers.isEmpty()) return;
			message.playAlarm();
			for (JobAnalyzer analyzer : analyzers) {
				playJustBroken(analyzer.build, analyzer.justBroken);
				playStillBroken(analyzer.build, analyzer.stillBroken);
				playJustFixed(analyzer.build, analyzer.justFixed);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Clears build state.
	 */
	public void clear() {
		logger.debug("clear");
		this.summarizedBuilds = new Builds();
	}

	/**
	 * Alert reminder for builds still broken. Gives the still broken audio message for any jobs in
	 * a broken state.
	 */
	public void remind() {
		logger.debug("remind");
		try {
			Collection<JobAnalyzer> analyzers = analyzeStillBrokenJobs(summarizedBuilds);
			if (analyzers.isEmpty()) return;
			message.playAlarm();
			for (JobAnalyzer analyzer : analyzers) {
				playStillBroken(analyzer.build, analyzer.stillBroken);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private Collection<JobAnalyzer> analyzeJobs(Builds builds, Builds previousBuilds) {
		Collection<JobAnalyzer> analyzers = new ArrayList<>();
		for (Build build : builds) {
			Build previousBuild = previousBuilds.build(build.name);
			JobAnalyzer analyzer = new JobAnalyzer(build, previousBuild);
			if (analyzer.isEmpty()) continue;
			analyzers.add(analyzer);
		}
		return analyzers;
	}

	private Collection<JobAnalyzer> analyzeStillBrokenJobs(Builds builds) {
		Collection<JobAnalyzer> analyzers = new ArrayList<>();
		for (Build build : builds) {
			JobAnalyzer analyzer = new JobAnalyzer(build, build);
			if (analyzer.stillBroken.isEmpty()) continue;
			analyzers.add(analyzer);
		}
		return analyzers;
	}

	private void playJustBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playJustBroken(buildName, job.name, event.names);
		}
	}

	private void playStillBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playStillBroken(buildName, job.name, event.names);
		}
	}

	private void playJustFixed(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playJustFixed(buildName, job.name, event.names);
		}
	}

}
