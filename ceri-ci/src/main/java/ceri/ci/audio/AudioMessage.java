package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;

public class AudioMessage {
	private static final Logger logger = LogManager.getLogger();
	private static final String AUDIO_FILE_SUFFIX = ".wav";
	private static final String BUILD_DIR = "build";
	private static final String JOB_DIR = "job";
	private static final String NAME_DIR = "name";
	private static final String PHRASE_DIR = "phrase";
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

	/**
	 * Interrupt the thread playing audio.
	 */
	public void interrupt() {
		interrupted = true;
	}

	/**
	 * Play a random alarm sound.
	 */
	public void playAlarm() throws IOException {
		checkRuntimeInterrupted();
		int index = (int) (Math.random() * AudioClip.values().length);
		AudioClip clip = AudioClip.values()[index];
		logger.debug("Alarm: {}", clip.name());
		player.play(clip.load());
	}

	/**
	 * Plays "<build> <job> has just been broken [by <names>]"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.has_just_been_broken);
		Collection<File> nameFiles = verifyAll(files(NAME_DIR, names));
		if (nameFiles.isEmpty()) return;
		play(AudioPhrase.by);
		play(nameFiles);
	}

	/**
	 * Plays "<build> <job> is still broken. [<names> please fix it.]"
	 */
	public void playStillBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_still_broken);
		Collection<File> nameFiles = verifyAll(files(NAME_DIR, names));
		if (nameFiles.isEmpty()) return;
		play(nameFiles);
		play(AudioPhrase.please_fix_it);
	}

	/**
	 * Plays "<build> <job> is now fixed [thanks to <names> | thank you]."
	 */
	public void playJustFixed(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_now_fixed);
		Collection<File> nameFiles = verifyAll(files(NAME_DIR, names));
		if (!nameFiles.isEmpty()) {
			play(AudioPhrase.thanks_to);
			play(nameFiles);
		} else play(AudioPhrase.thank_you);
	}

	private void playBuildJob(String build, String job) throws IOException {
		if (playBuild(build)) playJob(job);
	}

	private boolean playBuild(String build) throws IOException {
		File file = file(BUILD_DIR, build);
		boolean verified = verify(file);
		if (verified) play(file);
		else play(AudioPhrase.the_build);
		return verified;
	}

	private boolean playJob(String job) throws IOException {
		File file = file(JOB_DIR, job);
		boolean verified = verify(file);
		if (verified) play(file);
		return verified;
	}

	private void play(Collection<File> files) throws IOException {
		int i = files.size();
		for (File file : files) {
			play(file);
			if (--i == 1) play(AudioPhrase.and);
		}
	}

	private void play(AudioPhrase phrase) throws IOException {
		play(file(PHRASE_DIR, phrase.name()));
	}

	private void play(File file) throws IOException {
		if (!verify(file)) return;
		logger.debug("Speech: {}", file.getName());
		checkRuntimeInterrupted();
		Audio audio = Audio.create(file);
		player.play(audio.changePitch(pitch));
	}

	private Collection<File> files(String dir, Collection<String> keys) {
		Collection<File> files = new ArrayList<>();
		for (String key : keys) {
			File file = file(dir, key);
			files.add(file);
		}
		return files;
	}

	private File file(String dir, String key) {
		return new File(soundDir, dir + "/" + key + AUDIO_FILE_SUFFIX);
	}

	private Collection<File> verifyAll(Collection<File> files) throws IOException {
		Collection<File> verifiedFiles = new ArrayList<>();
		for (File file : files)
			if (verify(file)) verifiedFiles.add(file);
		return verifiedFiles;
	}

	private boolean verify(File file) throws IOException {
		if (file.exists()) return true;
		logger.warn("Missing sound file {}", file.getCanonicalPath());
		return false;
	}

	private void checkRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		if (interrupted) {
			interrupted = false;
			throw new RuntimeInterruptedException("Thread has been interrupted");
		}
	}

}
