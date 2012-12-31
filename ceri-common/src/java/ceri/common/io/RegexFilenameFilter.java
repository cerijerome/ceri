package ceri.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filename and File Filter that tries to match any one of the given regular expressions.
 * Can match against absolute or single names in system-specific or unix-style formats.
 */
public class RegexFilenameFilter implements FilenameFilter, FileFilter {
	private final List<Pattern> patterns;
	private final boolean absolutePath;
	private final boolean unixPath;

	public static class Builder {
		boolean absolutePath = false;
		boolean unixPath = false;
		List<Pattern> patterns = new ArrayList<>();
		
		Builder() {
		}
		
		/**
		 * Add regex patterns to check.
		 */
		public Builder pattern(String...patterns) {
			for (String pattern : patterns)
				this.patterns.add(Pattern.compile(pattern));
			return this;
		}
		
		/**
		 * Add regex patterns to check.
		 */
		public Builder pattern(Collection<String> patterns) {
			for (String pattern : patterns)
				this.patterns.add(Pattern.compile(pattern));
			return this;
		}
		
		/**
		 * Specify whether to check against absolute paths.
		 */
		public Builder absolutePath(boolean absolutePath) {
			this.absolutePath = absolutePath;
			return this;
		}
		
		/**
		 * Specify whether to check against unix-style '/' paths.
		 */
		public Builder unixPath(boolean unixPath) {
			this.unixPath = unixPath;
			return this;
		}
		
		/**
		 * Build the filter.
		 */
		public RegexFilenameFilter build() {
			return new RegexFilenameFilter(this);
		}
	}
	
	RegexFilenameFilter(Builder builder) {
		patterns = Collections.unmodifiableList(new ArrayList<>(builder.patterns));
		absolutePath = builder.absolutePath;
		unixPath = builder.unixPath;
	}

	/**
	 * Creates the builder to construct a regex filter. 
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Convenience method to create a filter based on given patterns.
	 */
	public static RegexFilenameFilter create(String...patterns) {
		return builder().pattern(patterns).build();
	}
	
	/**
	 * FilenameFilter interface implementation.
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (absolutePath) name = new File(dir, name).getAbsolutePath();
		return accept(name);
	}

	/**
	 * FileFilter interface implementation.
	 */
	@Override
	public boolean accept(File file) {
		String name = absolutePath ? file.getAbsolutePath() : file.getName();
		return accept(name);
	}

	private boolean accept(String path) {
		if (unixPath) path = IoUtil.convertPath(path);
		for (Pattern pattern : patterns)
			if (pattern.matcher(path).matches()) return true;
		return false;
	}
	
	public FileFilter asFile() {
		return this;
	}

	public FilenameFilter asFilename() {
		return this;
	}

}
