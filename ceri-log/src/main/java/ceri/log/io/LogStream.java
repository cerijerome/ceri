package ceri.log.io;

import java.nio.charset.Charset;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.StringStream;
import ceri.common.text.Regex;
import ceri.common.text.Strings;

/**
 * Collects output, writes compacted lines to logger on flush().
 */
public class LogStream extends StringStream {
	private static final Logger defaultLogger = LogManager.getLogger();
	private final Logger logger;
	private Level level;

	public static LogStream of() {
		return of(Level.DEBUG);
	}

	public static LogStream of(Level level) {
		return of(defaultLogger, level);
	}

	public static LogStream of(Logger logger) {
		return of(logger, Level.DEBUG);
	}

	public static LogStream of(Logger logger, Level level) {
		return new LogStream(level, logger);
	}

	private LogStream(Level level, Logger logger) {
		super(Charset.defaultCharset());
		this.level = level;
		this.logger = logger;
	}

	public void level(Level level) {
		flush();
		this.level = level;
	}

	@Override
	public void flush() {
		super.flush();
		log();
	}

	public void log() {
		var lines = Regex.Split.LINE.array(toString());
		clear();
		for (String line : lines)
			logger.log(level, Strings.compact(line));
	}
}
