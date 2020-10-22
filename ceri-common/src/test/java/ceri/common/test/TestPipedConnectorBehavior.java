package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.TaskQueue;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionSupplier;
import ceri.common.io.StateChange;
import ceri.common.test.ErrorGen.Mode;

public class TestPipedConnectorBehavior {
	private static TestPipedConnector con;
	private static TaskQueue<IOException> queue;
	private static SimpleExecutor<?, ?> exec;

	@BeforeClass
	public static void beforeClass() {
		con = TestPipedConnector.of();
		// Task queue and thread for async piped reads; piped write fails if thread is stopped
		queue = TaskQueue.of(1);
		exec = SimpleExecutor.run(() -> processQueue());
	}

	@Before
	public void before() {
		con.reset(true);
	}

	@AfterClass
	public static void afterClass() {
		exec.close();
		con.close();
	}

	@Test
	public void shouldListenForStateChanges() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = con.listeners().enclose(sync::signal)) {
			con.listeners.accept(StateChange.broken);
			assertEquals(sync.await(), StateChange.broken);
			con.reset(false);
			con.listeners.accept(StateChange.broken);
			assertEquals(sync.await(), StateChange.broken);
		}
		con.listeners.accept(StateChange.broken);
		assertNull(sync.value());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedBytes() throws IOException {
		con.to.writeBytes(1, 2, 3);
		assertEquals(con.in().available(), 3);
		assertRead(con.in(), 1, 2, 3);
		assertEquals(con.in().available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReturnEof() throws IOException {
		con.to.writeBytes(1, 2, 3, 4, 5);
		con.eof(true);
		assertEquals(con.in().available(), 5);
		assertEquals(con.in().read(), -1);
		assertEquals(con.in().read(new byte[2]), -1);
		con.eof(false);
		assertRead(con.in(), 4, 5);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSinkBytes() throws IOException {
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertEquals(con.from.available(), 3);
		assertRead(con.from, 1, 2, 3);
		assertEquals(con.from.available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForFeedToBeEmpty() throws IOException {
		con.to.writeBytes(1, 2, 3);
		try (var exec = call(() -> con.in().readNBytes(3))) {
			con.awaitFeed();
			assertArray(exec.get(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateReadError() throws IOException {
		con.to.writeByte(0);
		con.readError.mode(Mode.checked);
		assertEquals(con.in().available(), 1);
		assertThrown(() -> con.in().read());
		assertEquals(con.in().available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateAvailableError() throws IOException {
		assertEquals(con.in().available(), 0);
		con.availableError.mode(Mode.checked);
		assertThrown(() -> con.in().available());
	}

	@SuppressWarnings("resource")
	@Test
	public void should() throws IOException {
		con.assertAvailable(0);
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		con.out().flush();
		assertAssertion(() -> con.assertAvailable(2));
		con.assertAvailable(3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateWriteError() throws IOException {
		con.out().write(1);
		con.writeError.mode(Mode.checked);
		assertThrown(() -> con.out().write(2));
		assertRead(con.from, 1, 2);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(con.toString(), "(?s)listeners=0.*in=none;0;none;\n");
		con.eof(true);
		assertFind(con.toString(), "(?s)listeners=0.*in=none;0;none;EOF");
	}

	private static <T> SimpleExecutor<RuntimeException, T>
		call(ExceptionSupplier<IOException, T> supplier) {
		return SimpleExecutor.call(() -> queue.executeGet(supplier));
	}

	private static void processQueue() throws IOException {
		while (true)
			queue.processNext();
	}
}
