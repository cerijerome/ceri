package ceri.log.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.findsRegex;
import static ceri.common.test.TestUtil.matchesRegex;
import static ceri.common.test.TestUtil.throwIt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;
import ceri.common.util.StartupValues;
import ceri.log.io.LogPrintStream;

public class LogUtilTest {
	private static final Object OBJ = new Object() {};
	private static TestLog testLog;
	private static Logger logger;

	@BeforeClass
	public static void beforeClass() {
		testLog = TestLog.of();
		logger = testLog.logger();
	}

	@AfterClass
	public static void afterClass() {
		testLog.close();
	}

	@Before
	public void before() {
		testLog.clear();
	}

	@Test
	public void testLoggerName() {
		assertThat(LogUtil.loggerName(getClass()), is(getClass().getName()));
		assertThat(LogUtil.loggerName(OBJ.getClass()), is(getClass().getName() + "$1"));
	}

	@Test
	public void testStartupValues() {
		StartupValues values = LogUtil.startupValues("abc", "123");
		assertThat(values.next().get(), is("abc"));
		assertThat(values.next().asInt(), is(123));
		values = LogUtil.startupValues(logger, "abc", "123");
		assertThat(values.next("val1").get(), is("abc"));
		testLog.assertFindRegex("val1 = abc \\(from args\\[0\\]\\)");
		assertThat(values.next("val2").asInt(), is(123));
		testLog.assertFindRegex("val2 = 123 \\(from args\\[1\\]\\)");
	}

	@Test
	public void testBinaryLogger() {
		BinaryPrinter bp = LogUtil.binaryLogger();
		bp.print(1, 2, 3);
		try (var stream = LogPrintStream.of(logger)) {
			bp = LogUtil.binaryLogger(BinaryPrinter.ASCII, stream);
			bp.print("abc").flush();
			testLog.assertFindRegex("DEBUG .* 61 62 63 abc");
		}
	}

	@Test
	public void testExecute() {
		assertThat(LogUtil.execute(null, () -> {}), is(true));
		assertThat(LogUtil.execute(logger, () -> {}), is(true));
		assertThat(LogUtil.execute(logger, null), is(false));
		testLog.assertEmpty();
	}

	@Test
	public void testExecuteWithException() {
		assertThat(LogUtil.execute(null, () -> throwIt(new RuntimeException("rtx"))), is(false));
		assertThat(LogUtil.execute(logger, () -> throwIt(new RuntimeException("rtx"))), is(false));
		testLog.assertFindRegex("(?is)ERROR .* catching.*RuntimeException.*rtx");
		assertThat(LogUtil.execute(null, () -> throwIt(new InterruptedException())), is(false));
		assertThat(LogUtil.execute(logger, () -> throwIt(new InterruptedException())), is(false));
		testLog.assertFindRegex("(?is)INFO .*InterruptedException");
	}

