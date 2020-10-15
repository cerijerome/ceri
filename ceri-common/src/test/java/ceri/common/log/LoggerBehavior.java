package ceri.common.log;

import static ceri.common.log.Logger.FormatFlag.abbreviatePackage;
import static ceri.common.log.Logger.FormatFlag.noDate;
import static ceri.common.log.Logger.FormatFlag.noStackTrace;
import static ceri.common.log.Logger.FormatFlag.noThread;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertValue;
import static org.hamcrest.CoreMatchers.is;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.test.Capturer;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class LoggerBehavior {
	private static final Object KEY = "test";
	private static final SystemIo stdIo = SystemIo.of();
	private static final StringBuilder out = new StringBuilder();
	private static final StringBuilder err = new StringBuilder();

	@SuppressWarnings("resource")
	@BeforeClass
	public static void beforeClass() {
		stdIo.out(StringUtil.asPrintStream(out));
		stdIo.err(StringUtil.asPrintStream(err));
	}

	@AfterClass
	public static void afterClass() {
		stdIo.close();
	}

	@Before
	public void before() {
		out.setLength(0);
		err.setLength(0);
	}

	@After
	public void after() {
		Logger.removeLogger(KEY);
	}

	@Test
	public void shouldLogToStdIo() {
		Logger logger = Logger.logger();
		logger.error("test: %s", "error");
		assertAndReset(err, s -> s.endsWith("test: error\n"));
	}

	@Test
	public void shouldAllowOutputsToBeSet() {
		Capturer<String> out = Capturer.of();
		Capturer<String> err = Capturer.of();
		Logger logger = Logger.builder(KEY).out(out).err(err).build();
		logger.info("test: %s", "info");
		logger.error("test: %s", "error");
		assertThat(out.values.size(), is(1));
		assertValue(out.values.get(0), s -> s.endsWith("test: info"));
		assertThat(err.values.size(), is(1));
		assertValue(err.values.get(0), s -> s.endsWith("test: error"));
	}

	@Test
	public void shouldAllowNullOutputs() {
		Consumer<String> NULL = null;
		Logger logger = Logger.builder(KEY).out(NULL).err(NULL).build();
		logger.error("test: %s", "error");
		logger.log(Level.ALL, "test: %s", "all");
		assertAndReset(out, String::isEmpty);
		assertAndReset(err, String::isEmpty);
	}

	@Test
	public void shouldLogLevels() {
		Logger logger = Logger.builder(KEY).minLevel(Level.ALL).build();
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
		Logger logger = Logger.builder(KEY).minLevel(Level.DEBUG).errLevel(Level.INFO).build();
		logger.debug("test: %s", "debug");
		assertAndReset(out, s -> s.endsWith("test: debug\n"));
		logger.info("test: %s", "info");
		assertAndReset(err, s -> s.endsWith("test: info\n"));
	}

	@Test
	public void shouldLogClassAndLineNumber() {
		Logger logger = Logger.builder(KEY).build();
		logger.info("test: %s", "info");
		assertAndReset(out, RegexUtil.finder("%s:\\d+", getClass().getName()));
	}

	@Test
	public void shouldAllowFormatToBeSet() {
		Logger logger = Logger.builder(KEY).format("[%5$s]").build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.equals("[test: info]\n"));
	}

	@Test
	public void shouldOptimizeOutputWithFlags() {
		Logger logger = Logger.builder(KEY).flags(noDate, noThread, noStackTrace).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.equals("1970-01-01 00:00:00.000 [] INFO   - test: info\n"));
	}

	@Test
	public void shouldTruncateThreadName() {
		Logger logger = Logger.builder(KEY).threadMax(0).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.contains("[]"));
		logger = Logger.builder(KEY).threadMax(1).build();
		logger.info("test: %s", "info");
		assertAndReset(out, RegexUtil.finder("\\[.\\]"));
		logger = Logger.builder(KEY).threadMax(Integer.MAX_VALUE).build();
		logger.info("test: %s", "info");
		assertAndReset(out, RegexUtil.finder("\\[.+\\]"));
	}

	@Test
	public void shouldAbbreviatePackageNames() {
		Logger logger = Logger.builder(KEY).flags(abbreviatePackage).build();
		logger.info("test: %s", "info");
		assertAndReset(out, s -> s.contains(" c.c.l.LoggerBehavior:"));
	}

	@Test
	public void shouldFindLoggerByKey() {
		Logger logger = Logger.builder(KEY).build();
		assertThat(Logger.logger(KEY), is(logger));
		logger = Logger.builder(KEY).build();
		assertThat(Logger.logger(KEY), is(logger));
	}

	private void assertAndReset(StringBuilder b, Predicate<String> test) {
		assertValue(b.toString(), test::test);
		b.setLength(0);
	}

}
