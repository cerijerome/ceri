package ceri.ci.audio;

import java.io.IOException;
import ceri.common.io.IoUtil;

public enum AudioClip {
	bieber,
	boss,
	bubblepop,
	eas,
	ebay,
	fox,
	friday,
	gangnam,
	gangnam2,
	numa,
	nyan,
	redalert,
	rickroll,
	trololo,
	whisper;

	private static final String SUBDIR = "clip";
	private static final String FILE_SUFFIX = ".wav";
	public final String filename;

	private AudioClip() {
		filename = name() + FILE_SUFFIX;
	}

	public Audio load() throws IOException {
		return Audio.create(IoUtil.getResource(AudioClip.class, SUBDIR + "/" + filename));
	}

	public static void main(String[] args) throws IOException {
		//friday.load().play();
		//gangnam.load().play();
		//gangnam2.load().play();
		for (AudioClip clip : AudioClip.values()) clip.load().play();
	}
}
