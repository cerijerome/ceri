package ceri.ci.audio;


public enum AudioPhrase {
	and,
	build,
	by,
	eas,
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
	
	private AudioPhrase() {
		filename = "_" + name().replaceAll("_", "-") + FILE_SUFFIX;
	}
	
}