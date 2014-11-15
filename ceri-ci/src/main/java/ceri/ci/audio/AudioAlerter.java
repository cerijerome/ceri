package ceri.ci.audio;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.AnalyzedJob;
import ceri.ci.build.BuildAnalyzer;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.ci.common.Alerter;

/**
 * Plays alarms and phrases when build events occur.
 */
public class AudioAlerter implements Alerter, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final AudioMessages messages;
	private final AudioListener listener;
	private BuildAnalyzer buildAnalyzer = new BuildAnalyzer();

	public AudioAlerter(AudioMessages messages) {
		this(messages, null);
	}
	
	public AudioAlerter(AudioMessages messages, AudioListener listener) {
		this.messages = messages;
		this.listener = listener;
		clear();
	}

	/**
	 * Interrupts AudioMessage processing. This is needed as InterruptedExceptions thrown after a
	 * thread.interrupt() are swallowed by audio library code.
	 */
	@Override
	public void close() {
		messages.interrupt();
	}

	/**
	 * For each build, check which jobs are just broken, still broken and just fixed. Gives an audio
	 * message for each of these cases. Grouped by build.
	 */
	@Override
	public void update(Builds builds) {
		logger.info("Audio update");
		analyze(builds);
	}

	/**
	 * Clears build state.
	 */
	@Override
	public void clear() {
		logger.info("Clearing state");
		buildAnalyzer.clear();
	}

	/**
	 * Alert reminder for builds still broken. Gives the still broken audio message for any jobs in
	 * a broken state.
	 */
	@Override
	public void remind() {
		logger.info("Audio reminder");
		analyze(null);
	}

	private void analyze(Builds builds) {
		Collection<AnalyzedJob> analyzedJobs = buildAnalyzer.update(builds);
		if (analyzedJobs.isEmpty()) return;
		try {
			audioStarted();
			messages.playRandomAlarm();
			voiceStarted();
			for (AnalyzedJob analyzedJob : analyzedJobs) {
				playJustBroken(analyzedJob.build, analyzedJob.justBroken);
				playStillBroken(analyzedJob.build, analyzedJob.stillBroken);
				playJustFixed(analyzedJob.build, analyzedJob.justFixed);
			}
		} catch (IOException e) {
			logger.catching(e);
		} finally {
			audioEnded();
		}
	}

	private void audioStarted() {
		if (listener != null) listener.audioStart();
	}
	
	private void voiceStarted() {
		if (listener != null) listener.voiceStart();
	}
	
	private void audioEnded() {
		if (listener != null) listener.audioEnd();
	}
	
	private void playJustBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			messages.playJustBroken(buildName, job.name, event.names);
		}
	}

	private void playStillBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			messages.playStillBroken(buildName, job.name, event.names);
		}
	}

	private void playJustFixed(String buildName, Collection<Job> jobs) throws IOException {
		if (jobs.isEmpty()) return;
		for (Job job : jobs) {
			Event event = BuildUtil.latestEvent(job);
			messages.playJustFixed(buildName, job.name, event.names);
		}
	}

}
