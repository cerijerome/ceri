/*
 * Created on Aug 21, 2004
 */
package ceri.common.io;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * I/O utility functions.
 */
public class IoUtil {
	private static final int MAX_UUID_ATTEMPTS = 10; // Shouldn't be needed
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 32;

	public static File systemTempDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	/**
	 * Create a temp dir with random name under given dir. Use null for current
	 * dir.
	 */
	public static File createTempDir(File rootDir) {
		FileTracker tracker = new FileTracker();
		for (int i = MAX_UUID_ATTEMPTS; i > 0; i--) {
			String dirName = UUID.randomUUID().toString();
			File tempDir = new File(rootDir, dirName);
			if (tempDir.exists()) continue;
			tracker.dir(tempDir); // create dir path
			if (!tempDir.exists()) {
				tracker.delete(); // delete any created parent dirs
				throw new IllegalStateException("Unable to create directory " +
					tempDir.getAbsolutePath());
			}
			return tempDir;
		}
		throw new IllegalStateException("Unable to create random temp dir in " + MAX_UUID_ATTEMPTS +
			" attempts");
	}

	/**
	 * Delete all files under the directory, and the directory itself.
	 * Be careful!
	 */
	public static void deleteAll(File root) {
		if (root.isDirectory()) for (File file : root.listFiles())
			deleteAll(file);
		root.delete();
	}

	/**
	 * Closes a closeable stream. Returns false if this resulted in an error.
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
	public static char getChar() throws IOException {
		if (System.in.available() > 0) {
			return (char) System.in.read();
		}
		return (char) 0;
	}

	/**
	 * Checks if the current thread has been interrupted, and throws an
	 * InterruptedException. With blocking I/O, (i.e. anything that doesn't use
	 * the nio packages) a thread can be interrupted, but the I/O will still
	 * proceed, and the InterruptedException is only thrown when a call to
	 * Thread.sleep() or Object.wait() is made. This method is recommended to be
	 * called after any blocking I/O calls.
	 */
	public static void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException("Thread has been interrupted");
	}

	/**
	 * This performs the same task as checkInterrupted, but throws an
	 * InterruptedIOException instead.
	 */
	public static void checkIoInterrupted() throws InterruptedIOException {
		if (Thread.interrupted()) throw new InterruptedIOException("Thread has been interrupted");
	}

	/**
	 * Returns the set of relative file paths under a given directory.
	 */
	public static List<String> getFilenames(File dir) {
		return getFilenames(dir, null);
	}

	/**
	 * Returns the set of relative file paths under a given directory. A null
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
	 * Returns the set of file paths under a given directory. A null filter
	 * matches all files.
	 */
	public static List<File> getFiles(File dir, FileFilter filter) {
		List<File> list = new ArrayList<>();
		addFiles(list, dir, filter);
		return list;
	}

	/**
	 * Joins two '/' based paths together, making sure only one '/' is between
	 * the two paths.
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
	 * Returns the file path relative to a given dir. Or the returns the file if
	 * not relative.
	 */
	public static String getRelativePath(File dir, File file) {
		String filename = getAbsolutePath(file);
		String backPath = "";
		while (dir != null) {
			String dirname = getAbsolutePath(dir) + "/";
			if (filename.startsWith(dirname)) return backPath +
				filename.substring(dirname.length());
			backPath += "../";
			dir = dir.getParentFile();
		}
		return filename;
	}

	/**
	 * Returns the absolute path of a file/dir with '/' separator.
	 */
	public static String getAbsolutePath(File file) {
		return file.getAbsolutePath().replace(File.separator, "/");
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
	 * Copies file contents from one file to another, creating the destination
	 * directories if necessary.
	 */
	public static void copyFile(File src, File dest) throws IOException {
		FileTracker tracker = new FileTracker().file(dest); // creates parent dirs
		InputStream in = new BufferedInputStream(new FileInputStream(src));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
		try {
			transferContent(in, out, 0);
		} catch (IOException e) {
			tracker.delete();
		} finally {
			close(out);
			close(in);
		}
	}

	/**
	 * Gets content from a file as a byte array.
	 */
	public static byte[] getContent(File file) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return getContent(in, 0);
		} finally {
			close(in);
		}
	}

	public static String getContentString(File file) throws IOException {
		byte[] bytes = getContent(file);
		return new String(bytes).intern();
	}

	/**
	 * Gets content from input stream as a string. Use 0 for default buffer
	 * size.
	 */
	public static String getContentString(InputStream in, int bufferSize) throws IOException {
		byte[] bytes = getContent(in, bufferSize);
		return new String(bytes).intern();
	}

	/**
	 * Gets content from an input stream as byte array. Use 0 for default buffer
	 * size.
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
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			transferContent(in, out, 0);
		} finally {
			close(out);
		}
	}

	/**
	 * Writes byte array content to a file.
	 */
	public static void setContent(File file, byte[] content) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			out.write(content);
			out.flush();
		} finally {
			close(out);
		}
	}

	/**
	 * Writes a string to a file using default encoding.
	 */
	public static void setContentString(File file, String content) throws IOException {
		setContent(file, content.getBytes());
	}

	/**
	 * Transfers content from an input stream to an output stream, using the
	 * specified buffer size, or 0 for default buffer size.
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
	 * Attempts to fill given buffer by reading from the input stream. Returns
	 * the last position filled in the buffer. Returns 0 if end of stream.
	 */
	public static int fillBuffer(InputStream in, byte[] buffer) throws IOException {
		return fillBuffer(in, buffer, 0, buffer.length);
	}

	/**
	 * Attempts to fill given buffer by reading from the input stream until full.
	 * Returns the last position filled in the buffer. Returns 0 if end of stream.
	 */
	public static int fillBuffer(InputStream in, byte[] buffer, int offset, int len)
		throws IOException {
		if (offset + len > buffer.length) throw new IllegalArgumentException(
			"Offset plus length must not exceed buffer size (" + buffer.length + "): " + offset +
				", " + len);
		int pos = offset;
		while (pos < offset + len) {
			int count = in.read(buffer, pos, offset + len - pos);
			if (count == -1) break;
			pos += count;
		}
		return pos;
	}

	/**
	 * Gets resource as a string.
	 */
	public static String getResourceString(Class<?> cls, String resourceName) throws IOException {
		return new String(getResource(cls, resourceName)).intern();
	}

	/**
	 * Gets resource from same package as class. Use 0 for default buffer size.
	 */
	public static byte[] getResource(Class<?> cls, String resourceName) throws IOException {
		InputStream in = cls.getResourceAsStream(resourceName);
		try {
			return IoUtil.getContent(in, 0);
		} finally {
			close(in);
		}
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
