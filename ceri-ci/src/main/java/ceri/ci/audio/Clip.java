package ceri.ci.audio;

import java.io.IOException;
import ceri.common.io.IoUtil;

public enum Clip {
	has_just_been_broken_by,
	is_still_broken,
	please_take_a_look,
	is_now_fixed_thanks_to,
	//
	build_broken,
	build_ok,
	thank_you,
	out_of_time,
	you_have_10min,
	you_have_20min,
	you_have_30min,
	alarm;
	
	private static final String FILE_SUFFIX = ".wav";
	public final String filename;
	
	private Clip() {
		filename = "_" + name().replaceAll("_", "-") + FILE_SUFFIX;
	}
	
	public byte[] load() throws IOException {
		return IoUtil.getResource(getClass(), filename);
	}
	
	public void play() throws IOException {
		Audio.create(load()).play();
	}
	
}