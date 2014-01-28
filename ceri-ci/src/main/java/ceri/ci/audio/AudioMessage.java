package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import ceri.common.io.IoUtil;

public class AudioMessage {
	private static final String AUDIO_FILE_SUFFIX = ".wav";
	private final File soundDir;
	private final float pitch;

	public AudioMessage(File soundDir) {
		this(soundDir, 1.0f);
	}
	
	public AudioMessage(File soundDir, float pitch) {
		this.soundDir = soundDir;
		this.pitch = pitch;
	}

	/**
	 * Plays alarm sound.
	 */
	public void playAlarm()	throws IOException {
		play(Clip.alarm);
	}
	
	/**
	 * Plays "<build> <job> has just been broken by <names>"
	 */
	public void playJustBroken(String build, String job, Collection<String> names)
		throws IOException {
		play(build);
		play(job);
		play(Clip.has_just_been_broken_by);
		for (String name : names)
			play(name);
	}

	/**
	 * Plays "<build> <job> is still broken. <names> please take a look."
	 */
	public void playStillBroken(String build, String job, Collection<String> names)
		throws IOException {
		play(build);
		play(job);
		play(Clip.is_still_broken);
		for (String name : names)
			play(name);
		play(Clip.please_take_a_look);
	}

	/**
	 * Plays "<build> <job> is now fixed thanks to <names>."
	 */
	public void playFixed(String build, String job, Collection<String> names) throws IOException {
		play(build);
		play(job);
		play(Clip.is_now_fixed_thanks_to);
		for (String name : names)
			play(name);
	}

	private void play(String key) throws IOException {
		IoUtil.checkIoInterrupted();
		File file = new File(soundDir, key + AUDIO_FILE_SUFFIX);
		if (!file.exists()) return;
		new Audio.Builder(file).pitch(pitch).build().play();
	}

	private void play(Clip clip) throws IOException {
		IoUtil.checkIoInterrupted();
		new Audio.Builder(clip.load()).pitch(pitch).build().play();
	}

}
