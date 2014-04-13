package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.log.LogUtil;

public class AudioMessage {
	private static final Logger logger = LogManager.getLogger();
	private static final String AUDIO_FILE_SUFFIX = ".wav";
	private final File soundDir;
	private final float pitch;
	private final AudioPlayer player;
	// Needed as audio library code swallows InterruptedExceptions
	private volatile boolean interrupted = false;

	public AudioMessage(AudioPlayer player, File soundDir) {
		this(player, soundDir, Audio.NORMAL_PITCH);
	}

	public AudioMessage(AudioPlayer player, File soundDir, float pitch) {
		this.player = player;
		this.soundDir = soundDir;
		this.pitch = pitch;
	}

	public void interrupt() {
		interrupted = true;
	}
	
	/**
	 * Plays random alarm sound.
	 */
	public void playAlarm() throws IOException {
		checkRuntimeInterrupted();
		int index = (int)(Math.random() * AudioClip.values().length);
		AudioClip clip = AudioClip.values()[index];
		player.play(clip.load());
	}

	/**
	 * Plays "<build> <job> has just been broken by <names>"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.has_just_been_broken);
		if (audioExists(names)) {
			play(AudioPhrase.by);
			playNames(names);
		}
	}

	/**
	 * Plays "<build> <job> is still broken. <names> please take a look."
	 */
	public void playStillBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_still_broken);
		playNames(names);
		play(AudioPhrase.please_fix_it);
	}

	/**
	 * Plays "<build> <job> is now fixed thanks to <names>."
	 */
	public void playJustFixed(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_now_fixed);
		if (audioExists(names)) {
			play(AudioPhrase.thanks_to);
			playNames(names);
		} else play(AudioPhrase.thank_you);
	}

	private void playBuildJob(String build, String job) throws IOException {
		if (!audioExists(build)) {
			play(AudioPhrase.the_build);
			return;
		}
		play(build);
		if (!audioExists(job)) play(AudioPhrase.job);
		else play(job);
	}

	private void playNames(Collection<String> names) throws IOException {
		int i = names.size();
		for (String name : names) {
			play(name);
			if (--i == 1) play(AudioPhrase.and);
		}
	}

	private File audioFile(String key) {
		return new File(soundDir, key + AUDIO_FILE_SUFFIX);
	}

	private boolean audioExists(String... keys) {
		return audioExists(Arrays.asList(keys));
	}

	private boolean audioExists(Collection<String> keys) {
		if (keys.isEmpty()) return false;
		for (String key : keys)
			if (!audioFile(key).exists()) return false;
		return true;
	}

	private void play(String key) throws IOException {
		File file = audioFile(key);
		if (!file.exists()) {
			logger.warn("No sound file for {}", key);
			return;
		}
		logger.debug("Speech: {}", key);
		play(Audio.create(file));
	}

	private void play(AudioPhrase phrase) throws IOException {
		logger.debug("Speech: {}", LogUtil.toString(() -> phrase.name()));
		play(loadPhrase(phrase));
	}

	private void play(Audio audio) throws IOException {
		checkRuntimeInterrupted();
		player.play(audio.changePitch(pitch));
	}

	private Audio loadPhrase(AudioPhrase phrase) throws IOException {
		return Audio.create(new File(soundDir, phrase.filename));
	}

	private void checkRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		if (interrupted) {
			interrupted = false;
			throw new RuntimeInterruptedException("Thread has been interrupted");
		}
	}
	
}
