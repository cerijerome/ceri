package ceri.log.util;

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
import ceri.common.concurrent.BoolCondition;
import ceri.common.test.Assert;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.Captor;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestProcess;
import ceri.common.text.Strings;
import ceri.log.io.LogPrintStream;

public class LogsTest {
	private static final Object OBJ = new Object() {};
	private static TestLog testLog;
	private static Logger logger;

	@BeforeClass
	public static void beforeClass() {
		testLog = TestLog.of(Logs.class);
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
		Assert.equal(Logs.loggerName(getClass()), getClass().getName());
		Assert.equal(Logs.loggerName(OBJ.getClass()), getClass().getName() + "$1");
	}

	@Test
	public void testStartupValues() {
		var values = Logs.startupValues("abc", "123");
		Assert.equal(values.next(p -> p.get()), "abc");
		Assert.equal(values.next(p -> p.toInt()), 123);
		Assert.equal(values.envVar("test"), "CERI_LOG_UTIL_TEST");
		values = Logs.startupValues(Level.WARN, "abc", "123");
		Assert.equal(values.next(p -> p.get()), "abc");
		Assert.equal(values.next(p -> p.toInt()), 123);
		Assert.equal(values.envVar("test"), "CERI_LOG_UTIL_TEST");
		testLog.assertFind("WARN.*value = abc");
	}

	@Test
	public void testBinaryLogger() {
		var bp = Logs.binaryLogger();
		bp.print(1, 2, 3);
		try (var stream = LogPrintStream.of(logger)) {
			bp = Logs.binaryLogger(BinaryPrinter.ASCII, stream);
			bp.print("abc").flush();
			testLog.assertFind("DEBUG .* 61 62 63 abc");
		}
	}

	@Test
	public void testGetSilently() {
		Assert.thrown(() -> Logs.getSilently(null));
		Assert.equal(Logs.getSilently(() -> null), null);
		Assert.equal(Logs.getSilently(() -> null, 123), null);
		Assert.equal(Logs.getSilently(() -> 123), 123);
		Assert.equal(Logs.getSilently(() -> 123, 456), 123);
		Assert.equal(Logs.getSilently(() -> Assert.throwIo()), null);
		Assert.equal(Logs.getSilently(() -> Assert.throwIo(), 123), 123);
		Assert.no(Thread.interrupted());
		Logs.getSilently(() -> Assert.throwInterrupted());
		Assert.yes(Thread.interrupted());
	}

