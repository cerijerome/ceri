package ceri.log.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwInterrupted;
import static ceri.common.test.AssertUtil.throwIo;
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
import ceri.common.test.BinaryPrinter;
import ceri.common.test.Captor;
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
		testLog = TestLog.of(LogUtil.class);
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
		assertEquals(values.next(p -> p.get()), "abc");
		assertEquals(values.next(p -> p.toInt()), 123);
		assertEquals(values.envVar("test"), "CERI_LOG_UTIL_TEST");
		values = LogUtil.startupValues(Level.WARN, "abc", "123");
		assertEquals(values.next(p -> p.get()), "abc");
		assertEquals(values.next(p -> p.toInt()), 123);
		assertEquals(values.envVar("test"), "CERI_LOG_UTIL_TEST");
		testLog.assertFind("WARN.*value = abc");
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
	public void testGetSilently() {
		assertThrown(() -> LogUtil.getSilently(null));
		assertEquals(LogUtil.getSilently(() -> null), null);
		assertEquals(LogUtil.getSilently(() -> null, 123), null);
		assertEquals(LogUtil.getSilently(() -> 123), 123);
		assertEquals(LogUtil.getSilently(() -> 123, 456), 123);
		assertEquals(LogUtil.getSilently(() -> throwIo()), null);
		assertEquals(LogUtil.getSilently(() -> throwIo(), 123), 123);
		assertFalse(Thread.interrupted());
		LogUtil.getSilently(() -> throwInterrupted());
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testRunSilently() {
		assertThrown(() -> LogUtil.runSilently(null));
		LogUtil.runSilently(() -> throwIo());
		assertFalse(Thread.interrupted());
		LogUtil.runSilently(() -> throwInterrupted());
		assertTrue(Thread.interrupted());
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrClose() {
		assertEquals(LogUtil.acceptOrClose(null, _ -> {}), null);
		var closer = TestCloseable.of("1");
		assertEquals(LogUtil.acceptOrClose(closer, _ -> {}), closer);
		closer.assertClosed(false);
		assertThrown(() -> LogUtil.acceptOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrClose() throws Exception {
		assertEquals(LogUtil.applyOrClose(null, null), null);
		assertEquals(LogUtil.applyOrClose(null, null, "x"), "x");
		var closer = TestCloseable.of("1");
		assertEquals(LogUtil.applyOrClose(closer, _ -> null, 1), 1);
		assertEquals(LogUtil.applyOrClose(closer, _ -> 0), 0);
		closer.assertClosed(false);
		assertThrown(() -> LogUtil.applyOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrClose() {
		assertEquals(LogUtil.runOrClose(null, () -> {}), null);
		var closer = TestCloseable.of("1");
		assertEquals(LogUtil.runOrClose(closer, () -> {}), closer);
		closer.assertClosed(false);
		assertThrown(() -> LogUtil.runOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrClose() throws Exception {
		assertEquals(LogUtil.getOrClose(null, () -> null), null);
		assertEquals(LogUtil.getOrClose(null, () -> null, "x"), "x");
		var closer = TestCloseable.of("1");
		assertEquals(LogUtil.getOrClose(closer, () -> null, 1), 1);
		assertEquals(LogUtil.getOrClose(closer, () -> 0), 0);
		closer.assertClosed(false);
		assertThrown(() -> LogUtil.getOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		assertEquals(LogUtil.acceptOrCloseAll(closers, _ -> {}), closers);
		assertThrown(() -> LogUtil.acceptOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		assertEquals(LogUtil.applyOrCloseAll(closers, _ -> null), null);
		assertEquals(LogUtil.applyOrCloseAll(closers, _ -> null, 3), 3);
		assertEquals(LogUtil.applyOrCloseAll(closers, _ -> 1, 3), 1);
		assertThrown(() -> LogUtil.applyOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		assertEquals(LogUtil.runOrCloseAll(closers, () -> {}), closers);
		assertThrown(() -> LogUtil.runOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		assertEquals(LogUtil.getOrCloseAll(closers, () -> null), null);
		assertEquals(LogUtil.getOrCloseAll(closers, () -> null, 3), 3);
		assertEquals(LogUtil.getOrCloseAll(closers, () -> 1, 3), 1);
		assertThrown(() -> LogUtil.getOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@Test
	public void testCloseReversed() {
		var captor = Captor.ofInt();
		assertTrue(LogUtil.closeReversed(() -> captor.accept(0), () -> captor.accept(1),
			() -> captor.accept(2)));
		captor.verifyInt(2, 1, 0);
		assertFalse(LogUtil.closeReversed(() -> captor.accept(0), () -> throwIo()));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreate() {
		assertIterable(LogUtil.create(TestCloseable::of, "1", "-1"), new TestCloseable(1),
			new TestCloseable(-1));
		assertThrown(() -> LogUtil.create(TestCloseable::of, "1", "-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateWithCount() {
		assertIterable(LogUtil.create(() -> new TestCloseable(0), 3), new TestCloseable(0),
			new TestCloseable(0), new TestCloseable(0));
		assertThrown(() -> LogUtil.create(() -> TestCloseable.of("x"), 3));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		assertArray(LogUtil.createArray(TestCloseable[]::new, TestCloseable::of, "1", "-1"),
			new TestCloseable(1), new TestCloseable(-1));
		assertThrown(
			() -> LogUtil.createArray(TestCloseable[]::new, TestCloseable::of, "1", "-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayWithCount() {
		assertArray(LogUtil.createArray(TestCloseable[]::new, () -> new TestCloseable(0), 3),
			new TestCloseable(0), new TestCloseable(0), new TestCloseable(0));
		assertThrown(
			() -> LogUtil.createArray(TestCloseable[]::new, () -> TestCloseable.of("x"), 3));
	}

	@Test
	public void testCloseProcess() throws IOException {
		assertTrue(LogUtil.close((Process) null));
		try (TestProcess process = TestProcess.of()) {
			process.waitFor.autoResponses(true);
			assertTrue(LogUtil.close(process));
			process.waitFor.autoResponses(false);
			assertFalse(LogUtil.close(process));
		}
	}

	@Test
	public void testCloseProcessWithInterrupt() throws IOException {
		try (TestProcess process = TestProcess.of()) {
			process.waitFor.error.setFrom(INX);
			assertFalse(LogUtil.close(process));
			testLog.assertFind("(?is)DEBUG .*InterruptedException");
		}
	}

	@Test
	public void testCloseExecutorService() throws InterruptedException {
		assertTrue(LogUtil.close((ExecutorService) null));
		BooleanCondition sync = BooleanCondition.of();
		try (ExecutorService exec = Executors.newSingleThreadExecutor()) {
			exec.execute(() -> signalAndSleep(sync, 60000));
			sync.await();
			assertTrue(LogUtil.close(exec));
		}
	}

	@Test
	public void testCloseExecutorServiceWithInterrupt() {
		try (TestExecutorService exec = TestExecutorService.of()) {
			exec.awaitTermination.error.setFrom(INX, INX, null);
			assertTrue(LogUtil.close(exec));
		}
	}

	@Test
	public void testCloseFuture() throws InterruptedException, ExecutionException {
		assertTrue(LogUtil.close((Future<?>) null));
		try (ExecutorService exec = Executors.newSingleThreadExecutor()) {
			Future<?> future = exec.submit(() -> {});
			future.get();
			assertTrue(LogUtil.close(future));
		}
	}

	@Test
	public void testCloseFutureWithCancellation() throws InterruptedException {
		assertTrue(LogUtil.close((Future<?>) null));
		BooleanCondition sync = BooleanCondition.of();
		try (ExecutorService exec = Executors.newSingleThreadExecutor()) {
			Future<?> future = exec.submit(() -> signalAndSleep(sync, 60000));
			sync.await();
			assertTrue(LogUtil.close(future));
		}
	}

	@Test
	public void testCloseFutureWithInterrupt() {
		TestFuture<?> future = TestFuture.of("test");
		future.get.error.setFrom(INX);
		assertFalse(LogUtil.close(future));
		testLog.assertFind("(?is)DEBUG .*InterruptedException");
	}

	@Test
	public void testCloseFutureWithException() {
		TestFuture<?> future = TestFuture.of("test");
		future.get.error.setFrom(TimeoutException::new);
		assertFalse(LogUtil.close(future));
		testLog.assertFind("(?is)WARN .*TimeoutException");
	}

	@Test
	public void testCloseCloseables() {
		assertTrue(LogUtil.close((List<TestCloseable>) null));
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
	public void testToHex() {
		assertEquals(LogUtil.toHex(1023).toString(), "3ff");
	}

	@Test
	public void testToFormat() {
		assertEquals(LogUtil.toFormat("test%d", 123).toString(), "test123");
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
