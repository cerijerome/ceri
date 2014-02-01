package ceri.ci.audio;

import java.io.IOException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;

public class AudioAlerter {
	private static final Logger logger = LogManager.getLogger();
	private final AudioMessage message;
	private volatile Builds summarizedBuilds;

	public AudioAlerter(AudioMessage message) {
		this.message = message;
		clear();
	}

	/**
	 * For each build, check which jobs are just broken, still broken and just
	 * fixed. Gives an audio message for each of these cases.
	 */
	public void alert(Builds summarizedBuilds) {
		Builds previousBuilds = this.summarizedBuilds;
		this.summarizedBuilds = new Builds(summarizedBuilds);
		try {
			for (Build latestBuild : this.summarizedBuilds.builds) {
				Build previousBuild = previousBuilds.build(latestBuild.name);
				JobAnalyzer analyzer = new JobAnalyzer(latestBuild, previousBuild);
				playJustBroken(latestBuild.name, analyzer.justBroken);
				playStillBroken(latestBuild.name, analyzer.stillBroken);
				playJustFixed(latestBuild.name, analyzer.justFixed);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Clears build state.
	 */
	public void clear() {
		this.summarizedBuilds = new Builds();
	}

	/**
	 * Alert reminder for builds still broken. Gives the still broken audio
	 * message for any jobs in a broken state.
	 */
	public void remind() {
		try {
			for (Build latestBuild : summarizedBuilds.builds) {
				JobAnalyzer analyzer = new JobAnalyzer(latestBuild, new Build(latestBuild.name));
				playStillBroken(latestBuild.name, analyzer.justBroken);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private void playJustBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		message.playAlarm();
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playJustBroken(buildName, job.name, event.names);
		}
	}

	private void playStillBroken(String buildName, Collection<Job> jobs) throws IOException {
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playStillBroken(buildName, job.name, event.names);
		}
	}

	private void playJustFixed(String buildName, Collection<Job> jobs) throws IOException {
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			message.playJustFixed(buildName, job.name, event.names);
		}
	}

}
