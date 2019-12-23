package ceri.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Predicate;
import ceri.common.filter.Filters;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.FunctionUtil;

public class PathFilters {
	/**
	 * A filter that accepts no paths.
	 */
	public static final Predicate<Path> NONE = path -> false;

	/**
	 * A filter that accepts all paths.
	 */
	public static final Predicate<Path> ALL = path -> true;

	/**
	 * A filter that only accepts directories.
	 */
	public static final Predicate<Path> DIR = Files::isDirectory;

	/**
	 * A filter that only accepts files.
	 */
	public static final Predicate<Path> FILE = Files::isRegularFile;

	private PathFilters() {}

	/**
	 * Adapts a non-exception predicate, making it easier to combine with other predicates.
	 */
	public static ExceptionPredicate<IOException, Path> adapt(Predicate<Path> filter) {
		return filter::test;
	}

	/**
	 * A filter that applies the text match to a unix path.
	 */
	public static Predicate<Path> byUnixPath(Predicate<String> filter) {
		return FunctionUtil.testing(IoUtil::pathToUnix, filter);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static ExceptionPredicate<IOException, Path> byFileNamePath(Predicate<Path> filter) {
		return ExceptionPredicate.testing(Path::getFileName, filter::test);
	}

	/**
	 * A filter that applies the path filter to the filename path only.
	 */
	public static ExceptionPredicate<IOException, Path> byFileName(Predicate<String> filter) {
		return ExceptionPredicate.testing(IoUtil::fileName, filter::test);
	}

	/**
	 * A filter that applies the string filter to partial path at given index. Returns false if
	 * index
	 */
	public static Predicate<Path> byIndex(int index, Predicate<String> test) {
		return path -> {
			String name = IoUtil.name(path, index);
			return name == null ? false : test.test(name);
		};
	}

	/**
	 * A filter that applies the text match to filename extension of files only.
	 */
	public static Predicate<Path> byExtension(Predicate<String> filter) {
		return FILE.and(FunctionUtil.testing(IoUtil::extension, filter));
	}

	/**
	 * A filter that applies the text match to the filename extension.
	 */
	public static Predicate<Path> byExtension(String... extensions) {
		return byExtension(Filters.eqAny(extensions));
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static ExceptionPredicate<IOException, Path> byLastModified(Predicate<Instant> filter) {
		return ExceptionPredicate.testing(path -> Files.getLastModifiedTime(path).toInstant(),
			filter::test);
	}

	/**
	 * A filter that applies the time instant match to the path last modified time.
	 */
	public static ExceptionPredicate<IOException, Path> byModifiedSince(long epochMs) {
		return byLastModified(instant -> instant.toEpochMilli() >= epochMs);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static ExceptionPredicate<IOException, Path> bySize(Predicate<Long> filter) {
		return ExceptionPredicate.testing(path -> Files.size(path), filter::test);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static ExceptionPredicate<IOException, Path> byMaxSize(long maxSize) {
		return bySize(size -> size <= maxSize);
	}

	/**
	 * A filter that applies the numeric match to the file size.
	 */
	public static ExceptionPredicate<IOException, Path> byMinSize(long minSize) {
		return bySize(size -> size >= minSize);
	}

}
