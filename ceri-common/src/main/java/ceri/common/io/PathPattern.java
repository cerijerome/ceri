package ceri.common.io;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import ceri.common.function.Functions;

/**
 * A path filter for glob and regex patterns, using a FileSystem matcher.
 */
public record PathPattern(String pattern) {
	private static final String GLOB_SYNTAX = "glob";
	private static final String REGEX_SYNTAX = "regex";

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

	/**
	 * Matcher using the default file system.
	 */
	@SuppressWarnings("resource")
	public Functions.Predicate<Path> matcher() {
		return matcher(FileSystems.getDefault());
	}

	/**
	 * Matcher for the given path file system.
	 */
	@SuppressWarnings("resource")
	public Functions.Predicate<Path> matcher(Path path) {
		return matcher(path.getFileSystem());
	}

	/**
	 * Matcher for the given file system.
	 */
	public Functions.Predicate<Path> matcher(FileSystem fs) {
		PathMatcher matcher = fs.getPathMatcher(pattern);
		return matcher::matches;
	}
}
