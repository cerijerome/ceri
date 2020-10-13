package ceri.common.test;

import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.test.ErrorGen.Mode;

public class SyncStateBehavior {

	@Test
	public void shouldAwaitCallToAcceptBooleanWithRuntimeException() throws InterruptedException {
		SyncState.Bool sync = SyncState.bool();
		try (var exec = SimpleExecutor.run(() -> sync.accept())) {
			sync.awaitCall();
			exec.get();
		}
	}

	@Test
	public void shouldAwaitCallToAcceptBooleanWithIoException() throws InterruptedException {
		SyncState.Bool sync = SyncState.bool();
		try (var exec = SimpleExecutor.run(() -> sync.acceptIo())) {
			sync.awaitCall();
			exec.get();
		}
	}

	@Test
	public void shouldAwaitCallToAcceptBooleanWithException() throws InterruptedException {
		SyncState.Bool sync = SyncState.bool();
		try (var exec = SimpleExecutor.run(() -> sync.accept(SQLException::new))) {
			sync.awaitCall();
			exec.get();
		}
	}

	@Test
	public void shouldNotWaitIfBoolResumeIsDisabled() throws IOException, InterruptedException {
		SyncState.Bool sync = SyncState.boolNoResume();
		sync.acceptIo();
		sync.awaitCall();
	}

	@Test
	public void shouldAssertCall() throws InterruptedException {
		SyncState<String> sync = SyncState.of();
		try (var exec = SimpleExecutor.run(() -> sync.accept("test"))) {
			sync.assertCall("test");
			exec.get();
		}
	}

	@Test
	public void shouldFailToAssertWrongCall() {
		SyncState<String> sync = SyncState.of();
		try (var exec = SimpleExecutor.run(() -> sync.acceptIo("test0"))) {
			assertAssertion(() -> sync.assertCall("test1"));
		}
	}

	@Test
	public void shouldAwaitCallAndExecuteAction() throws InterruptedException {
		SyncState<String> sync = SyncState.of();
		try (var exec = SimpleExecutor.run(() -> sync.acceptIo("test"))) {
			sync.awaitCall(() -> sync.error(Mode.rtInterrupted));
			assertThrown(() -> exec.get());
		}
	}

	@Test
	public void shouldAssertCallAndExecuteAction() throws InterruptedException {
		SyncState<String> sync = SyncState.of();
		try (var exec = SimpleExecutor.run(() -> sync.acceptIo("test"))) {
			sync.assertCall("test", () -> sync.error(Mode.checked));
			assertThrown(() -> exec.get());
		}
	}

	@Test
	public void shouldAssertNoCallWasMade() {
		SyncState<String> sync = SyncState.of();
		sync.resume(false);
		sync.error(Mode.rt);
		assertThrown(() -> sync.accept("test"));
		assertAssertion(() -> sync.assertNoCall());
		sync.reset();
		sync.assertNoCall();
	}

	@Test
	public void shouldNotWaitIfResumeIsDisabled() throws IOException, InterruptedException {
		SyncState<String> sync = SyncState.noResume();
		sync.acceptIo("test");
		sync.assertCall("test");
	}

	@Test
	public void shouldGetCurrentValue() {
		SyncState<String> sync = SyncState.of();
		assertNull(sync.get());
		sync.accept("test");
		assertThat(sync.get(), is("test"));
	}

}
