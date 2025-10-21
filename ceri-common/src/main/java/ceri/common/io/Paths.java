package ceri.common.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.data.ByteProvider;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functional;
import ceri.common.function.Functions;
import ceri.common.stream.Stream;
import ceri.common.text.Strings;
import ceri.common.util.Basics;

/**
 * Support for file system paths.
 */
public class Paths {
	private static final LinkOption[] NO_LINK_OPTIONS = new LinkOption[0];
	private static final int MIN_ABSOLUTE_DIRS = 3;
	private static final FileVisitor<Path> DELETING_VISITOR =
		FileVisitUtil.visitor(null, FileVisitUtil.deletion(), FileVisitUtil.deletion());
	private static final Excepts.Predicate<IOException, Path> NULL_FILTER = _ -> true;
	public static final ByteProvider EOL_BYTES = ByteProvider.of(Strings.EOL.getBytes());

	private Paths() {}

	/**
	 * Predicates for filtering paths.
	 */
	public static class Filter {
		private static final Functions.Predicate<Path> FILE = Files::isRegularFile;
		private static final Functions.Predicate<Path> DIR = Files::isDirectory;

		private Filter() {}

		/**
		 * A filter that only accepts files.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Path> file() {
			return Filters.ex(FILE);
		}

		/**
		 * A filter that only accepts directories.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Path> dir() {
			return Filters.ex(DIR);
		}

		/**
		 * A filter that applies the filter to the filename as a path.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Path>
			namePath(Excepts.Predicate<? extends E, Path> filter) {
			return Filters.as(Path::getFileName, filter);
		}

		/**
		 * A filter that applies the filter to the filename.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Path>
			name(Excepts.Predicate<? extends E, String> filter) {
			return Filters.as(Paths::name, filter);
		}

		/**
		 * A filter that applies the filter to the filename extension.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Path>
			ext(Excepts.Predicate<? extends E, String> filter) {
			return Filters.as(Paths::ext, filter);
		}

		/**
		 * A filter that applies the filter to the file last modified time.
		 */
		public static Excepts.Predicate<IOException, Path>
			lastModified(Excepts.Predicate<? extends IOException, ? super Instant> filter) {
			return Filters.as(Paths::lastModified, filter);
		}

		/**
		 * A filter that applies the filter to the file size.
		 */
		public static Excepts.Predicate<IOException, Path>
			size(Excepts.Predicate<? extends IOException, ? super Long> filter) {
			return Filters.as(Files::size, filter);
		}
	}

	/**
	 * Return the first root path of the given file system.
	 */
	public static Path root(FileSystem fs) {
		if (fs == null) return null;
		Iterator<Path> i = fs.getRootDirectories().iterator();
		return i.hasNext() ? i.next() : null;
	}

	/**
	 * Checks if a path is absolute root.
	 */
	public static boolean isRoot(Path path) {
		return path != null && path.isAbsolute() && path.equals(path.getRoot());
	}

	/**
	 * Returns the last segment of the path as a string, or blank if empty. Returns null for a null
	 * path.
	 */
	public static String name(Path path) {
		if (path == null) return null;
		return Strings.safe(path.getFileName());
	}

	/**
	 * Retrieves the path section name at given index if it exists, otherwise null. A non-negative
	 * index starts with 0 at root. A negative index start at the furthest filename with index -1.
	 */
	public static String name(Path path, int index) {
		if (path == null) return null;
		int n = path.getNameCount();
		if (index < -n || index >= n) return null;
		if (index < 0) index += n;
		return Strings.safe(path.getName(index));
	}

	/**
	 * Returns the file name without extension, full name if none, or null for null path. Does not
	 * check if path is a file or if it exists. Returns full name for file names starting with a
	 * dot, and no extension.
	 */
	public static String nameWithoutExt(Path path) {
		return nameWithoutExt(name(path));
	}

