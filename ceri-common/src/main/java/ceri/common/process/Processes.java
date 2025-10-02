package ceri.common.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.IoUtil;
import ceri.common.text.Joiner;
import ceri.common.text.Regex;

/**
 * Processor support.
 */
public class Processes {
	private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"");
	/** A no-op, stateless process instance. */
	public static final Process NULL = new Null();

	private Processes() {}

	/**
	 * A no-op, stateless implementation.
	 */
	public static class Null extends Process {
		protected Null() {}

		@Override
		public void destroy() {}

		@Override
		public int exitValue() {
			return 0;
		}

		@Override
		public InputStream getErrorStream() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public InputStream getInputStream() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public OutputStream getOutputStream() {
			return IoStreamUtil.nullOut;
		}

		@Override
		public int waitFor() throws InterruptedException {
			return exitValue();
		}
	}
	
	/**
	 * Reads available bytes from stream as a string.
	 */
	@SuppressWarnings("resource")
	public static String stdOut(Process process) throws IOException {
		return IoUtil.availableString(process.getInputStream());
	}

	/**
	 * Reads available bytes from stream as a string.
	 */
	@SuppressWarnings("resource")
	public static String stdErr(Process process) throws IOException {
		return IoUtil.availableString(process.getErrorStream());
	}

	public static String toString(ProcessBuilder builder) {
		return toString(builder.command());
	}

	public static String toString(List<String> command) {
		return Joiner.SPACE.join(Processes::quoteSpace, command);
	}

	private static String quoteSpace(String s) {
		if (!Regex.SPACE.matcher(s).find()) return s;
		return "\"" + DOUBLE_QUOTES.matcher(s).replaceAll("\\\"") + "\"";
	}
}
