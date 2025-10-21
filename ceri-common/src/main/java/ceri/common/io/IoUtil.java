package ceri.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.common.concurrent.Concurrent;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.function.Excepts;
import ceri.common.stream.Stream;
import ceri.common.text.Strings;

/**
 * I/O utility functions.
 */
public class IoUtil {
	private static final int READ_POLL_MS = 20;
	private static final int SKIP_BUFFER_SIZE = 64;
	private static final int BUFFER_SIZE_DEF = 1024;
	public static final ByteProvider EOL_BYTES = ByteProvider.of(Strings.EOL.getBytes());

	private IoUtil() {}

	/**
	 * System line separator as encoded bytes.
	 */
	public static ByteProvider eolBytes(Charset charset) {
		if (charset == null || Charset.defaultCharset().equals(charset)) return EOL_BYTES;
		return ByteProvider.of(Strings.EOL.getBytes(charset));
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
	 * Reads single bytes without blocking, stopping if no bytes are available, or a line separator
	 * is found. The returned string contains the line separator if found.
	 */
	public static String availableLine(InputStream in) throws IOException {
		return availableLine(in, Charset.defaultCharset());
	}

	/**
	 * Reads single bytes without blocking, stopping if no bytes are available, or a line separator
	 * is found. The returned string contains the line separator if found.
	 */
	public static String availableLine(InputStream in, Charset charset) throws IOException {
		if (in == null) return null;
		var eol = eolBytes(charset);
		return availableBytes(in, (b, n) -> eol.isEqualTo(0, b, n - eol.length(), eol.length()))
			.getString(0, charset);
	}

	/**
	 * Reads single bytes without blocking, stopping if no bytes are available, or the predicate
	 * tests true. The predicate receives the byte array buffer and the number of bytes filled.
	 */
	public static <E extends Exception> ByteProvider availableBytes(InputStream in,
		Excepts.ObjIntPredicate<E, byte[]> predicate) throws E, IOException {
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
		return readNextString(in, StandardCharsets.UTF_8);
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
			if (timeoutMs != 0 && (System.currentTimeMillis() > t)) throw new IoExceptions.Timeout(
				"Bytes not available within " + timeoutMs + "ms: " + n + "/" + count);
			Concurrent.delay(pollMs);
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
			Concurrent.checkRuntimeInterrupted();
			int n = in.read(buffer);
			if (n < 0) break;
			if (n > 0) out.write(buffer, 0, n);
			else Concurrent.delay(delayMs);
			total += n;
		}
		return total;
	}

	/**
	 * Gets content from input stream as a string.
	 */
	public static String readString(InputStream in) throws IOException {
		return readString(in, StandardCharsets.UTF_8);
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
	public static Stream<IOException, String> lines(InputStream in) {
		return lines(in, StandardCharsets.UTF_8);
	}

	/**
	 * Returns a stream of lines lazily read from input stream.
	 */
	public static Stream<IOException, String> lines(InputStream in, Charset charset) {
		return Stream.from(new BufferedReader(new InputStreamReader(in, charset)).lines());
	}
}
