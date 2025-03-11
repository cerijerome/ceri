package ceri.common.io;

import static ceri.common.function.FunctionUtil.safeAccept;
import static ceri.common.text.StringUtil.EOL;
import static ceri.common.util.BasicUtil.defaultValue;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.collection.StreamUtil;
import ceri.common.collection.WrappedStream;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionObjIntPredicate;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.FunctionUtil;
import ceri.common.function.FunctionWrapper;
import ceri.common.text.StringUtil;
import ceri.common.util.SystemVars;

/**
 * I/O utility functions.
 */
public class IoUtil {
	private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
	private static final String USER_HOME_PROPERTY = "user.home";
	private static final String USER_DIR_PROPERTY = "user.dir";
	private static final String CLASS_SUFFIX = ".class";
	private static final int READ_POLL_MS = 20;
	private static final int SKIP_BUFFER_SIZE = 64;
	private static final int BUFFER_SIZE_DEF = 1024;
	private static final int MIN_ABSOLUTE_DIRS = 3;
	private static final Pattern PATH_SEPARATOR_REGEX =
		Pattern.compile("\\Q" + File.pathSeparator + "\\E");
	private static final FileVisitor<Path> DELETING_VISITOR =
		FileVisitUtil.visitor(null, FileVisitUtil.deletion(), FileVisitUtil.deletion());
	public static final ExceptionAdapter<IOException> IO_ADAPTER =
		ExceptionAdapter.of(IOException.class, IOException::new);
	public static final ExceptionAdapter<RuntimeIoException> RUNTIME_IO_ADAPTER =
		ExceptionAdapter.of(RuntimeIoException.class, RuntimeIoException::new);
	private static final FunctionWrapper<IOException> WRAPPER = FunctionWrapper.of();
	private static final ExceptionPredicate<IOException, Path> NULL_FILTER = _ -> true;
	public static final ByteProvider EOL_BYTES = ByteProvider.of(EOL.getBytes());

	private IoUtil() {}

