package ceri.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Predicates for filtering paths.
 */
public class PathFilters {
	/** A filter that only accepts files. */
	public static final Functions.Predicate<Path> FILE = Files::isRegularFile;
	/** A filter that only accepts directories. */
	public static final Functions.Predicate<Path> DIR = Files::isDirectory;

	private PathFilters() {}

	/**
	 * A filter that only accepts files.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path> file() {
		return Reflect.unchecked(FILE);
	}

	/**
	 * A filter that only accepts directories.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path> dir() {
		return Reflect.unchecked(DIR);
	}

	/**
	 * A filter that applies the text match to a unix path.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path>
		byUnixPath(Excepts.Predicate<? extends E, ? super String> filter) {
		return Filters.as(IoUtil::pathToUnix, filter);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path>
		byFileNamePath(Excepts.Predicate<? extends E, Path> filter) {
		return Filters.as(Path::getFileName, filter::test);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path>
		byFileName(Excepts.Predicate<? extends E, String> filter) {
		return Filters.as(IoUtil::filename, filter::test);
	}

	/**
	 * A filter that applies the string filter to partial path at given index. Returns false if
	 * index
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path> byIndex(int index,
		Excepts.Predicate<? extends E, String> test) {
		return path -> {
			String name = IoUtil.name(path, index);
			return name == null ? false : test.test(name);
		};
	}

	/**
	 * A filter that applies the text match to filename extension of files only.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path>
		byExtension(Excepts.Predicate<? extends E, String> filter) {
		return Filters.andOf(file(), Filters.as(IoUtil::extension, filter));
	}

	/**
	 * A filter that applies the text match to the filename extension.
	 */
	public static <E extends Exception> Excepts.Predicate<E, Path>
		byExtension(String... extensions) {
		return byExtension(Filters.ex(Filters.equalAnyOf(extensions)));
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static Excepts.Predicate<IOException, Path>
		byLastModified(Excepts.Predicate<? extends IOException, ? super Instant> filter) {
		return Filters.as(IoUtil::lastModified, filter::test);
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static Excepts.Predicate<IOException, Path> byModifiedSince(long epochMs) {
		return byLastModified(instant -> instant.toEpochMilli() >= epochMs);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Excepts.Predicate<IOException, Path>
		bySize(Excepts.Predicate<? extends IOException, ? super Long> filter) {
		return Filters.as(Files::size, filter);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Excepts.Predicate<IOException, Path> byMaxSize(long maxSize) {
		return bySize(size -> size <= maxSize);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Excepts.Predicate<IOException, Path> byMinSize(long minSize) {
		return bySize(size -> size >= minSize);
	}
}
