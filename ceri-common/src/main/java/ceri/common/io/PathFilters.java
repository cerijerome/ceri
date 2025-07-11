package ceri.common.io;

import static ceri.common.function.Predicates.and;
import static ceri.common.function.Predicates.testing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import ceri.common.function.Excepts.Predicate;
import ceri.common.function.Predicates;

/**
 * Predicates for filtering paths.
 */
public class PathFilters {
	/** A filter that accepts no paths. */
	public static final Predicate<IOException, Path> NONE = _ -> false;
	/** A filter that accepts all paths. */
	public static final Predicate<IOException, Path> ALL = _ -> true;
	/** A filter that only accepts directories. */
	public static final Predicate<IOException, Path> DIR = Files::isDirectory;
	/** A filter that only accepts files. */
	public static final Predicate<IOException, Path> FILE = Files::isRegularFile;

	private PathFilters() {}

	/**
	 * A filter that applies the text match to a unix path.
	 */
	public static Predicate<IOException, Path> byUnixPath(Predicate<IOException, String> filter) {
		return testing(IoUtil::pathToUnix, filter);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static Predicate<IOException, Path> byFileNamePath(Predicate<IOException, Path> filter) {
		return testing(Path::getFileName, filter::test);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static Predicate<IOException, Path> byFileName(Predicate<IOException, String> filter) {
		return testing(IoUtil::filename, filter::test);
	}

	/**
	 * A filter that applies the string filter to partial path at given index. Returns false if
	 * index
	 */
	public static Predicate<IOException, Path> byIndex(int index,
		Predicate<IOException, String> test) {
		return path -> {
			String name = IoUtil.name(path, index);
			return name == null ? false : test.test(name);
		};
	}

	/**
	 * A filter that applies the text match to filename extension of files only.
	 */
	public static Predicate<IOException, Path> byExtension(Predicate<IOException, String> filter) {
		Predicate<IOException, Path> ext = testing(IoUtil::extension, filter);
		return and(FILE, ext);
	}

	/**
	 * A filter that applies the text match to the filename extension.
	 */
	public static Predicate<IOException, Path> byExtension(String... extensions) {
		return byExtension(Predicates.ex(Predicates.eqAny(extensions)));
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static Predicate<IOException, Path>
		byLastModified(Predicate<IOException, Instant> filter) {
		return testing(path -> Files.getLastModifiedTime(path).toInstant(), filter::test);
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static Predicate<IOException, Path> byModifiedSince(long epochMs) {
		return byLastModified(instant -> instant.toEpochMilli() >= epochMs);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Predicate<IOException, Path> bySize(Predicate<IOException, Long> filter) {
		return testing(path -> Files.size(path), filter::test);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Predicate<IOException, Path> byMaxSize(long maxSize) {
		return bySize(size -> size <= maxSize);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static Predicate<IOException, Path> byMinSize(long minSize) {
		return bySize(size -> size >= minSize);
	}
}
