package ceri.common.log;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.SystemIo;
import ceri.common.log.Logger.FormatFlag;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.text.Regex;
import ceri.common.text.StringBuilders;

public class LoggerBehavior {
	private static final Object KEY = "test";
	private Logger logger;
	private StringBuilder out;
	private StringBuilder err;
	private SystemIo sysIo;

	@After
	public void after() {
		Logger.removeLogger(KEY);
		logger = null;
		Closeables.close(sysIo);
		sysIo = null;
		out = null;
		err = null;
	}

	@Test
	public void shouldLogToStdIo() {
		init();
		logger = Logger.logger();
		logger.error("test: %s", "error");
		assertAndReset(err, s -> s.endsWith("test: error\n"));
	}

	@Test
	public void shouldAllowOutputsToBeSet() {
		var out = Captor.<String>of();
		var err = Captor.<String>of();
		logger = builder().out(out).err(err).build();
		logger.info("test: %s", "info");
		logger.error("test: %s", "error");
		Assert.equal(out.values.size(), 1);
		Assert.match(out.values.get(0), ".*test: info");
		Assert.equal(err.values.size(), 1);
		Assert.match(err.values.get(0), ".*test: error");
	}

	@Test
	public void shouldAllowNullOutputs() {
		Functions.Consumer<String> NULL = null;
		logger = init().out(NULL).err(NULL).build();
		logger.error("test: %s", "error");
		logger.log(Level.ALL, "test: %s", "all");
		assertAndReset(out, String::isEmpty);
		assertAndReset(err, String::isEmpty);
	}

	@Test
	public void shouldLogLevels() {
		logger = init().minLevel(Level.ALL).build();
		logger.trace("test: %s", "trace");
		assertAndReset(out, s -> s.endsWith("test: trace\n"));
		logger.debug("test: %s", "debug");
		assertAndReset(out, s -> s.endsWith("test: debug\n"));
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.endsWith("test: info\n"));
		logger.warn("test: %s", "warn");
		assertAndReset(out, s -> s.endsWith("test: warn\n"));
		logger.error("test: %s", "error");
		assertAndReset(err, s -> s.endsWith("test: error\n"));
		logger.catching(new Exception("test-exception"));
		assertAndReset(err, s -> s.contains("test-exception"));
		logger.log(Level.WARN, new Exception("test-exception"));
		assertAndReset(out, s -> s.contains("test-exception"));
		logger.log(Level.ALL, "test: %s", "all");
		assertAndReset(out, s -> s.endsWith("test: all\n"));
		logger.log(Level.NONE, "test: %s", "none");
		assertAndReset(out, String::isEmpty);
	}

	@Test
	public void shouldAllowSeparateErrorLog() {
		logger = init().minLevel(Level.DEBUG).errLevel(Level.INFO).build();
		logger.debug("test: %s", "debug");
		assertAndReset(out, s -> s.endsWith("test: debug\n"));
		logger.info("test: %s", "info");
		assertAndReset(err, s -> s.endsWith("test: info\n"));
	}

	@Test
	public void shouldLogClassAndLineNumber() {
		logger = init().build();
		logger.info("test: %s", "info");
		assertAndReset(out, Regex.Filter.find("%s:\\d+", getClass().getName()));
	}

	@Test
	public void shouldAllowFormatToBeSet() {
		logger = init().format("[%5$s]").build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.equals("[test: info]\n"));
	}

	@Test
	public void shouldOptimizeOutputWithFlags() {
		logger =
			init().flags(FormatFlag.noDate, FormatFlag.noThread, FormatFlag.noStackTrace).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.equals("1970-01-01 00:00:00.000 [] INFO   - test: info\n"));
	}

	@Test
	public void shouldTruncateThreadName() {
		logger = init().threadMax(0).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.contains("[]"));
		logger = Logger.builder(KEY).threadMax(1).build();
		logger.info("test: %s", "info");
		assertAndReset(out, Regex.Filter.find("\\[.\\]"));
		logger = Logger.builder(KEY).threadMax(Integer.MAX_VALUE).build();
		logger.info("test: %s", "info");
		assertAndReset(out, Regex.Filter.find("\\[.+\\]"));
	}

	@Test
	public void shouldAbbreviatePackageNames() {
		logger = init().flags(FormatFlag.abbreviatePackage).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.contains(" c.c.l.LoggerBehavior:"));
	}

	@Test
	public void shouldFindLoggerByKey() {
		logger = init().build();
		Assert.equal(Logger.logger(KEY), logger);
		logger = builder().build();
		Assert.equal(Logger.logger(KEY), logger);
	}

	private Logger.Builder builder() {
		return Logger.builder(KEY);
	}

	@SuppressWarnings("resource")
	private Logger.Builder init() {
		sysIo = SystemIo.of();
		out = new StringBuilder();
		err = new StringBuilder();
		sysIo.out(StringBuilders.printStream(out));
		sysIo.err(StringBuilders.printStream(err));
		return builder();
	}

	private void assertAndReset(StringBuilder b, Excepts.Predicate<RuntimeException, String> test) {
		var s = StringBuilders.flush(b);
		if (!test.test(s)) throw Assert.failure("Failed: %s", String.valueOf(s).trim());
	}
}
