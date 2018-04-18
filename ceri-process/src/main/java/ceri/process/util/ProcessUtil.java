package ceri.process.util;

import java.io.IOException;
import java.io.InputStream;
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
		try (InputStream stdOut = process.getInputStream()) {
			return IoUtil.getContentString(stdOut, 0);
		}
	}

	public static String stdErr(Process process) throws IOException {
		try (InputStream stdOut = process.getErrorStream()) {
			return IoUtil.getContentString(stdOut, 0);
		}
	}

	public static Object logToString(ProcessBuilder builder) {
		return LogUtil.toString(() -> ProcessUtil.toString(builder));
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
