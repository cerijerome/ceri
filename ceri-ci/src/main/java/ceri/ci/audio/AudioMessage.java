package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.FileLib;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;

public class AudioMessage {
	private static final Logger logger = LogManager.getLogger();
	private static final Collection<String> EXT_DEF = Arrays.asList("wav", "mp3");
	private static final String CLIP_DIR = "clip";
	private static final String BUILD_DIR = "build";
	private static final String JOB_DIR = "job";
	private static final String NAME_DIR = "name";
	private static final String PHRASE_DIR = "phrase";
	private final List<String> clipKeys;
	private final FileLib clips;
	private final FileLib builds;
	private final FileLib jobs;
	private final FileLib names;
	private final FileLib phrases;
	private final float pitch;
	private final AudioPlayer player;
	// Needed as audio library code swallows InterruptedExceptions
	private volatile boolean interrupted = false;

	public AudioMessage(AudioPlayer player, File soundDir) throws IOException {
		this(player, soundDir, Audio.NORMAL_PITCH);
	}

	public AudioMessage(AudioPlayer player, File soundDir, float pitch) throws IOException {
		this.player = player;
		this.pitch = pitch;
		File clipDir = clipDir();
		clips = new FileLib(clipDir, EXT_DEF);
		clipKeys = ImmutableUtil.copyAsList(clips.keys());
		builds = new FileLib(new File(soundDir, BUILD_DIR), EXT_DEF);
		jobs = new FileLib(new File(soundDir, JOB_DIR), EXT_DEF);
		names = new FileLib(new File(soundDir, NAME_DIR), EXT_DEF);
		phrases = new FileLib(new File(soundDir, PHRASE_DIR), EXT_DEF);
		// Check all phrases exist
		for (AudioPhrase phrase : AudioPhrase.values())
			phrases.file(phrase.name());
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
	public void playRandomAlarm() throws IOException {
		checkRuntimeInterrupted();
		int index = (int) (Math.random() * clipKeys.size());
		String key = clipKeys.get(index);
		File file = clips.file(key);
		logger.debug("Alarm: {}", file.getName());
		checkRuntimeInterrupted();
		player.play(Audio.create(file));
	}

	/**
	 * Plays "<build> <job> has just been broken [by <names>]"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.has_just_been_broken);
		Collection<File> nameFiles = this.names.files(names);
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
		Collection<File> nameFiles = this.names.files(names);
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
		Collection<File> nameFiles = this.names.files(names);
		if (!nameFiles.isEmpty()) {
			play(AudioPhrase.thanks_to);
			play(nameFiles);
		} else play(AudioPhrase.thank_you);
	}

	private void playBuildJob(String build, String job) throws IOException {
		if (playBuild(build)) playJob(job);
	}

	private boolean playBuild(String build) throws IOException {
		File file = builds.file(build);
		if (file != null) play(file);
		else play(AudioPhrase.the_build);
		return file != null;
	}

	private boolean playJob(String job) throws IOException {
		File file = jobs.file(job);
		if (file != null) play(file);
		return file != null;
	}

	private void play(Collection<File> files) throws IOException {
		int i = files.size();
		for (File file : files) {
			play(file);
			if (--i == 1) play(AudioPhrase.and);
		}
	}

	private void play(AudioPhrase phrase) throws IOException {
		play(phrases.file(phrase.name()));
	}

	private void play(File file) throws IOException {
		logger.debug("Speech: {}", file.getName());
		checkRuntimeInterrupted();
		Audio audio = Audio.create(file);
		player.play(audio.changePitch(pitch));
	}

	private void checkRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		if (interrupted) {
			interrupted = false;
			throw new RuntimeInterruptedException("Thread has been interrupted");
		}
	}

	private File clipDir() throws IOException {
		try {
			return new File(getClass().getResource(CLIP_DIR).toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

}
