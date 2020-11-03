package ceri.common.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import ceri.common.io.IoUtil;
import ceri.common.text.StringUtil;

public class ProcessUtil {
	private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"");
	private static final Process NULL_PROCESS = NullProcess.of();

	private ProcessUtil() {}

	public static Process nullProcess() {
		return NULL_PROCESS;
	}

	@SuppressWarnings("resource")
	public static String stdOut(Process process) throws IOException {
		return IoUtil.availableString(process.getInputStream());
	}

	@SuppressWarnings("resource")
	public static String stdErr(Process process) throws IOException {
		return IoUtil.availableString(process.getErrorStream());
	}

	public static String toString(ProcessBuilder builder) {
		return toString(builder.command());
	}

	public static String toString(List<String> command) {
		return command.stream().map(ProcessUtil::quoteWhiteSpace).collect(Collectors.joining(" "));
	}

	private static String quoteWhiteSpace(String s) {
		if (!StringUtil.WHITE_SPACE_REGEX.matcher(s).find()) return s;
		return "\"" + DOUBLE_QUOTES.matcher(s).replaceAll("\\\"") + "\"";
	}
}