	/**
	 * Returns the file name without extension, full name if none, or null for null name. Does not
	 * check if path is a file or if it exists. Returns full name for file names starting with a
	 * dot, and no extension.
	 */
	public static String nameWithoutExt(String filename) {
		if (filename == null) return null;
		int i = filename.lastIndexOf('.');
		return i <= 0 ? filename : filename.substring(0, i);
	}

	/**
	 * Returns the file extension, empty string if none, or null for null path. Does not check if
	 * path is a file or if it exists. Returns empty string for file names starting with a dot, and
	 * no extension.
	 */
	public static String ext(Path path) {
		return ext(name(path));
	}

	/**
	 * Returns the file extension, empty string if none, or null for null path. Does not check if
	 * path is a file or if it exists. Returns empty string for file names starting with a dot, and
	 * no extension.
	 */
	public static String ext(String filename) {
		if (filename == null) return null;
		int i = filename.lastIndexOf('.');
		return i <= 0 ? "" : filename.substring(i + 1);
	}

	/**
	 * Returns the last modified time of a file.
	 */
	public static Instant lastModified(Path path, LinkOption... options) throws IOException {
		if (path == null) return null;
		options = Basics.def(options, NO_LINK_OPTIONS);
		return Files.getLastModifiedTime(path, options).toInstant();
	}

	/**
	 * Retrieves the path from given index.
	 */
	public static Path sub(Path path, int index) {
		if (path == null) return null;
		return sub(path, index, path.getNameCount());
	}

	/**
	 * Retrieves the path from given index.
	 */
	public static Path sub(Path path, int start, int end) {
		if (path == null) return null;
		int count = path.getNameCount();
		if (count == 1 && path.getName(0).toString().isEmpty()) count = 0;
		if (start == end && start <= count) return newPath(path, "");
		return path.subpath(start, end);
	}

	/**
	 * Extends a path. Returns null if path is null.
	 */
	public static Path extend(Path path, String... paths) {
		if (path == null || ArrayUtil.isEmpty(paths)) return path;
		return newPath(path, path.toString(), paths);
	}

	/**
	 * Creates a new path using the FileSystem of the given path.
	 */
	@SuppressWarnings("resource")
	public static Path newPath(Path ref, String first, String... more) {
		if (ref == null) return null;
		return ref.getFileSystem().getPath(first, more);
	}

	/**
	 * Replaces the last part of the path.
	 */
	public static Path changeName(Path path, String filename) {
		return changeName(path, _ -> filename);
	}

	/**
	 * Replaces the last part of the path.
	 */
	public static <E extends Exception> Path changeName(Path path,
		Excepts.Operator<E, String> filenameFn) throws E {
		if (path == null) return null;
		var file = path.getFileName();
		var filename = filenameFn.apply(file == null ? "" : file.toString());
		if (filename == null) return path;
		var parent = path.getParent();
		if (parent != null) return parent.resolve(filename);
		if (path.isAbsolute()) return path.resolve(filename);
		return newPath(path, filename);
	}

	/**
	 * Shortens the end of a path by given number of levels.
	 */
	public static Path shorten(Path path, int levels) {
		if (path == null || levels == 0) return path;
		int nameCount = path.getNameCount();
		boolean absolute = path.isAbsolute();
		if (nameCount == levels) return absolute ? path.getRoot() : newPath(path, "");
		if (!path.isAbsolute()) return sub(path, 0, nameCount - levels);
		return path.getRoot().resolve(sub(path, 0, nameCount - levels));
	}

	/**
	 * Create a temp dir with random name under given dir. Use null for current dir.
	 */
	public static Path createTempDir(Path rootDir) throws IOException {
		if (rootDir == null) rootDir = Path.of("");
		return Files.createTempDirectory(rootDir, null);
	}

	/**
	 * Delete all files under the directory, and the directory itself. Be careful! Returns true if
	 * the directory existed and was deleted.
	 */
	public static boolean deleteAll(Path path) throws IOException {
		if (path == null) return false;
		if (path.getRoot() != null && path.getNameCount() <= MIN_ABSOLUTE_DIRS)
			throw new IOException("Unsafe delete not permitted: " + path);
		if (!Files.isDirectory(path)) return false;
		Files.walkFileTree(path, DELETING_VISITOR);
		return true;
	}

