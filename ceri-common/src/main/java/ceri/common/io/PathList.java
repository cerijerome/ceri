package ceri.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import ceri.common.collect.Collectable;
import ceri.common.collect.Lists;
import ceri.common.function.Compares;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functional;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;

/**
 * Provides a path list under a directory with optional filters, mapping, and sorting.
 */
public class PathList {
	private static final Excepts.Operator<IOException, Path> IDENTITY = p -> p;
	private final Path dir;
	private final boolean all;
	private boolean sort = false;
	private Excepts.Operator<IOException, Path> modifier = IDENTITY; // filters and modifiers

	/**
	 * Returns an instance that accesses all sub-paths.
	 */
	public static PathList all(Path dir) {
		return new PathList(dir, true);
	}

	/**
	 * Returns an instance that accesses only child paths.
	 */
	public static PathList of(Path dir) {
		return new PathList(dir, false);
	}

	private PathList(Path dir, boolean all) {
		this.dir = dir;
		this.all = all;
	}

	/**
	 * Filter files only; may not work after calling relative().
	 */
	public PathList files() {
		return filter(Paths.Filter.file());
	}

	/**
	 * Filter path by file system pattern syntax.
	 */
	public PathList filter(String format, Object... args) {
		return filter(pattern(format, args));
	}

	/**
	 * Filter paths based on predicate.
	 */
	public PathList filter(Excepts.Predicate<IOException, ? super Path> filter) {
		return modify(p -> Filters.test(Filters.Nulls.no, filter, p) ? p : null);
	}

	/**
	 * Filter final path segment name by file system pattern syntax.
	 */
	public PathList nameFilter(String format, Object... args) {
		return filter(Filters.as(Path::getFileName, pattern(format, args)));
	}

	/**
	 * Filter final path segment name based on predicate.
	 */
	public PathList nameFilter(Excepts.Predicate<IOException, ? super String> filter) {
		return filter(Filters.as(Paths::name, filter));
	}

	/**
	 * Modifies paths relative to the dir. Additional filters will apply to the modified paths.
	 */
	public PathList relative() {
		int levels = Paths.nameCount(dir);
		return modify(p -> p.startsWith(dir) ? Paths.sub(p, levels) : p);
	}

	/**
	 * Sort the final list.
	 */
	public PathList sort() {
		sort = true;
		return this;
	}

	/**
	 * Returns the filtered paths as a mutable list.
	 */
	public List<Path> list() throws IOException {
		return list(IDENTITY);
	}

	/**
	 * Returns the filtered path strings as a mutable list.
	 */
	public List<String> strings() throws IOException {
		return list(Strings::safe);
	}

	/**
	 * Returns the filtered path final segment names as a mutable list.
	 */
	public List<String> names() throws IOException {
		return list(Paths::name);
	}

	/**
	 * Returns a stream of the filtered paths.
	 */
	public Stream<RuntimeException, Path> stream() throws IOException {
		return Streams.from(list());
	}

	// support

	private <T extends Comparable<? super T>> List<T>
		list(Excepts.Function<IOException, Path, ? extends T> mapper) throws IOException {
		var list = Lists.<T>of();
		consume(p -> Collectable.safeAdd(list, Functional.apply(mapper, apply(p))));
		if (sort) Lists.sort(list, Compares.of());
		return list;
	}

	@SuppressWarnings("resource")
	private Excepts.Predicate<IOException, Path> pattern(String format, Object... args) {
		return Paths.Filter.pattern(Paths.fs(dir), format, args);
	}
	
	private PathList modify(Excepts.Operator<IOException, Path> modifier) {
		this.modifier = combine(this.modifier, modifier);
		return this;
	}

	private void consume(Excepts.Consumer<IOException, Path> consumer) throws IOException {
		if (dir == null) return;
		if (all) consumeAll(consumer);
		else consumeChildren(consumer);
	}

	private void consumeAll(Excepts.Consumer<IOException, Path> consumer) throws IOException {
		var visitFn = FileVisitors.<Path, BasicFileAttributes>adaptConsumer(consumer);
		Files.walkFileTree(dir, FileVisitors.of(visitFn, null, visitFn));
	}

	private void consumeChildren(Excepts.Consumer<IOException, Path> consumer) throws IOException {
		try (var stream = Files.newDirectoryStream(dir)) {
			Stream.<IOException, Path>from(stream).forEach(consumer);
		}
	}

	private Path apply(Path path) throws IOException {
		if (Objects.equals(dir, path)) return null;
		return Functional.apply(modifier, path);
	}

	private static Excepts.Operator<IOException, Path> combine(
		Excepts.Operator<IOException, Path> mapper, Excepts.Operator<IOException, Path> next) {
		return p -> Functional.apply(next, Functional.apply(mapper, p));
	}
}