	@Test
	public void testRunSilently() {
		Assert.thrown(() -> Logs.runSilently(null));
		Logs.runSilently(() -> Assert.throwIo());
		Assert.no(Thread.interrupted());
		Logs.runSilently(() -> Assert.throwInterrupted());
		Assert.yes(Thread.interrupted());
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrClose() {
		Assert.equal(Logs.acceptOrClose(null, _ -> {}), null);
		var closer = TestCloseable.of("1");
		Assert.equal(Logs.acceptOrClose(closer, _ -> {}), closer);
		closer.assertClosed(false);
		Assert.thrown(() -> Logs.acceptOrClose(closer, _ -> Assert.throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrClose() throws Exception {
		Assert.equal(Logs.applyOrClose(null, null), null);
		Assert.equal(Logs.applyOrClose(null, null, "x"), "x");
		var closer = TestCloseable.of("1");
		Assert.equal(Logs.applyOrClose(closer, _ -> null, 1), 1);
		Assert.equal(Logs.applyOrClose(closer, _ -> 0), 0);
		closer.assertClosed(false);
		Assert.thrown(() -> Logs.applyOrClose(closer, _ -> Assert.throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrClose() {
		Assert.equal(Logs.runOrClose(null, () -> {}), null);
		var closer = TestCloseable.of("1");
		Assert.equal(Logs.runOrClose(closer, () -> {}), closer);
		closer.assertClosed(false);
		Assert.thrown(() -> Logs.runOrClose(closer, () -> Assert.throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrClose() throws Exception {
		Assert.equal(Logs.getOrClose(null, () -> null), null);
		Assert.equal(Logs.getOrClose(null, () -> null, "x"), "x");
		var closer = TestCloseable.of("1");
		Assert.equal(Logs.getOrClose(closer, () -> null, 1), 1);
		Assert.equal(Logs.getOrClose(closer, () -> 0), 0);
		closer.assertClosed(false);
		Assert.thrown(() -> Logs.getOrClose(closer, () -> Assert.throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		Assert.equal(Logs.acceptOrCloseAll(closers, _ -> {}), closers);
		Assert.thrown(() -> Logs.acceptOrCloseAll(closers, _ -> Assert.throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		Assert.equal(Logs.applyOrCloseAll(closers, _ -> null), null);
		Assert.equal(Logs.applyOrCloseAll(closers, _ -> null, 3), 3);
		Assert.equal(Logs.applyOrCloseAll(closers, _ -> 1, 3), 1);
		Assert.thrown(() -> Logs.applyOrCloseAll(closers, _ -> Assert.throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		Assert.equal(Logs.runOrCloseAll(closers, () -> {}), closers);
		Assert.thrown(() -> Logs.runOrCloseAll(closers, () -> Assert.throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrCloseAll() {
		var closers = List.of(TestCloseable.of("1"), TestCloseable.of("2"));
		Assert.equal(Logs.getOrCloseAll(closers, () -> null), null);
		Assert.equal(Logs.getOrCloseAll(closers, () -> null, 3), 3);
		Assert.equal(Logs.getOrCloseAll(closers, () -> 1, 3), 1);
		Assert.thrown(() -> Logs.getOrCloseAll(closers, () -> Assert.throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@Test
	public void testCloseReversed() {
		var captor = Captor.ofInt();
		Assert.yes(Logs.closeReversed(() -> captor.accept(0), () -> captor.accept(1),
			() -> captor.accept(2)));
		captor.verifyInt(2, 1, 0);
		Assert.no(Logs.closeReversed(() -> captor.accept(0), () -> Assert.throwIo()));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreate() {
		Assert.ordered(Logs.create(TestCloseable::of, "1", "-1"), new TestCloseable(1),
			new TestCloseable(-1));
		Assert.thrown(() -> Logs.create(TestCloseable::of, "1", "-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateWithCount() {
		Assert.ordered(Logs.create(() -> new TestCloseable(0), 3), new TestCloseable(0),
			new TestCloseable(0), new TestCloseable(0));
		Assert.thrown(() -> Logs.create(() -> TestCloseable.of("x"), 3));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		Assert.array(Logs.createArray(TestCloseable[]::new, TestCloseable::of, "1", "-1"),
			new TestCloseable(1), new TestCloseable(-1));
		Assert.thrown(
			() -> Logs.createArray(TestCloseable[]::new, TestCloseable::of, "1", "-1", "x"));
		testLog.assertFind("(?is)WARN .* catching.*IOException.*-1");
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayWithCount() {
		Assert.array(Logs.createArray(TestCloseable[]::new, () -> new TestCloseable(0), 3),
			new TestCloseable(0), new TestCloseable(0), new TestCloseable(0));
		Assert.thrown(
			() -> Logs.createArray(TestCloseable[]::new, () -> TestCloseable.of("x"), 3));
	}

	@Test
	public void testCloseProcess() throws IOException {
		Assert.yes(Logs.close((Process) null));
		try (var process = TestProcess.of()) {
			process.waitFor.autoResponses(true);
			Assert.yes(Logs.close(process));
			process.waitFor.autoResponses(false);
			Assert.no(Logs.close(process));
		}
	}

	@Test
	public void testCloseProcessWithInterrupt() throws IOException {
		try (var process = TestProcess.of()) {
			process.waitFor.error.setFrom(ErrorGen.INX);
			Assert.no(Logs.close(process));
			testLog.assertFind("(?is)DEBUG .*InterruptedException");
		}
	}

	@Test
	public void testCloseExecutorService() throws InterruptedException {
		Assert.yes(Logs.close((ExecutorService) null));
		var sync = BoolCondition.of();
		try (var exec = Executors.newSingleThreadExecutor()) {
			exec.execute(() -> signalAndSleep(sync, 60000));
			sync.await();
			Assert.yes(Logs.close(exec));
		}
	}

	@Test
	public void testCloseExecutorServiceWithInterrupt() {
		try (var exec = TestExecutorService.of()) {
			exec.awaitTermination.error.setFrom(ErrorGen.INX, ErrorGen.INX, null);
			Assert.yes(Logs.close(exec));
		}
	}

	@Test
	public void testCloseFuture() throws InterruptedException, ExecutionException {
		Assert.yes(Logs.close((Future<?>) null));
		try (var exec = Executors.newSingleThreadExecutor()) {
			Future<?> future = exec.submit(() -> {});
			future.get();
			Assert.yes(Logs.close(future));
		}
	}

	@Test
	public void testCloseFutureWithCancellation() throws InterruptedException {
		Assert.yes(Logs.close((Future<?>) null));
		var sync = BoolCondition.of();
		try (var exec = Executors.newSingleThreadExecutor()) {
			Future<?> future = exec.submit(() -> signalAndSleep(sync, 60000));
			sync.await();
			Assert.yes(Logs.close(future));
		}
	}

	@Test
	public void testCloseFutureWithInterrupt() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(ErrorGen.INX);
		Assert.no(Logs.close(future));
		testLog.assertFind("(?is)DEBUG .*InterruptedException");
	}

	@Test
	public void testCloseFutureWithException() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(TimeoutException::new);
		Assert.no(Logs.close(future));
		testLog.assertFind("(?is)WARN .*TimeoutException");
	}

	@Test
	public void testCloseCloseables() {
		Assert.yes(Logs.close((List<TestCloseable>) null));
	}

	@Test
	public void testToStringFunction() {
		Assert.equal(Logs.toString("test", String::toUpperCase).toString(), "TEST");
	}

	@Test
	public void testHashId() {
		Assert.match(Logs.hashId(new Object()).toString(), "@[a-f\\d]+");
	}

	@Test
	public void testToHex() {
		Assert.equal(Logs.toHex(1023).toString(), "3ff");
	}

	@Test
	public void testToFormat() {
		Assert.equal(Logs.toFormat("test%d", 123).toString(), "test123");
	}

	@Test
	public void testCompact() {
		Assert.equal(Logs.compact("a  b\n  c   ").toString(), "a b c");
	}

	@Test
	public void testEscaped() {
		Assert.equal(Logs.escaped("a\n\0\t").toString(), "a\\n\\0\\t");
	}

	@Test
	public void testEscapedAscii() {
		Assert.equal(Logs.escapedAscii("a\n\0\t".getBytes(ISO_8859_1), 1, 3).toString(),
			"\\n\\0\\t");
	}

	@Test
	public void testIntroMessage() {
		Assert.find(Logs.introMessage("Test"), " Test ");
		Assert.find(Logs.introMessage(Strings.repeat("Test", 20)),
			" " + Strings.repeat("Test", 19) + " ");
	}

	private static void signalAndSleep(BoolCondition sync, long sleepMs) {
		sync.signal();
		try {
			Thread.sleep(sleepMs);
		} catch (InterruptedException e) {
			// Stop
		}
	}
}
