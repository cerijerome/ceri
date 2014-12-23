package ceri.ci.audio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.Resource;
import ceri.ci.common.ResourceMap;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;

public class AudioMessages {
	private static final Logger logger = LogManager.getLogger();
	private static final Collection<String> EXT_DEF = Arrays.asList("wav");
	private static final String CLIP_DIR = "clip/guardians";
	private static final String BUILD_DIR = "build";
	private static final String JOB_DIR = "job";
	private static final String NAME_DIR = "name";
	private static final String PHRASE_DIR = "phrase";
	private final Random random = new Random();
	private final List<String> clipKeys;
	private final ResourceMap clips;
	private final ResourceMap builds;
	private final ResourceMap jobs;
	private final ResourceMap names;
	private final ResourceMap phrases;
	private final float pitch;
	private final AudioPlayer player;
	// Needed as audio library code swallows InterruptedExceptions
	private volatile boolean interrupted = false;

	public AudioMessages(AudioPlayer player, Class<?> cls, String voiceDir) throws IOException {
		this(player, cls, voiceDir, Audio.NORMAL_PITCH);
	}

	public AudioMessages(AudioPlayer player, Class<?> cls, String voiceDir, float pitch)
		throws IOException {
		if (voiceDir == null || voiceDir.length() == 0) voiceDir = "";
		else if (!voiceDir.endsWith("/")) voiceDir += "/";
		this.player = player;
		this.pitch = pitch;
		clips = new ResourceMap(getClass(), CLIP_DIR, EXT_DEF);
		clipKeys = ImmutableUtil.copyAsList(clips.keys());
		builds = new ResourceMap(cls, voiceDir + BUILD_DIR, EXT_DEF);
		jobs = new ResourceMap(cls, voiceDir + JOB_DIR, EXT_DEF);
		names = new ResourceMap(cls, voiceDir + NAME_DIR, EXT_DEF);
		phrases = new ResourceMap(cls, voiceDir + PHRASE_DIR, EXT_DEF);
		// Check all phrases exist
		for (AudioPhrase phrase : AudioPhrase.values())
			phrases.verify(phrase.name());
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
		int index = (int) (random.nextDouble() * clipKeys.size());
		String key = clipKeys.get(index);
		Resource resource = clips.resource(key);
		logger.debug("Alarm: {}", resource.name);
		checkRuntimeInterrupted();
		player.play(Audio.create(resource.data));
	}

	/**
	 * Plays "<build> <job> has just been broken [by <names>]"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.has_just_been_broken);
		Collection<Resource> nameResources = this.names.resources(names);
		if (nameResources.isEmpty()) return;
		play(AudioPhrase.by);
		play(nameResources);
	}

	/**
	 * Plays "<build> <job> is still broken. [<names> please fix it.]"
	 */
	public void playStillBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_still_broken);
		Collection<Resource> nameResources = this.names.resources(names);
		if (nameResources.isEmpty()) return;
		play(nameResources);
		play(AudioPhrase.please_fix_it);
	}

	/**
	 * Plays "<build> <job> is now fixed [thanks to <names> | thank you]."
	 */
	public void playJustFixed(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(AudioPhrase.is_now_fixed);
		Collection<Resource> nameResources = this.names.resources(names);
		if (!nameResources.isEmpty()) {
			play(AudioPhrase.thanks_to);
			play(nameResources);
		} else play(AudioPhrase.thank_you);
	}

	private void playBuildJob(String build, String job) throws IOException {
		if (playBuild(build)) playJob(job);
	}

	private boolean playBuild(String build) throws IOException {
		Resource resource = builds.resource(build);
		if (resource != null) play(resource);
		else play(AudioPhrase.the_build);
		return resource != null;
	}

	private boolean playJob(String job) throws IOException {
		Resource resource = jobs.resource(job);
		if (resource != null) play(resource);
		return resource != null;
	}

	private void play(Collection<Resource> resources) throws IOException {
		int i = resources.size();
		for (Resource resource : resources) {
			play(resource);
			if (--i == 1) play(AudioPhrase.and);
		}
	}

	private void play(AudioPhrase phrase) throws IOException {
		Resource resource = phrases.resource(phrase.name());
		play(resource);
	}

	private void play(Resource resource) throws IOException {
		logger.debug("Speech: {}", resource.name);
		checkRuntimeInterrupted();
		Audio audio = Audio.create(resource.data);
		player.play(audio.changePitch(pitch));
	}

	private void checkRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		if (interrupted) {
			interrupted = false;
			throw new RuntimeInterruptedException("Thread has been interrupted");
		}
	}

}