	@Test
	public void testSubmit() throws InterruptedException, ExecutionException {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		LogUtil.submit(logger, exec, () -> {}).get();
		LogUtil.submit(logger, exec, () -> throwIt(new IOException("iox"))).get();
		testLog.assertFindRegex("(?is)ERROR .* catching.*IOException.*iox");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreate() {
		assertIterable(LogUtil.create(logger, TestCloseable::of, "1", "-1"), new TestCloseable(1),
			new TestCloseable(-1));
		assertThrown(() -> LogUtil.create(logger, TestCloseable::of, "1", "-1", "x"));
		testLog.assertFindRegex("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		assertArray(LogUtil.createArray(logger, TestCloseable[]::new, TestCloseable::of, "1", "-1"),
			new TestCloseable(1), new TestCloseable(-1));
		assertThrown(() -> LogUtil.createArray(logger, TestCloseable[]::new, TestCloseable::of, "1",
			"-1", "x"));
		testLog.assertFindRegex("(?is)WARN .* catching.*IOException.*-1");
	}

	@Test
	public void testCloseable() throws Exception {
		@SuppressWarnings("resource")
		TestCloseable tc1 = new TestCloseable(1);
		@SuppressWarnings("resource")
		TestCloseable tc2 = new TestCloseable(-1);
		try (AutoCloseable closeable = LogUtil.closeable(logger, tc1, tc2)) {}
		testLog.assertFindRegex("(?is)WARN .* catching.*IOException.*-1");
		try (AutoCloseable closeable = LogUtil.closeable(logger, List.of(tc1, tc2))) {}
		testLog.assertFindRegex("(?is)WARN .* catching.*IOException.*-1");
	}

	@Test
	public void testCloseProcess() throws InterruptedException {
		assertFalse(LogUtil.close(logger, (Process) null));
		Process process = Mockito.mock(Process.class);
		when(process.waitFor(anyLong(), any())).thenReturn(true);
		assertTrue(LogUtil.close(logger, process));
	}

	@Test
	public void testCloseProcessWithInterrupt() throws InterruptedException {
		Process process = Mockito.mock(Process.class);
		when(process.waitFor(anyLong(), any())).thenThrow(new InterruptedException());
		assertFalse(LogUtil.close(null, process));
		assertFalse(LogUtil.close(logger, process));
		testLog.assertFindRegex("(?is)INFO .*InterruptedException");
	}

	@Test
	public void testCloseExecutorService() throws InterruptedException {
		assertFalse(LogUtil.close(logger, (ExecutorService) null));
		BooleanCondition sync = BooleanCondition.of();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.execute(() -> signalAndSleep(sync, 60000));
		sync.await();
		assertTrue(LogUtil.close(logger, exec));
	}

	@Test
	public void testCloseExecutorServiceWithInterrupt() throws InterruptedException {
		ExecutorService exec = Mockito.mock(ExecutorService.class);
		when(exec.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException());
		assertFalse(LogUtil.close(null, exec));
		assertFalse(LogUtil.close(logger, exec));
		testLog.assertFindRegex("(?is)INFO .*InterruptedException");
	}

	@Test
	public void testCloseFuture() throws InterruptedException, ExecutionException {
		assertFalse(LogUtil.close(logger, (Future<?>) null));
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<?> future = exec.submit(() -> {});
		future.get();
		assertTrue(LogUtil.close(logger, future));
		LogUtil.close(logger, exec);
	}

	@Test
	public void testCloseFutureWithCancellation() throws InterruptedException {
		assertFalse(LogUtil.close(logger, (Future<?>) null));
		BooleanCondition sync = BooleanCondition.of();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<?> future = exec.submit(() -> signalAndSleep(sync, 60000));
		sync.await();
		assertTrue(LogUtil.close(logger, future));
		LogUtil.close(logger, exec);
	}

	@Test
	public void testCloseFutureWithInterrupt()
		throws InterruptedException, ExecutionException, TimeoutException {
		Future<?> future = Mockito.mock(Future.class);
		when(future.get(anyLong(), any())).thenThrow(new InterruptedException());
		assertFalse(LogUtil.close(null, future));
		assertFalse(LogUtil.close(logger, future));
		testLog.assertFindRegex("(?is)INFO .*InterruptedException");
	}

	@Test
	public void testCloseFutureWithException()
		throws InterruptedException, ExecutionException, TimeoutException {
		Future<?> future = Mockito.mock(Future.class);
		when(future.get(anyLong(), any())).thenThrow(new TimeoutException());
		assertFalse(LogUtil.close(null, future));
		assertFalse(LogUtil.close(logger, future));
		testLog.assertFindRegex("(?is)WARN .*TimeoutException");
	}

	@Test
	public void testCloseCloseables() {
		assertFalse(LogUtil.close(logger, (List<TestCloseable>) null));
	}

	@Test
	public void testFatalf() {
		LogUtil.fatalf(logger, "%d", 123);
		testLog.assertFindRegex("FATAL .* 123");
	}

	@Test
	public void testErrorf() {
		LogUtil.errorf(logger, "%d", 123);
		testLog.assertFindRegex("ERROR .* 123");
	}

	@Test
	public void testWarnf() {
		LogUtil.warnf(logger, "%d", 123);
		testLog.assertFindRegex("WARN .* 123");
	}

	@Test
	public void testInfof() {
		LogUtil.infof(logger, "%d", 123);
		testLog.assertFindRegex("INFO .* 123");
	}

	@Test
	public void testDebug() {
		LogUtil.debugf(logger, "%d", 123);
		testLog.assertFindRegex("DEBUG .* 123");
	}

	@Test
	public void testTracef() {
		LogUtil.tracef(logger, "%d", 123);
		testLog.assertFindRegex("TRACE .* 123");
	}

	@Test
	public void testLogf() {
		LogUtil.logf(logger, Level.WARN, "%d", 123);
		testLog.assertFindRegex("WARN .* 123");
	}

	@Test
	public void testToStringFunction() {
		assertThat(LogUtil.toString("test", String::toUpperCase).toString(), is("TEST"));
	}

	@Test
	public void testHashId() {
		assertThat(LogUtil.hashId(new Object()).toString(), matchesRegex("@[a-f\\d]+"));
	}

	@Test
	public void testToStringOrHash() {
		assertThat(LogUtil.toStringOrHash("test@1234").toString(), is("@1234"));
		assertThat(LogUtil.toStringOrHash(new Object()).toString(), matchesRegex("@[a-f\\d]+"));
	}

	@Test
	public void testToHex() {
		assertThat(LogUtil.toHex(1023).toString(), is("3ff"));
	}

	@Test
	public void testCompact() {
		assertThat(LogUtil.compact("a  b\n  c   ").toString(), is("a b c"));
	}

	@Test
	public void testEscaped() {
		assertThat(LogUtil.escaped("a\n\0\t").toString(), is("a\\n\\0\\t"));
	}

	@Test
	public void testIntroMessage() {
		assertThat(LogUtil.introMessage("Test"), findsRegex(" Test "));
		assertThat(LogUtil.introMessage(StringUtil.repeat("Test", 20)),
			findsRegex(" " + StringUtil.repeat("Test", 19) + " "));
	}

	private static void signalAndSleep(BooleanCondition sync, long sleepMs) {
		sync.signal();
		try {
			Thread.sleep(sleepMs);
		} catch (InterruptedException e) {
			// Stop
		}
	}

}
