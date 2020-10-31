package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorProducer.*;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.TaskQueue;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionSupplier;
import ceri.common.io.StateChange;

public class TestConnectorBehavior {
	private TestConnector con;
	private TaskQueue<IOException> queue;
	private SimpleExecutor<?, ?> exec;

	@Before
	public void before() throws IOException {
		con = TestConnector.of();
		con.connect();
		// Task queue and thread for async piped reads; piped write fails if thread is stopped
		queue = TaskQueue.of(1);
		exec = SimpleExecutor.run(() -> processQueue());
	}

	@After
	public void after() throws IOException {
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
		con.in.to.writeBytes(1, 2, 3);
		assertEquals(con.in().available(), 3);
		assertRead(con.in(), 1, 2, 3);
		assertEquals(con.in().available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSinkBytes() throws IOException {
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertEquals(con.out.from.available(), 3);
		assertRead(con.out.from, 1, 2, 3);
		assertEquals(con.out.from.available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForFeedToBeEmpty() throws IOException {
		con.in.to.writeBytes(1, 2, 3);
		try (var exec = call(() -> con.in().readNBytes(3))) {
			con.in.awaitFeed();
			assertArray(exec.get(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateReadError() throws IOException {
		con.in.to.writeByte(0);
		con.in.read.error.setFrom(IOX);
		assertEquals(con.in().available(), 1);
		assertThrown(() -> con.in().read());
		assertEquals(con.in().available(), 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateAvailableError() throws IOException {
		assertEquals(con.in().available(), 0);
		con.in.available.error.setFrom(IOX);
		assertThrown(() -> con.in().available());
	}

	@SuppressWarnings("resource")
	@Test
	public void should() throws IOException {
		con.out.assertAvailable(0);
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		con.out().flush();
		assertAssertion(() -> con.out.assertAvailable(2));
		con.out.assertAvailable(3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateWriteError() throws IOException {
		con.out().write(1);
		con.out.write.error.setFrom(IOX);
		assertThrown(() -> con.out().write(2));
		assertRead(con.out.from, 1, 2);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(con.toString(), ".+");
	}

	private <T> SimpleExecutor<RuntimeException, T>
		call(ExceptionSupplier<IOException, T> supplier) {
		return SimpleExecutor.call(() -> queue.executeGet(supplier));
	}

	private void processQueue() throws IOException {
		while (true)
			queue.processNext();
	}
}
