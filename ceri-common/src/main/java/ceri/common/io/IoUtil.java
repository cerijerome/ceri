package ceri.common.io;

import static ceri.common.util.BasicUtil.shouldNotThrow;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.util.BasicUtil;

/**
 * I/O utility functions.
 */
public class IoUtil {
	private static final Pattern FILE_SEPARATOR_REGEX = Pattern.compile("\\" + File.separatorChar);
	private static final int MAX_CLEAR_BUFFER = 8 * 1024;
	private static final int MAX_UUID_ATTEMPTS = 10; // Shouldn't be needed
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 32;
	private static final String CLASS_SUFFIX = ".class";
	private static final int READ_POLL_MS = 100;

	private IoUtil() {}

	/**
	 * Clears available bytes from an input stream and returns the total number of bytes cleared.
	 */
	public static int clear(InputStream in) throws IOException {
		int count = 0;
		byte[] buffer = null;
		while (true) {
			int n = Math.min(in.available(), MAX_CLEAR_BUFFER);
			if (n <= 0) break;
			if (buffer == null) buffer = new byte[n];
			int i = in.read(buffer);
			if (i <= 0) break;
			count += i;
		}
		return count;
	}

	/**
	 * Returns a print stream that swallows all output.
	 */
	public static PrintStream nullPrintStream() {
		return new PrintStream(IoStreamUtil.nullOut());
	}

	/**
	 * Returns the system tmp directory.
	 */
	public static Path systemTempDir() {
		String property = System.getProperty("java.io.tmpdir");
		return property == null ? null : Path.of(property);
	}

	/**
	 * Returns the user home path extended with given paths, based on system property 'user.home'.
	 * Returns null if property does not exist.
	 */
	public static Path userHome(String... paths) {
		return systemPropertyPath("user.home", paths);
	}

	/**
	 * Returns the path extending from a given system property path.
	 * Returns null if the system property does not exist.
	 */
	public static Path systemPropertyPath(String name, String... paths) {
		String property = System.getProperty(name);
		return property == null ? null : Path.of(property, paths);
	}

	/**
	 * Returns the path extending from a given environment variable.
	 * Returns null if the variable does not exist.
	 */
	public static Path environmentPath(String name, String... paths) {
		String property = System.getenv(name);
		return property == null ? null : Path.of(property, paths);
	}

	/**
	 * Extends a path. Returns null if path is null.
	 */
	public static Path extend(Path path, String... paths) {
		if (paths.length == 0) return path;
		return path == null ? null : Path.of(path.toString(), paths);
	}

	/**
	 * Create a temp dir with random name under given dir. Use null for current dir.
	 */
	public static File createTempDir(File rootDir) {
		return createTempDir(rootDir, IoUtil::generateTempDir);
	}

	static File createTempDir(File rootDir, Function<File, File> generator) {
		FileTracker tracker = new FileTracker();
		for (int i = MAX_UUID_ATTEMPTS; i > 0; i--) {
			File tempDir = generator.apply(rootDir);
			if (tempDir.exists()) continue;
			tracker.dir(tempDir); // create dir path
			if (tempDir.exists()) return tempDir;
			// unable to create full path, delete any created parent dirs
			tracker.delete();
		}
		throw new IllegalStateException(
			"Unable to create random temp dir in " + MAX_UUID_ATTEMPTS + " attempts");
	}

	private static File generateTempDir(File rootDir) {
		String dirName = UUID.randomUUID().toString();
		return new File(rootDir, dirName);
	}

	/**
	 * Delete all files under the directory, and the directory itself. Be careful!
	 */
	public static void deleteAll(File root) {
		if (root.isDirectory()) for (File file : root.listFiles())
			deleteAll(file);
		root.delete();
	}

