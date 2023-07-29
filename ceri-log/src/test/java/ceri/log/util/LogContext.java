package ceri.log.util;

import static java.lang.Boolean.TRUE;
import java.nio.file.Path;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Used to create, add, and remove loggers.
 */
public class LogContext {
	private static final String PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36}:%L - %m%n";
	private final LoggerContext context;
	private final Configuration config;

	public static LogContext of(boolean current) {
		return new LogContext(current);
	}

	private LogContext(boolean current) {
		context = LoggerContext.getContext(current);
		config = context.getConfiguration();
	}

	public Appender getAppender(String appenderName) {
		return config.getAppender(appenderName);
	}

	public Appender setAppender(Appender appender) {
		var old = removeAppender(appender.getName());
		config.addAppender(appender);
		return old;
	}

	public Appender removeAppender(String appenderName) {
		var appender = getAppender(appenderName);
		if (appender != null && config instanceof AbstractConfiguration c)
			c.removeAppender(appenderName);
		return appender;
	}

	public LoggerConfig getLoggerConfig(String loggerName) {
		return config.getLoggerConfig(loggerName);
	}

	public LoggerConfig setLoggerConfig(LoggerConfig loggerConfig) {
		var old = removeLoggerConfig(loggerConfig.getName());
		config.addLogger(loggerConfig.getName(), loggerConfig);
		return old;
	}

	public LoggerConfig removeLoggerConfig(String loggerName) {
		var loggerConfig = config.getLoggerConfig(loggerName);
		if (loggerConfig != null) config.removeLogger(loggerName);
		return loggerConfig;
	}

	public void updateLoggers() {
		context.updateLoggers();
	}

	public Appender createFileAppender(Path logFile) {
		return createFileAppender(logFile, logFile.getFileName().toString(), PATTERN);
	}

	public Appender createFileAppender(Path logFile, String appenderName, String pattern) {
		PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
		FileAppender appender =
			FileAppender.newBuilder().withFileName(logFile.toString()).setIgnoreExceptions(false)
				.setName(appenderName).setLayout(layout).setConfiguration(config).build();
		appender.start();
		return appender;
	}

	public LoggerConfig createLoggerConfig(String loggerName, Appender appender) {
		AppenderRef ref = AppenderRef.createAppenderRef(appender.getName(), null, null);
		LoggerConfig loggerConfig = LoggerConfig.newBuilder().withAdditivity(false)
			.withLevel(Level.ALL).withLoggerName(loggerName).withIncludeLocation(TRUE.toString())
			.withRefs(new AppenderRef[] { ref }).withConfig(config).build();
		loggerConfig.addAppender(appender, null, null);
		return loggerConfig;
	}
}
