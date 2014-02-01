package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import ceri.common.concurrent.ConcurrentUtil;

public class AudioMessage {
	private static final String AUDIO_FILE_SUFFIX = ".wav";
	private final File soundDir;
	private final float pitch;
	private final AudioPlayer player;

	public AudioMessage(AudioPlayer player, File soundDir) {
		this(player, soundDir, Audio.NORMAL_PITCH);
	}
	
	public AudioMessage(AudioPlayer player, File soundDir, float pitch) {
		this.player = player;
		this.soundDir = soundDir;
		this.pitch = pitch;
	}

	/**
	 * Plays alarm sound.
	 */
	public void playAlarm() throws IOException {
		play(Clip.alarm);
	}

	/**
	 * Plays "<build> <job> has just been broken by <names>"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(Clip.has_just_been_broken);
		if (audioExists(names)) {
			play(Clip.by);
			playNames(names);
		}
	}

	/**
	 * Plays "<build> <job> is still broken. <names> please take a look."
	 */
	public void playStillBroken(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(Clip.is_still_broken);
		playNames(names);
		play(Clip.please_fix_it);
	}

	/**
	 * Plays "<build> <job> is now fixed thanks to <names>."
	 */
	public void playJustFixed(String build, String job, Collection<String> names)
		throws IOException {
		playBuildJob(build, job);
		play(Clip.is_now_fixed);
		if (audioExists(names)) {
			play(Clip.thanks_to);
			playNames(names);
		} else play(Clip.thank_you);
	}

	private void playBuildJob(String build, String job) throws IOException {
		if (!audioExists(build)) {
			play(Clip.the_build);
			return;
		}
		play(build);
		if (!audioExists(job)) play(Clip.job);
		else play(job);
	}

	private void playNames(Collection<String> names) throws IOException {
		int i = names.size();
		for (String name : names) {
			play(name);
			if (--i == 1) play(Clip.and);
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
		ConcurrentUtil.checkRuntimeInterrupted();
		File file = audioFile(key);
		if (!file.exists()) return;
		play(Audio.create(file));
	}

	private void play(Clip clip) throws IOException {
		ConcurrentUtil.checkRuntimeInterrupted();
		play(Audio.create(clip.load()));
	}

	private void play(Audio audio) throws IOException {
		player.play(audio.changePitch(pitch));
	}
	
}