	/**
	 * Closes a closeable stream. Returns false if this resulted in an I/O error.
	 */
	public static boolean close(Closeable closeable) {
		if (closeable == null) return false;
		try {
			closeable.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Get a char from stdin, return 0 if no key pressed
	 */
	public static char getChar() {
		return getChar(System.in);
	}

	/**
	 * Get a char from input stream, return 0 if nothing available
	 */
	public static char getChar(InputStream in) {
		try {
			if (in.available() > 0) return (char) in.read();
		} catch (IOException e) {
			//
		}
		return (char) 0;
	}

	/**
	 * Get a string from input stream in interruptible way.
	 */
	public static String readString(InputStream in) throws IOException {
		return readString(in, Charset.defaultCharset());
	}

	/**
	 * Get a string from input stream in interruptible way.
	 */
	public static String readString(InputStream in, Charset charset) throws IOException {
		int n = waitForData(in, 1);
		byte[] buffer = new byte[n];
		n = in.read(buffer);
		return new String(buffer, 0, n, charset);
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static String readAvailableString(InputStream in) throws IOException {
		return readAvailableString(in, Charset.defaultCharset());
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static String readAvailableString(InputStream in, Charset charset) throws IOException {
		if (in == null) return null;
		return readAvailable(in).asString(charset);
	}

	/**
	 * Reads all available bytes without blocking.
	 */
	public static ImmutableByteArray readAvailable(InputStream in) throws IOException {
		if (in == null) return null;
		int count = in.available();
		if (count == 0) return ImmutableByteArray.EMPTY;
		byte[] buffer = new byte[count];
		count = in.read(buffer);
		if (count <= 0) return ImmutableByteArray.EMPTY;
		return ImmutableByteArray.wrap(buffer, 0, count);
	}

	/**
	 * Reads until buffer is filled; calls readNBytes.
	 */
	public static int readBytes(InputStream in, byte[] b) throws IOException {
		if (in == null || b == null) return 0;
		return in.readNBytes(b, 0, b.length);
	}

	/**
	 * Wait for given number of bytes to be available on input stream.
	 */
	public static int waitForData(InputStream in, int count) throws IOException {
		return waitForData(in, count, 0);
	}

	/**
	 * Wait for given number of bytes to be available on input stream.
	 */
	public static int waitForData(InputStream in, int count, long timeoutMs) throws IOException {
		return waitForData(in, count, timeoutMs, READ_POLL_MS);
	}

	/**
	 * Wait for given number of bytes to be available on input stream.
	 */
	public static int waitForData(InputStream in, int count, long timeoutMs, long pollMs)
		throws IOException {
		long t = System.currentTimeMillis() + timeoutMs;
		while (true) {
			int n = in.available();
			if (n >= count) return n;
			if (timeoutMs != 0 && (System.currentTimeMillis() > t)) throw new IoTimeoutException(
				"Bytes not available within " + timeoutMs + "ms: " + n + "/" + count);
			BasicUtil.delay(pollMs);
			ConcurrentUtil.checkRuntimeInterrupted();
		}
	}

	/**
	 * Checks if the current thread has been interrupted, and throws an InterruptedIOException.
	 * With
	 * blocking I/O, (i.e. anything that doesn't use the nio packages) a thread can be interrupted,
	 * but the I/O will still proceed, and the InterruptedException is only thrown when a call to
	 * Thread.sleep() or Object.wait() is made. This method or the BasicUtil.checkInterrupted()
	 * method is recommended to be called after any blocking I/O calls.
	 */
	public static void checkIoInterrupted() throws InterruptedIOException {
		if (Thread.interrupted()) throw new InterruptedIOException("Thread has been interrupted");
	}

	/**
	 * Execute runnable and convert non-io exception to io exception.
	 */
	public static void execIo(ExceptionRunnable<Exception> runnable) throws IOException {
		try {
			runnable.run();
		} catch (RuntimeException | IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Execute callable and convert non-io exception to io exception.
	 */
	public static <T> T callableIo(Callable<T> callable) throws IOException {
		try {
			return callable.call();
		} catch (RuntimeException | IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Convert file path to unix-style
	 */
	public static String unixPath(File file) {
		return unixPath(file.getPath());
	}

	/**
	 * Convert file path to unix-style
	 */
	public static String unixPath(String path) {
		return unixPath(path, File.separatorChar, FILE_SEPARATOR_REGEX);
	}

	static String unixPath(String path, char separator, Pattern regex) {
		if (separator == '/') return path;
		return regex.matcher(path).replaceAll("/");
	}

	/**
	 * Returns the set of relative file paths under a given directory in Unix '/' format
	 */
	public static List<String> getFilenames(File dir) {
		return getFilenames(dir, null);
	}

	/**
	 * Returns the set of relative file paths under a given directory in Unix '/' format. A null
	 * filter matches all files.
	 */
	public static List<String> getFilenames(File dir, FilenameFilter filter) {
		List<String> list = new ArrayList<>();
		addFilenames(list, dir, null, filter);
		return list;
	}

	/**
	 * Returns the set of file paths under a given directory.
	 */
	public static List<File> getFiles(File dir) {
		return getFiles(dir, null);
	}

	/**
	 * Returns the set of file paths under a given directory. A null filter matches all files.
	 */
	public static List<File> getFiles(File dir, FileFilter filter) {
		List<File> list = new ArrayList<>();
		addFiles(list, dir, filter);
		return list;
	}

	/**
	 * Joins two '/' based paths together, making sure only one '/' is between the two paths.
	 */
	public static String joinPaths(String path1, String path2) {
		if (path1 == null) path1 = "";
		if (path2 == null) path2 = "";
		if (path1.isEmpty()) return path2;
		if (path2.isEmpty()) return path1;
		StringBuilder path = new StringBuilder(path1);
		if (path.charAt(path.length() - 1) == '/') path.setLength(path.length() - 1);
		if (path2.charAt(0) != '/') path.append('/');
		path.append(path2);
		return path.toString();
	}

	/**
	 * Returns the file path relative to a given dir in '/' format. Or the returns the file if not
	 * relative.
	 */
	public static String getRelativePath(File dir, File file) throws IOException {
		dir = dir.getCanonicalFile();
		String fileName = unixPath(file.getCanonicalPath());
		StringBuilder backPath = new StringBuilder();
		while (dir != null) {
			String dirName = unixPath(dir);
			if (!dirName.endsWith("/")) dirName += "/";
			if (fileName.startsWith(dirName))
				return backPath.toString() + fileName.substring(dirName.length());
			backPath.append("../");
			dir = dir.getParentFile();
		}
		return fileName;
	}

	/**
	 * Deletes all empty directories under this directory.
	 */
	public static void deleteEmptyDirs(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				deleteEmptyDirs(file);
				file.delete(); // Only deletes if empty
			}
		}
	}

	/**
	 * Copies file contents from one file to another, creating the destination directories if
	 * necessary.
	 */
	public static void copyFile(File src, File dest) throws IOException {
		FileTracker tracker = new FileTracker().file(dest); // creates parent dirs
		try (InputStream in = new BufferedInputStream(new FileInputStream(src));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
			transferContent(in, out, 0);
		} catch (RuntimeException | IOException e) {
			tracker.delete();
			throw e;
		}
	}

	/**
	 * Gets content from a file as a byte array.
	 */
	public static byte[] getContent(File file) throws IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			return getContent(in, 0);
		}
	}

	public static String getContentString(String filename) throws IOException {
		return getContentString(new File(filename));
	}

	public static String getContentString(File file) throws IOException {
		byte[] bytes = getContent(file);
		return new String(bytes).intern();
	}

	/**
	 * Gets content from input stream as a string. Use 0 for default buffer size.
	 */
	public static String getContentString(InputStream in, int bufferSize) throws IOException {
		byte[] bytes = getContent(in, bufferSize);
		return new String(bytes).intern();
	}

	/**
	 * Gets content from an input stream as byte array. Use 0 for default buffer size.
	 */
	public static byte[] getContent(InputStream in, int bufferSize) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transferContent(in, out, bufferSize);
		close(out);
		return out.toByteArray();
	}

	/**
	 * Writes bytes from input stream to a file.
	 */
	public static void setContent(File file, InputStream in) throws IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			transferContent(in, out, 0);
		}
	}

	/**
	 * Writes byte array content to a file.
	 */
	public static void setContent(File file, byte[] content) throws IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(content);
			out.flush();
		}
	}

	/**
	 * Writes byte array content to a file.
	 */
	public static void setContent(File file, ImmutableByteArray data) throws IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			data.writeTo(out);
			out.flush();
		}
	}

	/**
	 * Writes a string to a file using default encoding.
	 */
	public static void setContentString(File file, String content) throws IOException {
		setContent(file, content.getBytes());
	}

	/**
	 * Transfers content from an input stream to an output stream, using the specified buffer size,
	 * or 0 for default buffer size.
	 */
	public static void transferContent(InputStream in, OutputStream out, int bufferSize)
		throws IOException {
		if (bufferSize == 0) bufferSize = DEFAULT_BUFFER_SIZE;
		byte[] buffer = new byte[bufferSize];
		while (true) {
			int count = in.read(buffer);
			if (count == -1) break;
			if (out != null) out.write(buffer, 0, count);
		}
		if (out != null) out.flush();
	}

	/**
	 * Attempts to fill given buffer by reading from the input stream. Returns the last position
	 * filled in the buffer. Returns less than given length if end of stream is reached.
	 */
	public static int fillBuffer(InputStream in, byte[] buffer) throws IOException {
		return fillBuffer(in, buffer, 0, buffer.length);
	}

	/**
	 * Attempts to fill given buffer by reading from the input stream until full. Returns the total
	 * number of bytes read. Returns less than given length if end of stream is reached.
	 */
	public static int fillBuffer(InputStream in, byte[] buffer, int offset, int len)
		throws IOException {
		if (offset < 0) throw new IllegalArgumentException("Offset must be >= 0: " + offset);
		if (offset + len > buffer.length)
			throw new IllegalArgumentException("Offset plus length must not exceed buffer size (" +
				buffer.length + "): " + offset + " + " + len);
		int pos = offset;
		while (pos < offset + len) {
			int count = in.read(buffer, pos, offset + len - pos);
			if (count == -1) break;
			pos += count;
		}
		return pos - offset;
	}

	/**
	 * Lists resources from same package as class. Handles file resources and resources within a
	 * jar
	 * file.
	 */
	public static List<String> listResources(Class<?> cls) throws IOException {
		return ResourceLister.of(cls).list();
	}

	/**
	 * Lists resources from same package as class, under given sub-directory, matching given regex
	 * pattern. Handles file resources and resources within a jar file.
	 */
	public static List<String> listResources(Class<?> cls, String subDir, Pattern pattern)
		throws IOException {
		return ResourceLister.of(cls, subDir, pattern).list();
	}

	/**
	 * Gets a path representing a resource. Will fail if the class is in a jar file.
	 */
	public static Path getResourcePath(Class<?> cls, String resourceName) {
		return Paths.get(getResourceFile(cls, resourceName).toURI());
	}

	/**
	 * Gets a file representing a resource. Will fail if the class is in a jar file.
	 */
	public static File getResourceFile(Class<?> cls, String resourceName) {
		URL url = cls.getResource(resourceName);
		if (url != null) return shouldNotThrow(() -> new File(url.toURI()));
		throw new NullPointerException("Resource not found for " + cls + ": " + resourceName);
	}

	/**
	 * Gets resource as a string from same package as class, with given filename.
	 */
	public static String getResourceString(Class<?> cls, String resourceName) throws IOException {
		return new String(getResource(cls, resourceName)).intern();
	}

	/**
	 * Gets resource from same package as class, with given filename.
	 */
	public static byte[] getResource(Class<?> cls, String resourceName) throws IOException {
		try (InputStream in = cls.getResourceAsStream(resourceName)) {
			if (in == null) throw new MissingResourceException(
				"Missing resource for class " + cls.getName() + ": " + resourceName, cls.getName(),
				resourceName);
			return IoUtil.getContent(in, 0);
		}
	}

	/**
	 * Gets resource from same package as class, with same name as the class and given suffix.
	 */
	public static String getClassResourceAsString(Class<?> cls, String suffix) throws IOException {
		return new String(getClassResource(cls, suffix)).intern();
	}

	/**
	 * Gets resource from same package as class, with same name as the class and given suffix.
	 */
	public static byte[] getClassResource(Class<?> cls, String suffix) throws IOException {
		return getResource(cls, cls.getSimpleName() + "." + suffix);
	}

	/**
	 * Returns the root url path for class resources.
	 */
	public static String getResourcePath(Class<?> cls) {
		String name = cls.getSimpleName() + CLASS_SUFFIX;
		URL url = cls.getResource(name);
		String urlStr = url.toString();
		return urlStr.substring(0, urlStr.length() - name.length());
	}

	/**
	 * Returns the url path for class.
	 */
	public static URL getClassUrl(Class<?> cls) {
		String name = cls.getSimpleName() + CLASS_SUFFIX;
		return cls.getResource(name);
	}

	/**
	 * Scans directory for files and adds to the given set.
	 */
	private static void addFilenames(List<String> list, File rootDir, String root,
		FilenameFilter filter) {
		String[] files = rootDir.list();
		if (files == null) return;
		for (String file : files) {
			String path = root == null ? file : root + "/" + file;
			if (filter == null || filter.accept(rootDir, path)) list.add(path);
			File f = new File(rootDir, file);
			if (f.isDirectory()) addFilenames(list, f, path, filter);
		}
	}

	/**
	 * Scans directory for files and adds to the given set.
	 */
	private static void addFiles(List<File> list, File root, FileFilter filter) {
		File[] files = root.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (filter == null || filter.accept(file)) list.add(file);
			if (file.isDirectory()) addFiles(list, file, filter);
		}
	}

}