	/**
	 * System line separator as encoded bytes.
	 */
	public static ByteProvider eolBytes(Charset charset) {
		if (charset == null || Charset.defaultCharset().equals(charset)) return EOL_BYTES;
		return ByteProvider.of(EOL.getBytes(charset));
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException ioExceptionf(String format, Object... args) {
		return new IOException(StringUtil.format(format, args));
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException ioExceptionf(Throwable cause, String format, Object... args) {
		return new IOException(StringUtil.format(format, args), cause);
	}

	/**
	 * Clears available bytes from an input stream and returns the total number of bytes cleared.
	 */
	public static long clear(InputStream in) throws IOException {
		return in.skip(in.available());
	}

	/**
	 * Clears available chars from a reader and returns the total number of chars cleared.
	 */
	public static long clear(Reader in) throws IOException {
		var buffer = new char[SKIP_BUFFER_SIZE];
		long total = 0;
		while (in.ready()) {
			int n = in.read(buffer);
			if (n < 0) break;
			total += n;
		}
		return total;
	}

	/**
	 * Returns a print stream that swallows all output.
	 */
	public static PrintStream nullPrintStream() {
		return new PrintStream(IoStreamUtil.nullOut);
	}

	/**
	 * Returns the system tmp directory.
	 */
	public static Path systemTempDir() {
		String property = SystemVars.sys(TMP_DIR_PROPERTY);
		return FunctionUtil.safeApply(property, Path::of);
	}

	/**
	 * Returns the user home path extended with given paths, based on system property 'user.home'.
	 * Returns null if property does not exist.
	 */
	public static Path userHome(String... paths) {
		return systemPropertyPath(USER_HOME_PROPERTY, paths);
	}

	/**
	 * Returns the current path extended with given paths, based on system property 'user.dir'.
	 * Returns null if property does not exist.
	 */
	public static Path userDir(String... paths) {
		return systemPropertyPath(USER_DIR_PROPERTY, paths);
	}

	/**
	 * Returns the path extending from a given system property path. Returns null if the system
	 * property does not exist.
	 */
	public static Path systemPropertyPath(String name, String... paths) {
		String property = SystemVars.sys(name);
		return property == null ? null : Path.of(property, paths);
	}

	/**
	 * Returns the path extending from a given environment variable. Returns null if the variable
	 * does not exist.
	 */
	public static Path environmentPath(String name, String... paths) {
		String property = SystemVars.env(name);
		return property == null ? null : Path.of(property, paths);
	}

	/**
	 * Join paths using the system path separator.
	 */
	public static String pathVariable(String... paths) {
		return Stream.of(paths).map(String::trim).filter(StringUtil::nonEmpty)
			.collect(Collectors.joining(File.pathSeparator));
	}

	/**
	 * Extract unique paths in order, using the system path separator.
	 */
	public static Set<String> variablePaths(String variable) {
		if (variable == null) return Set.of();
		return StreamUtil.toSet(PATH_SEPARATOR_REGEX.splitAsStream(variable).map(String::trim)
			.filter(StringUtil::nonEmpty));
	}

	/**
	 * Extends a path. Returns null if path is null.
	 */
	public static Path extend(Path path, String... paths) {
		if (path == null || paths.length == 0) return path;
		return newPath(path, path.toString(), paths);
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
	 * Checks if a path is root.
	 */
	public static boolean isRoot(Path path) {
		return path != null && path.isAbsolute() && path.equals(path.getRoot());
	}

	/**
	 * Replaces the last part of the path.
	 */
	public static Path changeName(Path path, String fileName) {
		if (path == null) return null;
		Path parent = path.getParent();
		if (parent != null) return parent.resolve(fileName);
		if (path.isAbsolute()) return path.resolve(fileName);
		return newPath(path, fileName);
	}

	/**
	 * Retrieves the path section name at given index if it exists, otherwise null.
	 */
	public static String name(Path path, int index) {
		if (path == null || index < 0 || index >= path.getNameCount()) return null;
		return path.getName(index).toString();
	}

	/**
	 * Retrieves the path from given index.
	 */
	public static Path subpath(Path path, int index) {
		if (path == null) return null;
		return subpath(path, index, path.getNameCount());
	}

	/**
	 * Retrieves the path from given index.
	 */
	public static Path subpath(Path path, int start, int end) {
		if (path == null) return null;
		int count = path.getNameCount();
		if (count == 1 && path.getName(0).toString().isEmpty()) count = 0;
		if (start == end && start <= count) return newPath(path, "");
		return path.subpath(start, end);
	}

	/**
	 * Shortens the end of a path by given number of levels.
	 */
	public static Path shorten(Path path, int levels) {
		if (path == null || levels == 0) return path;
		int nameCount = path.getNameCount();
		boolean absolute = path.isAbsolute();
		if (nameCount == levels) return absolute ? path.getRoot() : newPath(path, "");
		if (!path.isAbsolute()) return subpath(path, 0, nameCount - levels);
		return path.getRoot().resolve(subpath(path, 0, nameCount - levels));
	}

	/**
	 * Creates a new path using the FileSystem of the given path.
	 */
	@SuppressWarnings("resource")
	public static Path newPath(Path ref, String first, String... more) {
		return ref.getFileSystem().getPath(first, more);
	}

	/**
	 * Returns the last segment of the path as a string, or blank if empty. Returns null for a null
	 * path.
	 */
	public static String fileName(Path path) {
		if (path == null) return null;
		Path fileName = path.getFileName();
		return fileName == null ? "" : fileName.toString();
	}

	/**
	 * Returns the file name without extension, full name if none, or null for null path. Does not
	 * check if path is a file or if it exists. Returns full name for file names starting with a
	 * dot, and no extension.
	 */
	public static String fileNameWithoutExt(Path path) {
		String fileName = fileName(path);
		if (fileName == null) return null;
		int i = fileName.lastIndexOf('.');
		return i <= 0 ? fileName : fileName.substring(0, i);
	}

	/**
	 * Returns the file extension, empty string if none, or null for null path. Does not check if
	 * path is a file or if it exists. Returns empty string for file names starting with a dot, and
	 * no extension.
	 */
	public static String extension(Path path) {
		String fileName = fileName(path);
		if (fileName == null) return null;
		int i = fileName.lastIndexOf('.');
		return i <= 0 ? "" : fileName.substring(i + 1);
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
		FileVisitor<Path> visitor = FileVisitUtil.visitor(null, (path, _) -> {
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
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			return !stream.iterator().hasNext();
		}
	}

	/**
	 * Poll until string data is available from input stream.
	 */
	public static String pollString(InputStream in) throws IOException {
		return pollString(in, Charset.defaultCharset());
	}

	/**
	 * Poll until string data is available from input stream.
	 */
	public static String pollString(InputStream in, Charset charset) throws IOException {
		return new String(pollBytes(in), charset);
	}

	/**
	 * Poll until data is available from input stream.
	 */
	public static byte[] pollBytes(InputStream in) throws IOException {
		return in.readNBytes(pollForData(in, 1));
	}

	/**
	 * Get a char from stdin, return 0 if no chars available.
	 */
	public static char availableChar() {
		return availableChar(System.in);
	}

	/**
	 * Get a char from input stream, return 0 if nothing available
	 */
	public static char availableChar(InputStream in) {
		try {
			if (in.available() > 0) return (char) in.read();
		} catch (IOException e) {
			//
		}
		return (char) 0;
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static String availableString(InputStream in) throws IOException {
		return availableString(in, Charset.defaultCharset());
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static String availableString(InputStream in, Charset charset) throws IOException {
		if (in == null) return null;
		return availableBytes(in).getString(0, charset);
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static ByteProvider availableBytes(InputStream in) throws IOException {
		if (in == null) return null;
		int count = in.available();
		if (count == 0) return ByteProvider.empty();
		byte[] buffer = new byte[count];
		count = in.read(buffer);
		if (count <= 0) return ByteProvider.empty();
		return ByteArray.Immutable.wrap(buffer, 0, count);
	}

	/**
	 * Reads available single bytes without blocking, stopping if a line separator is found. The
	 * returned string contains the line separator if found.
	 */
	public static String availableLine(InputStream in) throws IOException {
		return availableLine(in, Charset.defaultCharset());
	}

	/**
	 * Reads available single bytes without blocking, stopping if a line separator is found. The
	 * returned string contains the line separator if found.
	 */
	public static String availableLine(InputStream in, Charset charset) throws IOException {
		if (in == null) return null;
		var eol = eolBytes(charset);
		return availableBytes(in, (b, n) -> eol.isEqualTo(0, b, n - eol.length(), eol.length()))
			.getString(0, charset);
	}

	/**
	 * Reads available single bytes without blocking, stopping if the predicate tests true. The
	 * predicate receives the byte array buffer and the number of bytes filled.
	 */
	public static <E extends Exception> ByteProvider availableBytes(InputStream in,
		ExceptionObjIntPredicate<E, byte[]> predicate) throws E, IOException {
		if (in == null) return null;
		int count = in.available();
		if (count == 0) return ByteProvider.empty();
		var buffer = new byte[count];
		int i = 0;
		while (i < count) {
			var b = in.read();
			if (b < 0) break;
			buffer[i++] = (byte) b;
			if (predicate != null && predicate.test(buffer, i)) break;
		}
		return ByteArray.Immutable.wrap(buffer, 0, i);
	}

	/**
	 * Reads until buffer is filled; calls readNBytes.
	 */
	public static int readBytes(InputStream in, byte[] b) throws IOException {
		if (in == null || b == null) return 0;
		return in.readNBytes(b, 0, b.length);
	}

	/**
	 * Reads available bytes, blocking until at least one byte is available.
	 */
	public static ByteProvider readNext(InputStream in) throws IOException {
		if (in == null) return null;
		int b = in.read();
		if (b == -1) return ByteProvider.empty();
		byte[] buffer = new byte[1 + in.available()];
		buffer[0] = (byte) b;
		int n = Math.max(in.read(buffer, 1, buffer.length - 1), 0) + 1;
		return ByteArray.Immutable.wrap(buffer, 0, n);
	}

	/**
	 * Reads available bytes as a String, blocking until at least one byte is available.
	 */
	public static String readNextString(InputStream in) throws IOException {
		return readNextString(in, UTF_8);
	}

	/**
	 * Reads available bytes as a String, blocking until at least one byte is available.
	 */
	public static String readNextString(InputStream in, Charset charset) throws IOException {
		return readNext(in).getString(0, charset);
	}

	/**
	 * Wait for given number of bytes to be available on input stream.
	 */
	public static int pollForData(InputStream in, int count) throws IOException {
		return pollForData(in, count, 0);
	}

	/**
	 * Wait for given number of bytes to be available on input stream by polling.
	 */
	public static int pollForData(InputStream in, int count, long timeoutMs) throws IOException {
		return pollForData(in, count, timeoutMs, READ_POLL_MS);
	}

	/**
	 * Wait for given number of bytes to be available on input stream by polling.
	 */
	public static int pollForData(InputStream in, int count, long timeoutMs, long pollMs)
		throws IOException {
		long t = System.currentTimeMillis() + timeoutMs;
		while (true) {
			int n = in.available();
			if (n >= count) return n;
			if (timeoutMs != 0 && (System.currentTimeMillis() > t)) throw new IoTimeoutException(
				"Bytes not available within " + timeoutMs + "ms: " + n + "/" + count);
			ConcurrentUtil.delay(pollMs);
		}
	}

	/**
	 * Transfer bytes from input to output stream in current thread, until EOF. A polling delay is
	 * used in each iteration if the InputStream in non-blocking. Returns the total number of bytes
	 * transferred.
	 */
	public static long pipe(InputStream in, OutputStream out) throws IOException {
		return pipe(in, out, new byte[BUFFER_SIZE_DEF], READ_POLL_MS);
	}

	/**
	 * Transfer bytes from input to output stream in current thread, until EOF. Returns the total
	 * number of bytes transferred.
	 */
	public static long pipe(InputStream in, OutputStream out, byte[] buffer, int delayMs)
		throws IOException {
		long total = 0;
		while (true) {
			ConcurrentUtil.checkRuntimeInterrupted();
			int n = in.read(buffer);
			if (n < 0) break;
			if (n > 0) out.write(buffer, 0, n);
			else ConcurrentUtil.delay(delayMs);
			total += n;
		}
		return total;
	}

	/**
	 * Convert file path to unix format.
	 */
	public static String pathToUnix(Path path) {
		return pathToUnix(path.toString());
	}

	/**
	 * Convert file path to unix format.
	 */
	public static String pathToUnix(String path) {
		return convertPath(path, File.separatorChar, '/');
	}

	/**
	 * Convert unix format path to file path.
	 */
	public static String unixToPath(String unix) {
		return convertPath(unix, '/', File.separatorChar);
	}

	/**
	 * Convert path containing separators to use another separator.
	 */
	public static String convertPath(String path, char currentSeparator, char newSeparator) {
		return path.replace(currentSeparator, newSeparator); // does nothing for same separators
	}

	/**
	 * Streams relative paths recursively under a given directory. Must be used in context of
	 * try-with-resources.
	 */
	public static WrappedStream<IOException, Path> walkRelative(Path dir) throws IOException {
		return walkRelative(dir, NULL_FILTER);
	}

	/**
	 * Streams relative filtered paths recursively under a given directory. A null filter matches
	 * all paths. Must be used in context of try-with-resources.
	 */
	@SuppressWarnings("resource")
	public static WrappedStream<IOException, Path> walkRelative(Path dir,
		ExceptionPredicate<IOException, Path> filter) throws IOException {
		int levels = dir.getNameCount();
		return walk(dir, filter).map(path -> subpath(path, levels));
	}

	/**
	 * Streams relative filtered paths recursively under a given directory. A null filter matches
	 * all paths. Must be used in context of try-with-resources.
	 */
	public static WrappedStream<IOException, Path> walkRelative(Path dir, String syntaxPattern)
		throws IOException {
		ExceptionPredicate<IOException, Path> filter =
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test;
		return walkRelative(dir, filter);
	}

	/**
	 * Streams paths recursively under a given directory. Must be used in context of
	 * try-with-resources.
	 */
	public static WrappedStream<IOException, Path> walk(Path dir) throws IOException {
		return walk(dir, NULL_FILTER);
	}

	/**
	 * Streams filtered paths recursively under a given directory. A null filter matches all paths.
	 * Must be used in context of try-with-resources.
	 */
	@SuppressWarnings("resource")
	public static WrappedStream<IOException, Path> walk(Path dir,
		ExceptionPredicate<IOException, Path> filter) throws IOException {
		return WrappedStream.<IOException, Path>of(Files.walk(dir))
			.filter(defaultValue(filter, NULL_FILTER));
	}

	/**
	 * Streams paths recursively under a given directory. A null pattern matches all paths. The
	 * pattern is applied against the full path. Must be used in context of try-with-resources.
	 */
	public static WrappedStream<IOException, Path> walk(Path dir, String syntaxPattern)
		throws IOException {
		ExceptionPredicate<IOException, Path> filter =
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test;
		return walk(dir, filter);
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
	public static List<Path> pathsRelative(Path dir, ExceptionPredicate<IOException, Path> filter)
		throws IOException {
		ExceptionPredicate<IOException, Path> test = defaultValue(filter, NULL_FILTER);
		int levels = dir.getNameCount();
		return pathsCollect(dir, path -> test.test(path) ? subpath(path, levels) : null);
	}

	/**
	 * Lists relative filtered paths recursively under a given directory. A null pattern matches all
	 * paths. The pattern is applied against the full path.
	 */
	public static List<Path> pathsRelative(Path dir, String syntaxPattern) throws IOException {
		ExceptionPredicate<IOException, Path> filter =
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test;
		return pathsRelative(dir, filter);
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
	public static List<Path> paths(Path dir, ExceptionPredicate<IOException, Path> filter)
		throws IOException {
		ExceptionPredicate<IOException, Path> test = defaultValue(filter, NULL_FILTER);
		return pathsCollect(dir, path -> test.test(path) ? path : null);
	}

	/**
	 * Lists filtered paths recursively under a given directory. A null pattern matches all paths.
	 * The pattern is applied against the full path.
	 */
	public static List<Path> paths(Path dir, String syntaxPattern) throws IOException {
		ExceptionPredicate<IOException, Path> filter =
			syntaxPattern == null ? null : PathPattern.of(syntaxPattern).matcher(dir)::test;
		return paths(dir, filter);
	}

	/**
	 * Recursively collects mapped paths under a given directory. The mapping function excludes
	 * entries by returning null.
	 */
	public static <T> List<T> pathsCollect(Path dir, ExceptionFunction<IOException, Path, T> mapper)
		throws IOException {
		Objects.requireNonNull(dir);
		Objects.requireNonNull(mapper);
		List<T> list = new ArrayList<>();
		var visitFn = FileVisitUtil.<Path, BasicFileAttributes>adaptConsumer(
			path -> safeAccept(mapper.apply(path), list::add));
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
	public static List<String> listNames(Path dir, ExceptionPredicate<IOException, Path> filter)
		throws IOException {
		ExceptionPredicate<IOException, Path> test = defaultValue(filter, NULL_FILTER);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, test::test)) {
			return listCollect(stream, IoUtil::fileName);
		}
	}

	/**
	 * Lists filtered path names under a directory (no recursion). A null pattern matches all paths.
	 * The pattern is applied against the file name path, not the full path.
	 */
	public static List<String> listNames(Path dir, String syntaxPattern) throws IOException {
		ExceptionPredicate<IOException, Path> filter = syntaxPattern == null ? null :
			PathFilters.byFileNamePath(PathPattern.of(syntaxPattern).matcher(dir)::test);
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
	public static List<Path> list(Path dir, ExceptionPredicate<IOException, Path> filter)
		throws IOException {
		ExceptionPredicate<IOException, Path> test = defaultValue(filter, NULL_FILTER);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, test::test)) {
			return listCollect(stream, path -> path);
		}
	}

	/**
	 * Lists filtered paths under a directory (no recursion). A null pattern matches all paths. The
	 * pattern is applied against the file name path, not the full path.
	 */
	public static List<Path> list(Path dir, String syntaxPattern) throws IOException {
		ExceptionPredicate<IOException, Path> filter = syntaxPattern == null ? null :
			PathFilters.byFileNamePath(PathPattern.of(syntaxPattern).matcher(dir)::test);
		return list(dir, filter);
	}

	/**
	 * Collects mapped paths from a directory stream, then closes the stream. The mapping function
	 * excludes entries by returning null.
	 */
	@SuppressWarnings("resource")
	public static <T> List<T> listCollect(DirectoryStream<Path> stream,
		ExceptionFunction<IOException, Path, T> mapper) throws IOException {
		Objects.requireNonNull(stream);
		Objects.requireNonNull(mapper);
		List<T> list = new ArrayList<>();
		dirStreamForEach(stream, path -> FunctionUtil.safeApply(mapper.apply(path), list::add));
		return list;
	}

	/**
	 * Applies a function to each path in a directory stream, then closes the stream.
	 */
	public static void dirStreamForEach(DirectoryStream<Path> stream,
		ExceptionConsumer<IOException, Path> consumer) throws IOException {
		try (stream) {
			WRAPPER.unwrap(() -> stream.forEach(WRAPPER.wrap(consumer::accept)));
		}
	}

	/**
	 * Gets content from input stream as a string.
	 */
	public static String readString(InputStream in) throws IOException {
		return readString(in, UTF_8);
	}

	/**
	 * Gets content from input stream as a string.
	 */
	public static String readString(InputStream in, Charset charset) throws IOException {
		return new String(in.readAllBytes(), charset);
	}

	/**
	 * Returns a stream of lines lazily read from input stream.
	 */
	public static Stream<String> lines(InputStream in) {
		return lines(in, UTF_8);
	}

	/**
	 * Returns a stream of lines lazily read from input stream.
	 */
	public static Stream<String> lines(InputStream in, Charset charset) {
		return new BufferedReader(new InputStreamReader(in, charset)).lines();
	}

	/**
	 * Copies file contents from one file to another, creating the destination directories if
	 * necessary. Returns the destination path. Removes created directories on failure.
	 */
	public static Path copyFile(Path src, Path dest) throws IOException {
		FileTracker tracker = new FileTracker();
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
		FileTracker tracker = new FileTracker();
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
		FileTracker tracker = new FileTracker();
		try {
			tracker.file(file); // creates parent dirs
			try (OutputStream out = Files.newOutputStream(file)) {
				data.writeTo(0, out);
				out.flush();
			}
			return data.length();
		} catch (RuntimeException | IOException e) {
			tracker.delete();
			throw e;
		}
	}

	/**
	 * Returns the url path for class.
	 */
	public static URL classUrl(Class<?> cls) {
		if (cls == null) return null;
		return cls.getResource(cls.getSimpleName() + CLASS_SUFFIX);
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static byte[] resource(Class<?> cls, String... paths) throws IOException {
		try (ResourcePath rp = ResourcePath.of(cls, paths)) {
			return rp.readBytes();
		}
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static String resourceString(Class<?> cls, String... paths) throws IOException {
		return resourceString(cls, UTF_8, paths);
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static String resourceString(Class<?> cls, Charset charset, String... paths)
		throws IOException {
		try (ResourcePath rp = ResourcePath.of(cls, paths)) {
			return rp.readString(charset);
		}
	}

}
