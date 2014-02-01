package ceri.ci.audio;

import java.io.IOException;
import ceri.common.io.IoUtil;

public enum Clip {
	alarm,
	and,
	build,
	by,
	has_just_been_broken,
	is_now_fixed,
	is_still_broken,
	job,
	please_fix_it,
	red_alert,
	thank_you,
	thanks_to,
	the_build,
	;
	
	private static final String FILE_SUFFIX = ".wav";
	public final String filename;
	
	private Clip() {
		filename = "_" + name().replaceAll("_", "-") + FILE_SUFFIX;
	}
	
	public Audio audio() throws IOException {
		return Audio.create(IoUtil.getResource(getClass(), filename));
	}
	
}