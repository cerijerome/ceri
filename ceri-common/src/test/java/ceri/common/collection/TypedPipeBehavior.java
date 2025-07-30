package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.threadRun;
import org.junit.Test;
import ceri.common.test.CallSync;

public class TypedPipeBehavior {

	@Test
	public void shouldWriteToAndReadFromQueue() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		assertOrdered(pipe.in().readN(3), "a", "b", "c");
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
	public void shouldAwaitReadWithTimeout() {
		TypedPipe<String> pipe = TypedPipe.of();
		assertTrue(pipe.in().awaitRead(0, 1000));
		pipe.out().writeAll("a", "b", "c");
		assertFalse(pipe.in().awaitRead(0, 1));
	}

	@Test
	public void shouldAwaitRead() {
		CallSync.Supplier<Integer> available = CallSync.supplier();
		var in = new TypedPipe.In<String>(null) {
			@Override
			public int available() {
				return available.get();
			}
		};
		try (var exec = threadRun(() -> in.awaitRead(1))) {
			available.await(1);
			available.await(0);
			exec.get();
		}
	}

}
