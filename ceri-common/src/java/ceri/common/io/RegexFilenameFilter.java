package ceri.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Filename and File Filter that matches multiple given regular expressions.
 */
public class RegexFilenameFilter implements FilenameFilter, FileFilter {

	private final Pattern[] patterns;
	private boolean useAbsolutePath = false;

	/**
	 * Creates the filter with given regex patterns. useAbsolutePath specifies
	 * the patterns match against the full path names.
	 */
	public RegexFilenameFilter(boolean useAbsolutePath, String... strPatterns) {
		patterns = new Pattern[strPatterns.length];
		for (int i = 0; i < strPatterns.length; i++) {
			patterns[i] = Pattern.compile(strPatterns[i]);
		}
		this.useAbsolutePath = useAbsolutePath;
	}

	/**
	 * FilenameFilter interface implementation.
	 */
	@Override
	public boolean accept(File dir, String name) {
		name = getName(dir, name);
		for (Pattern pattern : patterns) {
			if (pattern.matcher(name).matches()) return true;
		}
		return false;
	}

	/**
	 * FileFilter interface implementation.
	 */
	@Override
	public boolean accept(File pathname) {
		String name = getName(pathname);
		for (Pattern pattern : patterns) {
			if (pattern.matcher(name).matches()) return true;
		}
		return false;
	}

	public FileFilter asFile() {
		return this;
	}

	public FilenameFilter asFilename() {
		return this;
	}

	private String getName(File file) {
		if (useAbsolutePath) return file.getAbsolutePath();
		return file.getName();
	}

	private String getName(File dir, String name) {
		if (useAbsolutePath) {
			File file = new File(dir, name);
			return file.getAbsolutePath();
		}
		return name;
	}

}
