package ceri.common.collection;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;

public class TypedPipeBehavior {

	@Test
	public void shouldWriteToAndReadFromQueue() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		assertIterable(pipe.in().readN(3), "a", "b", "c");
	}

	@Test
	public void shouldClearPipe() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		assertThat(pipe.in().available(), is(3));
		pipe.clear();
		assertThat(pipe.in().available(), is(0));
	}

	@Test
	public void shouldAwaitRead() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		try (var exec = SimpleExecutor.call(() -> pipe.in().readN(3))) {
			pipe.awaitRead(1);
			assertIterable(exec.get(), "a", "b", "c");
		}
	}

	@Test
	public void shouldAwaitReadWithTimeout() {
		TypedPipe<String> pipe = TypedPipe.of();
		assertThat(pipe.awaitRead(0, 1000), is(true));
		pipe.out().writeAll("a", "b", "c");
		assertThat(pipe.awaitRead(0, 1), is(false));
	}

	@Test
	public void shouldProvideDualPipe() {
		TypedPipe.Bi<String> pipe = TypedPipe.bi();
		pipe.inFeed().writeAll("a", "b", "c");
		pipe.out().writeAll(pipe.in().readN(3));
		assertIterable(pipe.outSink().readN(3), "a", "b", "c");
	}

	@Test
	public void shouldClearDualPipe() {
		TypedPipe.Bi<String> pipe = TypedPipe.bi();
		pipe.inFeed().writeAll("a", "b", "c");
		pipe.out().writeAll("a", "b", "c");
		pipe.clear();
		assertThat(pipe.in().available(), is(0));
		assertThat(pipe.outSink().available(), is(0));
	}

}
