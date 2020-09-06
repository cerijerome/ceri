package ceri.common.test;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SimpleExecutor;

public class SyncConsumerBehavior {

	@Test
	public void shouldProvideBooleanReceiver() throws InterruptedException {
		SyncConsumer.Bool<IOException> consumer = SyncConsumer.bool();
		try (var exec = SimpleExecutor.run(() -> consumer.accept())) {
			consumer.awaitCall();
			exec.get();
		}
	}

	@Test
	public void shouldExecuteActionBeforeResuming() throws InterruptedException, IOException {
		StringBuilder b = new StringBuilder().append("test");
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		try (var exec = SimpleExecutor.run(() -> {
			consumer.accept(b.toString());
			consumer.accept(b.toString());
			consumer.accept(b.toString());
		})) {
			consumer.assertCall("test", () -> b.append(0));
			assertThat(consumer.awaitCall(() -> b.append(1)), is("test0"));
			consumer.assertCall("test01");
			exec.get();
		}
	}

	@Test
	public void shouldVerifyNoCall() throws InterruptedException {
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		try (var exec = SimpleExecutor.run(() -> consumer.accept("test0"))) {
			consumer.assertCall("test0");
			exec.get();
			consumer.assertNoCall();
		}
	}

	@Test
	public void shouldGenerateRuntimeExceptions() throws InterruptedException {
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		consumer.rtError();
		try (var exec = SimpleExecutor.run(() -> {
			assertThrown(RuntimeException.class, () -> consumer.accept("test0"));
			assertThrown(RuntimeException.class, () -> consumer.accept("test1"));
		})) {
			consumer.assertCall("test0");
			consumer.assertCall("test1");
			exec.get();
		}
	}

	@Test
	public void shouldGenerateRuntimeInterruptedExceptions() throws InterruptedException {
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		consumer.rtiError();
		try (var exec = SimpleExecutor.run(() -> {
			assertThrown(RuntimeInterruptedException.class, () -> consumer.accept("test0"));
			assertThrown(RuntimeInterruptedException.class, () -> consumer.accept("test1"));
		})) {
			consumer.assertCall("test0");
			consumer.assertCall("test1");
			exec.get();
		}
	}

	@Test
	public void shouldGenerateExceptions() throws InterruptedException {
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		consumer.error(IOException::new);
		try (var exec = SimpleExecutor.run(() -> {
			assertThrown(IOException.class, () -> consumer.accept("test0"));
			assertThrown(IOException.class, () -> consumer.accept("test1"));
		})) {
			consumer.assertCall("test0");
			consumer.assertCall("test1");
			exec.get();
		}
	}

	@Test
	public void shouldGenerateExceptionsWithPredicate() throws InterruptedException {
		SyncConsumer<IOException, String> consumer = SyncConsumer.of();
		consumer.rtiError(i -> i == 1);
		consumer.error(IOException::new, i -> i == 2);
		try (var exec = SimpleExecutor.run(() -> {
			consumer.accept("test0");
			assertThrown(RuntimeInterruptedException.class, () -> consumer.accept("test1"));
			assertThrown(IOException.class, () -> consumer.accept("test2"));
			consumer.accept("test3");
		})) {
			consumer.assertCall("test0");
			consumer.assertCall("test1");
			consumer.assertCall("test2");
			consumer.assertCall("test3");
			exec.get();
		}
	}

}
