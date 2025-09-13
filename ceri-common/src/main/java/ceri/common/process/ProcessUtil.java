package ceri.common.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.io.IoUtil;
import ceri.common.text.Joiner;
import ceri.common.text.Regex;

public class ProcessUtil {
	private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"");
	private static final Process NULL_PROCESS = NullProcess.of();

	private ProcessUtil() {}

	public static Process nullProcess() {
		return NULL_PROCESS;
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
		return Joiner.SPACE.join(ProcessUtil::quoteWhiteSpace, command);
	}

	private static String quoteWhiteSpace(String s) {
		if (!Regex.SPACE.matcher(s).find()) return s;
		return "\"" + DOUBLE_QUOTES.matcher(s).replaceAll("\\\"") + "\"";
	}
}
