package ceri.ci.audio;

import java.io.IOException;
import ceri.common.io.IoUtil;

public enum AudioClip {
	bieber,
	bodyguard,
	boss,
	bubblepop,
	callmemaybe,
	dontstopbelievin,
	eas,
	ebay,
	finalcountdown,
	fox,
	friday,
	gangnam,
	gangnam2,
	ironside,
	ironside2,
	liveletdie,
	lmfao,
	numa,
	nyan,
	redalert,
	rickroll,
	sirmixalot,
	stayingalive,
	titanic,
	trololo,
	wannabe,
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
