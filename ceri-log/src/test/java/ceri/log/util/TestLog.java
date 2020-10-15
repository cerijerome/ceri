package ceri.log.util;

import static ceri.common.test.TestUtil.assertThat;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;
import ceri.common.text.StringUtil;

/**
 * Used to capture logging output to a file, and check the contents.
 */
public class TestLog implements Closeable {
	private static final String loggerName = LogUtil.loggerName(TestLog.class);
	private static final String TEST_LOG = TestLog.class.getSimpleName() + ".log";
	private static final String PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36}:%L - %m%n";
	private final FileTestHelper helper;
	private final Path logFile;
	private final Logger logger;

	public static TestLog of() {
		return of(TEST_LOG);
	}

	public static TestLog of(String path) {
		return new TestLog(path);
	}

	private TestLog(String path) {
		try {
			helper = FileTestHelper.builder().file(path, "").build(); // tidies up on error
			logFile = helper.path(path);
			logger = createLogger(path, logFile);
		} catch (RuntimeException | IOException e) {
			throw new AssertionError("Unexpected error", e);
		}
	}

	public Logger logger() {
		return logger;
	}

	public void assertEmpty() {
		assertThat(extract(), is(""));
	}

	public void assertFind(String pattern, Object... args) {
		TestUtil.assertFind(extract(), pattern, args);
	}

	public void assertMatch(String pattern, Object... args) {
		TestUtil.assertMatch(extract(), pattern, args);
	}

	public String extract() {
		String read = read();
		clear();
		return read;
	}

	public String read() {
		try {
			return Files.readString(logFile);
		} catch (IOException e) {
			throw new AssertionError("Unexpected error", e);
		}
	}

	public String[] lines() {
		return StringUtil.NEWLINE_REGEX.split(read());
	}

	public void clear() {
		try {
			FileChannel.open(logFile, StandardOpenOption.WRITE).truncate(0).close();
		} catch (IOException e) {
			throw new AssertionError("Unexpected error", e);
		}
	}

	@Override
	public void close() {
		removeLogger(loggerName);
		helper.close();
	}

	@SuppressWarnings("resource")
	private Logger createLogger(String appenderName, Path logFile) {
		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();
		Appender appender = createAppender(config, appenderName, logFile);
		addLogger(config, appender);
		context.updateLoggers();
		return LogManager.getLogger(loggerName);
	}

	private Appender createAppender(Configuration config, String name, Path logFile) {
		PatternLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
		FileAppender appender =
			FileAppender.newBuilder().withFileName(logFile.toString()).setIgnoreExceptions(false)
				.setName(name).setLayout(layout).setConfiguration(config).build();
		appender.start();
		return appender;
	}

	private void addLogger(Configuration config, Appender appender) {
		config.addAppender(appender);
		AppenderRef ref = AppenderRef.createAppenderRef(appender.getName(), null, null);
		AppenderRef[] refs = new AppenderRef[] { ref };
		LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, loggerName,
			TRUE.toString(), refs, null, config, null);
		loggerConfig.addAppender(appender, null, null);
		config.addLogger(loggerName, loggerConfig);
	}

	private void removeLogger(String loggerName) {
		@SuppressWarnings("resource")
		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();
		config.removeLogger(loggerName);
		context.updateLoggers();
	}

}
