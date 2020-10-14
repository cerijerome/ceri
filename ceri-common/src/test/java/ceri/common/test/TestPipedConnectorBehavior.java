package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertRead;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
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
			assertThat(sync.await(), is(StateChange.broken));
			con.reset(false);
			con.listeners.accept(StateChange.broken);
			assertThat(sync.await(), is(StateChange.broken));
		}
		con.listeners.accept(StateChange.broken);
		assertNull(sync.value());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedBytes() throws IOException {
		con.to.writeBytes(1, 2, 3);
		assertThat(con.in().available(), is(3));
		assertRead(con.in(), 1, 2, 3);
		assertThat(con.in().available(), is(0));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReturnEof() throws IOException {
		con.to.writeBytes(1, 2, 3, 4, 5);
		con.eof(true);
		assertThat(con.in().available(), is(5));
		assertThat(con.in().read(), is(-1));
		assertThat(con.in().read(new byte[2]), is(-1));
		con.eof(false);
		assertRead(con.in(), 4, 5);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSinkBytes() throws IOException {
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertThat(con.from.available(), is(3));
		assertRead(con.from, 1, 2, 3);
		assertThat(con.from.available(), is(0));
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
		assertThat(con.in().available(), is(1));
		assertThrown(() -> con.in().read());
		assertThat(con.in().available(), is(0));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldGenerateAvailableError() throws IOException {
		assertThat(con.in().available(), is(0));
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

	private static <T> SimpleExecutor<RuntimeException, T>
		call(ExceptionSupplier<IOException, T> supplier) {
		return SimpleExecutor.call(() -> queue.executeGet(supplier));
	}

	private static void processQueue() throws IOException {
		while (true)
			queue.processNext();
	}
}
