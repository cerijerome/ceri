package ceri.log.io;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.StringPrintStream;
import ceri.common.text.StringUtil;

public class LogPrintStream extends StringPrintStream {
	private static final Logger defaultLogger = LogManager.getLogger();
	private final Logger logger;
	private final Level level;

	public static LogPrintStream of() {
		return of(Level.DEBUG);
	}

	public static LogPrintStream of(Level level) {
		return of(level, defaultLogger);
	}

	public static LogPrintStream of(Level level, Logger logger) {
		return new LogPrintStream(level, logger);
	}

	private LogPrintStream(Level level, Logger logger) {
		this.level = level;
		this.logger = logger;
	}

	@Override
	public void flush() {
		super.flush();
		log();
	}

	public void log() {
		String[] lines = StringUtil.NEWLINE_REGEX.split(toString());
		clear();
		for (String line : lines)
			logger.log(level, StringUtil.compact(line));
	}

}