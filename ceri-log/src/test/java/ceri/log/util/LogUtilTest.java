package ceri.log.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.ErrorGen.INX;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
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
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestProcess;
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
		assertEquals(LogUtil.loggerName(getClass()), getClass().getName());
		assertEquals(LogUtil.loggerName(OBJ.getClass()), getClass().getName() + "$1");
	}

	@Test
	public void testStartupValues() {
		StartupValues values = LogUtil.startupValues("abc", "123");
		assertEquals(values.next().get(), "abc");
		assertEquals(values.next().asInt(), 123);
		values = LogUtil.startupValues(logger, "abc", "123");
		assertEquals(values.next("val1").get(), "abc");
		testLog.assertFind("val1 = abc \\(from args\\[0\\]\\)");
		assertEquals(values.next("val2").asInt(), 123);
		testLog.assertFind("val2 = 123 \\(from args\\[1\\]\\)");
	}

	@Test
	public void testBinaryLogger() {
		BinaryPrinter bp = LogUtil.binaryLogger();
		bp.print(1, 2, 3);
		try (var stream = LogPrintStream.of(logger)) {
			bp = LogUtil.binaryLogger(BinaryPrinter.ASCII, stream);
			bp.print("abc").flush();
			testLog.assertFind("DEBUG .* 61 62 63 abc");
		}
	}

	@Test
	public void testExecute() {
		assertTrue(LogUtil.execute(null, () -> {}));
		assertTrue(LogUtil.execute(logger, () -> {}));
		assertFalse(LogUtil.execute(logger, null));
		testLog.assertEmpty();
	}

	@Test
	public void testExecuteWithException() {
		assertThrown(RuntimeInterruptedException.class,
			() -> LogUtil.execute(null, () -> throwIt(new InterruptedException())));
		assertThrown(RuntimeInterruptedException.class,
			() -> LogUtil.execute(logger, () -> throwIt(new InterruptedException())));
		assertThrown(RuntimeInterruptedException.class,
			() -> LogUtil.execute(null, () -> throwIt(new RuntimeInterruptedException("test"))));
		assertThrown(RuntimeInterruptedException.class,
			() -> LogUtil.execute(logger, () -> throwIt(new RuntimeInterruptedException("test"))));
		assertFalse(LogUtil.execute(null, () -> throwIt(new RuntimeException("rtx"))));
		assertFalse(LogUtil.execute(logger, () -> throwIt(new RuntimeException("rtx"))));
		testLog.assertFind("(?is)ERROR .* catching.*RuntimeException.*rtx");
	}

	@Test
	public void testSubmit() throws InterruptedException, ExecutionException {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		LogUtil.submit(logger, exec, () -> {}).get();
		LogUtil.submit(logger, exec, () -> throwIt(new IOException("iox"))).get();
		testLog.assertFind("(?is)ERROR .* catching.*IOException.*iox");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreate() {
		assertIterable(LogUtil.create(logger, TestCloseable::of, "1", "-1"), new TestCloseable(1),
			new TestCloseable(-1));
		assertThrown(() -> LogUtil.create(logger, TestCloseable::of, "1", "-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		assertArray(LogUtil.createArray(logger, TestCloseable[]::new, TestCloseable::of, "1", "-1"),
			new TestCloseable(1), new TestCloseable(-1));
		assertThrown(() -> LogUtil.createArray(logger, TestCloseable[]::new, TestCloseable::of, "1",
			"-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@Test
	public void testCloseable() throws Exception {
		@SuppressWarnings("resource")
		TestCloseable tc1 = new TestCloseable(1);
		@SuppressWarnings("resource")
		TestCloseable tc2 = new TestCloseable(-1);
		try (AutoCloseable closeable = LogUtil.closeable(logger, tc1, tc2)) {}
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
		try (AutoCloseable closeable = LogUtil.closeable(logger, List.of(tc1, tc2))) {}
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@Test
	public void testCloseProcess() throws IOException {
		assertFalse(LogUtil.close(logger, (Process) null));
		try (TestProcess process = TestProcess.of()) {
			process.waitFor.autoResponses(true);
			assertTrue(LogUtil.close(logger, process));
			process.waitFor.autoResponses(false);
			assertFalse(LogUtil.close(logger, process));
		}
	}

	@Test
	public void testCloseProcessWithInterrupt() throws IOException {
		try (TestProcess process = TestProcess.of()) {
			process.waitFor.error.setFrom(INX);
			assertFalse(LogUtil.close(null, process));
			assertFalse(LogUtil.close(logger, process));
			testLog.assertFind("(?is)INFO .*InterruptedException");
		}
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
	public void testCloseExecutorServiceWithInterrupt() {
		TestExecutorService exec = TestExecutorService.of();
		exec.awaitTermination.error.setFrom(INX);
		assertFalse(LogUtil.close(null, exec));
		assertFalse(LogUtil.close(logger, exec));
		testLog.assertFind("(?is)INFO .*InterruptedException");
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
	public void testCloseFutureWithInterrupt() {
		TestFuture<?> future = TestFuture.of("test");
		future.get.error.setFrom(INX);
		assertFalse(LogUtil.close(null, future));
		assertFalse(LogUtil.close(logger, future));
		testLog.assertFind("(?is)INFO .*InterruptedException");
	}

	@Test
	public void testCloseFutureWithException() {
		TestFuture<?> future = TestFuture.of("test");
		future.get.error.setFrom(TimeoutException::new);
		assertFalse(LogUtil.close(null, future));
		assertFalse(LogUtil.close(logger, future));
		testLog.assertFind("(?is)WARN .*TimeoutException");
	}

	@Test
	public void testCloseCloseables() {
		assertFalse(LogUtil.close(logger, (List<TestCloseable>) null));
	}

	@Test
	public void testFatalf() {
		LogUtil.fatalf(logger, "%d", 123);
		testLog.assertFind("FATAL .* 123");
	}

	@Test
	public void testErrorf() {
		LogUtil.errorf(logger, "%d", 123);
		testLog.assertFind("ERROR .* 123");
	}

	@Test
	public void testWarnf() {
		LogUtil.warnf(logger, "%d", 123);
		testLog.assertFind("WARN .* 123");
	}

	@Test
	public void testInfof() {
		LogUtil.infof(logger, "%d", 123);
		testLog.assertFind("INFO .* 123");
	}

	@Test
	public void testDebug() {
		LogUtil.debugf(logger, "%d", 123);
		testLog.assertFind("DEBUG .* 123");
	}

	@Test
	public void testTracef() {
		LogUtil.tracef(logger, "%d", 123);
		testLog.assertFind("TRACE .* 123");
	}

	@Test
	public void testLogf() {
		LogUtil.logf(logger, Level.WARN, "%d", 123);
		testLog.assertFind("WARN .* 123");
	}

	@Test
	public void testToStringFunction() {
		assertEquals(LogUtil.toString("test", String::toUpperCase).toString(), "TEST");
	}

	@Test
	public void testHashId() {
		assertMatch(LogUtil.hashId(new Object()).toString(), "@[a-f\\d]+");
	}

	@Test
	public void testToStringOrHash() {
		assertEquals(LogUtil.toStringOrHash("test@1234").toString(), "@1234");
		assertMatch(LogUtil.toStringOrHash(new Object()).toString(), "@[a-f\\d]+");
	}

	@Test
	public void testToHex() {
		assertEquals(LogUtil.toHex(1023).toString(), "3ff");
	}

	@Test
	public void testCompact() {
		assertEquals(LogUtil.compact("a  b\n  c   ").toString(), "a b c");
	}

	@Test
	public void testEscaped() {
		assertEquals(LogUtil.escaped("a\n\0\t").toString(), "a\\n\\0\\t");
	}

	@Test
	public void testEscapedAscii() {
		assertEquals(LogUtil.escapedAscii("a\n\0\t".getBytes(ISO_8859_1), 1, 3).toString(),
			"\\n\\0\\t");
	}

	@Test
	public void testIntroMessage() {
		assertFind(LogUtil.introMessage("Test"), " Test ");
		assertFind(LogUtil.introMessage(StringUtil.repeat("Test", 20)),
			" " + StringUtil.repeat("Test", 19) + " ");
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
