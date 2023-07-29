package ceri.log.util;

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
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.AssertUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.text.StringUtil;
import ceri.common.util.CloseableUtil;

/**
 * Used to capture logging output to a file, and check the contents. Can override an existing class
 * logger, and revert
 */
public class TestLog implements AutoCloseable {
	private static final String PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36}:%L - %m%n";
	private final FileTestHelper helper;
	private final Path logFile;
	private final AutoCloseable loggerOverride;
	private final Logger logger;

	public static void main(String[] args) {
		testLogUtil(1);
		try (var testLog0 = TestLog.of(LogUtil.class)) {
			testLogUtil(2);
			try (var testLog1 = TestLog.of(LogUtil.class)) {
				testLogUtil(3);
				System.out.println(StringUtil.prefixLines("[1] ", testLog1.read()));
			}
			testLogUtil(4);
			System.out.println(StringUtil.prefixLines("[0] ", testLog0.read()));
		}
		testLogUtil(5);

	}

	private static void testLogUtil(int i) {
		LogUtil.close((AutoCloseable) () -> {
			throw new IOException("" + i);
		});
	}

	public static TestLog of() {
		return of(TestLog.class);
	}

	public static TestLog of(Class<?> cls) {
		return of(LogUtil.loggerName(cls), cls.getSimpleName() + ".log");
	}

	public static TestLog of(String loggerName, String logFileName) {
		return new TestLog(loggerName, logFileName);
	}

	private TestLog(String loggerName, String logFileName) {
		try {
			helper = FileTestHelper.builder().file(logFileName, "").build(); // tidies up on error
			logFile = helper.path(logFileName);
			loggerOverride = overrideLoggerConfig(loggerName, logFile);
			logger = LogManager.getLogger(loggerName);
		} catch (Exception e) {
			close();
			throw new AssertionError("Unexpected error", e);
		}
	}

	public Logger logger() {
		return logger;
	}

	public void assertEmpty() {
		AssertUtil.assertEquals(extract(), "");
	}

	public void assertFind(String pattern, Object... args) {
		AssertUtil.assertFind(extract(), pattern, args);
	}

	public void assertMatch(String pattern, Object... args) {
		AssertUtil.assertMatch(extract(), pattern, args);
	}

	public String extract() {
		String read = read();
		clear();
		return read;
	}

	public String read() {
		try {
			return Files.readString(logFile);
		} catch (Exception e) {
			throw new AssertionError("Unexpected error", e);
		}
	}

	public String[] lines() {
		return StringUtil.NEWLINE_REGEX.split(read());
	}

	public void clear() {
		try {
			boolean interrupted = Thread.interrupted();
			FileChannel.open(logFile, StandardOpenOption.WRITE).truncate(0).close();
			if (interrupted) ConcurrentUtil.interrupt();
		} catch (Exception e) {
			throw new AssertionError("Unexpected error", e);
		}
	}

	@Override
	public void close() {
		CloseableUtil.close(loggerOverride, helper);
	}

	@SuppressWarnings("resource")
	private AutoCloseable overrideLoggerConfig(String name, Path file) {
		var context = LoggerContext.getContext(false);
		var config = context.getConfiguration();
		var appender = createFileAppender(config, file);
		var loggerConfig = createLoggerConfig(config, name, appender);
		var oldLoggerConfig = setLoggerConfig(context, name, loggerConfig);
		return () -> setLoggerConfig(context, name, oldLoggerConfig);
	}

	private LoggerConfig setLoggerConfig(LoggerContext context, String name,
		LoggerConfig loggerConfig) {
		var config = context.getConfiguration();
		var old = config.getLoggerConfig(name);
		if (old != null) config.removeLogger(name);
		if (loggerConfig != null) config.addLogger(name, loggerConfig);
		context.updateLoggers();
		return old;
	}

	private LoggerConfig createLoggerConfig(Configuration config, String name, Appender appender) {
		AppenderRef ref = AppenderRef.createAppenderRef(appender.getName(), null, null);
		LoggerConfig loggerConfig = LoggerConfig.newBuilder().withAdditivity(false)
			.withLevel(Level.ALL).withLoggerName(name).withIncludeLocation(Boolean.TRUE.toString())
			.withRefs(new AppenderRef[] { ref }).withConfig(config).build();
		loggerConfig.addAppender(appender, null, null);
		return loggerConfig;
	}

	private Appender createFileAppender(Configuration config, Path file) {
		String name = file.getFileName().toString();
		PatternLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
		FileAppender appender =
			FileAppender.newBuilder().withFileName(file.toString()).setIgnoreExceptions(false)
				.setName(name).setLayout(layout).setConfiguration(config).build();
		appender.start();
		return appender;
	}
}