	/**
	 * Deletes all empty directories under this directory.
	 */
	public static void deleteEmptyDirs(Path dir) throws IOException {
		if (dir == null) return;
		var visitor = FileVisitUtil.<Path>visitor(null, (path, _) -> {
			if (isEmptyDir(path)) Files.delete(path);
			return FileVisitUtil.result(true);
		}, null);
		Files.walkFileTree(dir, visitor);
	}

	/**
	 * Checks if a directory is empty.
	 */
	public static boolean isEmptyDir(Path dir) throws IOException {
		if (dir == null || !Files.isDirectory(dir)) return false;
		try (var stream = Files.newDirectoryStream(dir)) {
			return !stream.iterator().hasNext();
		}
	}

	/**
	 * Convert file path to unix format.
	 */
	public static String toUnix(Path path) {
		return path == null ? null : toUnix(path.toString());
	}

	/**
	 * Convert file path to unix format.
	 */
	public static String toUnix(String path) {
		return convert(path, File.separatorChar, '/');
	}

	/**
	 * Convert unix format path to file path.
	 */
	public static String fromUnix(String unix) {
		return convert(unix, '/', File.separatorChar);
	}

	/**
	 * Convert path containing separators to use another separator.
	 */
	public static String convert(String path, char currentSeparator, char newSeparator) {
		if (path == null) return null;
		return path.replace(currentSeparator, newSeparator); // does nothing for same separators
	}

	/**
	 * Lists relative paths recursively under a given directory.
	 */
	public static List<Path> pathsRelative(Path dir) throws IOException {
		return pathsRelative(dir, NULL_FILTER);
	}

	/**
	 * Lists relative filtered paths recursively under a given directory. A null filter matches all
	 * paths.
	 */
	public static List<Path> pathsRelative(Path dir, Excepts.Predicate<IOException, Path> filter)
		throws IOException {
		if (dir == null) return Immutable.list();
		var test = Basics.def(filter, NULL_FILTER);
		int levels = dir.getNameCount();
		return pathsCollect(dir, path -> test.test(path) ? sub(path, levels) : null);
	}

	/**
	 * Lists relative filtered paths recursively under a given directory. A null pattern matches all
	 * paths. The pattern is applied against the full path.
	 */
	public static List<Path> pathsRelative(Path dir, String syntaxPattern) throws IOException {
		return pathsRelative(dir,
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test);
	}

	/**
	 * Lists paths recursively under a given directory.
	 */
	public static List<Path> paths(Path dir) throws IOException {
		return paths(dir, NULL_FILTER);
	}

	/**
	 * Lists filtered paths recursively under a given directory. A null filter matches all paths.
	 */
	public static List<Path> paths(Path dir, Excepts.Predicate<IOException, Path> filter)
		throws IOException {
		var test = Basics.def(filter, NULL_FILTER);
		return pathsCollect(dir, path -> test.test(path) ? path : null);
	}

	/**
	 * Lists filtered paths recursively under a given directory. A null pattern matches all paths.
	 * The pattern is applied against the full path.
	 */
	public static List<Path> paths(Path dir, String syntaxPattern) throws IOException {
		Excepts.Predicate<IOException, Path> filter =
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test;
		return paths(dir, filter);
	}

	/**
	 * Recursively collects mapped paths under a given directory. The mapping function excludes
	 * entries by returning null.
	 */
	public static <T> List<T> pathsCollect(Path dir, Excepts.Function<IOException, Path, T> mapper)
		throws IOException {
		if (dir == null || mapper == null) return Immutable.list();
		var list = Lists.<T>of();
		var visitFn = FileVisitUtil.<Path, BasicFileAttributes>adaptConsumer(
			path -> Functional.accept(list::add, mapper.apply(path)));
		Files.walkFileTree(dir, FileVisitUtil.visitor(visitFn, null, visitFn));
		return list;
	}

