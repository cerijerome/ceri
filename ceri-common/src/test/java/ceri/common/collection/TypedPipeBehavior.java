package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
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
		assertEquals(pipe.in().available(), 3);
		pipe.clear();
		assertEquals(pipe.in().available(), 0);
	}

	@Test
	public void shouldAwaitRead() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		try (var exec = SimpleExecutor.call(() -> {
			ConcurrentUtil.delay(1);
			return pipe.in().readN(3);
		})) {
			pipe.awaitRead(1);
			assertIterable(exec.get(), "a", "b", "c");
		}
	}

	@Test
	public void shouldAwaitReadWithTimeout() {
		TypedPipe<String> pipe = TypedPipe.of();
		assertTrue(pipe.awaitRead(0, 1000));
		pipe.out().writeAll("a", "b", "c");
		assertFalse(pipe.awaitRead(0, 1));
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
		assertEquals(pipe.in().available(), 0);
		assertEquals(pipe.outSink().available(), 0);
	}

}
