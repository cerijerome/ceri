package ceri.log.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import ceri.common.io.IoUtil;
import ceri.common.text.StringUtil;
import ceri.log.util.LogUtil;

public class ProcessUtil {
	private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"");
	
	private ProcessUtil() {}

	public static String stdOut(Process process) throws IOException {
		return IoUtil.readAvailableString(process.getInputStream());
	}

	public static String stdErr(Process process) throws IOException {
		return IoUtil.readAvailableString(process.getErrorStream());
	}

	public static Object logToString(ProcessBuilder builder) {
		return LogUtil.toString(() -> ProcessUtil.toString(builder));
	}

	public static String toString(ProcessBuilder builder) {
		return toString(builder.command());
	}

	public static String toString(Parameters params) {
		return toString(params.list());
	}

	public static String toString(List<String> command) {
		return command.stream().map(ProcessUtil::quoteWhiteSpace).collect(Collectors.joining(" "));
	}

	private static String quoteWhiteSpace(String s) {
		if (!StringUtil.WHITE_SPACE_REGEX.matcher(s).find()) return s;
		return "\"" + DOUBLE_QUOTES.matcher(s).replaceAll("\\\"") + "\"";
	}

}