	/**
	 * Lists path names under a directory (no recursion).
	 */
	public static List<String> listNames(Path dir) throws IOException {
		return listNames(dir, NULL_FILTER);
	}

	/**
	 * Lists filtered path names under a directory (no recursion).
	 */
	public static List<String> listNames(Path dir,
		Excepts.Predicate<? extends IOException, ? super Path> filter) throws IOException {
		if (dir == null) return Immutable.list();
		try (var stream = Files.newDirectoryStream(dir)) {
			return stream(stream).filter(filter).map(Paths::name).toList();
		}
	}

	/**
	 * Lists filtered path names under a directory (no recursion). A null pattern matches all paths.
	 * The pattern is applied against the file name path, not the full path.
	 */
	public static List<String> listNames(Path dir, String syntaxPattern) throws IOException {
		Excepts.Predicate<IOException, Path> filter = syntaxPattern == null ? null :
			Filter.namePath(PathPattern.of(syntaxPattern).matcher(dir)::test);
		return listNames(dir, filter);
	}

	/**
	 * Lists paths under a directory (no recursion).
	 */
	public static List<Path> list(Path dir) throws IOException {
		return list(dir, NULL_FILTER);
	}

	/**
	 * Lists filtered paths under a directory (no recursion). A null filter matches all paths.
	 */
	public static List<Path> list(Path dir, Excepts.Predicate<IOException, ? super Path> filter)
		throws IOException {
		if (dir == null) return Immutable.list();
		try (var stream = Files.newDirectoryStream(dir)) {
			return stream(stream).filter(filter).toList();
		}
	}

	/**
	 * Lists filtered paths under a directory (no recursion). A null pattern matches all paths. The
	 * pattern is applied against the file name path, not the full path.
	 */
	public static List<Path> list(Path dir, String syntaxPattern) throws IOException {
		return list(dir, syntaxPattern == null ? null :
			Filter.namePath(PathPattern.of(syntaxPattern).matcher(dir)::test));
	}

	/**
	 * Collects mapped paths from a directory. The mapping function excludes entries by returning
	 * null.
	 */
	public static <T> List<T> listCollect(Path dir,
		Excepts.Function<? extends IOException, ? super Path, T> mapper) throws IOException {
		if (dir == null || mapper == null) return Immutable.list();
		try (var stream = Files.newDirectoryStream(dir)) {
			return stream(stream).map(mapper).toList();
		}
	}

	/**
	 * Copies file contents from one file to another, creating the destination directories if
	 * necessary. Returns the destination path. Removes created directories on failure.
	 */
	public static Path copyFile(Path src, Path dest) throws IOException {
		if (src == null || dest == null) return dest;
		var tracker = new FileTracker();
		try {
			tracker.file(dest); // creates parent dirs
			return Files.copy(src, dest);
		} catch (RuntimeException | IOException e) {
			tracker.delete();
			throw e;
		}
	}

	/**
	 * Writes bytes from input stream to a file, creating the destination directories if necessary.
	 * Returns the number of bytes written. Removes created directories on failure.
	 */
	public static long copy(InputStream in, Path file) throws IOException {
		if (in == null || file == null) return 0;
		var tracker = new FileTracker();
		try {
			tracker.file(file); // creates parent dirs
			return Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (RuntimeException | IOException e) {
			tracker.delete();
			throw e;
		}
	}

	/**
	 * Writes byte array content to a file, creating the destination directories if necessary.
	 * Returns the number of bytes written. Removes created directories on failure.
	 */
	public static int write(Path file, ByteProvider data) throws IOException {
		if (file == null || data == null) return 0;
		var tracker = new FileTracker();
		try {
			tracker.file(file); // creates parent dirs
			try (var out = Files.newOutputStream(file)) {
				data.writeTo(0, out);
				out.flush();
			}
			return data.length();
		} catch (RuntimeException | IOException e) {
			tracker.delete();
			throw e;
		}
	}

	// support

	private static Stream<IOException, Path> stream(DirectoryStream<Path> stream) {
		return Stream.from(stream);
	}
}
