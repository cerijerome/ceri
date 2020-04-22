package ceri.common.io;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Predicate;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * A path filter for glob and regex patterns, using a FileSystem matcher.
 */
public class PathPattern {
	private static final String GLOB_SYNTAX = "glob";
	private static final String REGEX_SYNTAX = "regex";
	private final String pattern;

	/**
	 * Constructor for syntax:pattern, to be parsed by FileSystem.
	 */
	public static PathPattern of(String syntaxPattern) {
		return new PathPattern(syntaxPattern);
	}

	/**
	 * Constructor for glob:pattern, to be parsed by FileSystem.
	 */
	public static PathPattern glob(String pattern) {
		return of(GLOB_SYNTAX + ":" + pattern);
	}

	/**
	 * Constructor for regex:pattern, to be parsed by FileSystem.
	 */
	public static PathPattern regex(String pattern) {
		return of(REGEX_SYNTAX + ":" + pattern);
	}

	private PathPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Matcher using the default file system.
	 */
	@SuppressWarnings("resource")
	public Predicate<Path> matcher() {
		return matcher(FileSystems.getDefault());
	}

	/**
	 * Matcher for the given path file system.
	 */
	@SuppressWarnings("resource")
	public Predicate<Path> matcher(Path path) {
		return matcher(path.getFileSystem());
	}

	/**
	 * Matcher for the given file system.
	 */
	public Predicate<Path> matcher(FileSystem fs) {
		PathMatcher matcher = fs.getPathMatcher(pattern);
		return matcher::matches;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(pattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PathPattern)) return false;
		PathPattern other = (PathPattern) obj;
		if (!EqualsUtil.equals(pattern, other.pattern)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, pattern).toString();
	}

}
