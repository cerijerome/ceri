package ceri.ci.audio;

import java.io.File;
import java.io.IOException;

public class AudioPlayer {
	private static final String SOUND_FILE_SUFFIX = ".wav";
	private final File keyDir;
	private final File clipDir;

	public static enum Clip {
		alarm,
		build_broken,
		build_ok,
		thank_you,
		out_of_time,
		you_have_10min,
		you_have_20min,
		you_have_30min;
	}

	AudioPlayer(File keyDir, File clipDir) {
		this.keyDir = keyDir;
		this.clipDir = clipDir;
	}
	
	public void play(String name) {
		File file = new File(keyDir, name + SOUND_FILE_SUFFIX);
		if (file.exists()) play(file);
	}
	
	public void play(Clip clip) {
		play(new File(clipDir, clip.name().replaceAll("_", "-") + SOUND_FILE_SUFFIX));
	}
	
	private void play(File file) {
		try {
			Audio.create(file).play();
		} catch (IOException e) {
			// log error
			e.printStackTrace();
		}
	}
	
}
