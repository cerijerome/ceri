package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.io.IoUtil;

public class AudioAlerter {
	private final AudioMessage message;
	private final long reminderMs;
	private final long shutdownMs;
	private volatile Builds summarizedBuilds;

	public static void main(String[] args) throws Exception {
		File dir = IoUtil.getPackageDir(AudioAlerter.class);
		float pitch = 1.3f;
		AudioAlerter alerter = builder(dir).pitch(pitch).build();
		Builds builds = new Builds();
		builds.build("bolt").job("smoke").event(Event.broken("shuochen", "dxie"));
		alerter.alert(builds);
	}

	public static class Builder {
		final File soundDir;
		float pitch = AudioAlerterProperties.PITCH_DEF;
		long reminderMs = AudioAlerterProperties.REMINDER_MS_DEF;
		long shutdownMs = AudioAlerterProperties.SHUTDOWN_MS_DEF;

		Builder(File soundDir) {
			this.soundDir = soundDir;
		}

		public Builder reminderMs(long reminderMs) {
			this.reminderMs = reminderMs;
			return this;
		}

		public Builder shutdownMs(long shutdownMs) {
			this.shutdownMs = shutdownMs;
			return this;
		}

		public Builder pitch(float pitch) {
			this.pitch = pitch;
			return this;
		}

		public AudioAlerter build() {
			return new AudioAlerter(this);
		}
	}

	public static Builder builder(File soundDir) {
		return new Builder(soundDir);
	}

	public static AudioAlerter create(Properties properties, String prefix) {
		AudioAlerterProperties audioProperties = new AudioAlerterProperties(properties, prefix);
		File dir = IoUtil.getPackageDir(AudioAlerter.class);
		return builder(dir).reminderMs(audioProperties.reminderMs()).shutdownMs(
			audioProperties.shutdownreminderMs()).pitch(audioProperties.pitch()).build();
	}

	AudioAlerter(Builder builder) {
		message = new AudioMessage(builder.soundDir, builder.pitch);
		reminderMs = builder.reminderMs;
		shutdownMs = builder.shutdownMs;
		clear();
	}

	/**
	 * For each build, check which jobs are just broken, still broken and just fixed.
	 * Gives an audio message for each of these cases.
	 */
	public void alert(Builds summarizedBuilds) {
		Builds previousBuilds = this.summarizedBuilds;
		this.summarizedBuilds = summarizedBuilds;
		try {
			for (Build latestBuild : summarizedBuilds.builds) {
				Build previousBuild = previousBuilds.build(latestBuild.name);
				JobAnalyzer analyzer = new JobAnalyzer(latestBuild, previousBuild);
				playJustBroken(latestBuild.name, analyzer.justBroken);
				playStillBroken(latestBuild.name, analyzer.stillBroken);
				playJustFixed(latestBuild.name, analyzer.justFixed);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears build state.
	 */
	public void clear() {
		this.summarizedBuilds = new Builds();
	}

	private void playJustBroken(String buildName, Collection<Job> jobs) throws IOException {
		if (!jobs.isEmpty()) return;
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
			message.playJustBroken(buildName, job.name, event.names);
		}
	}
	
}
