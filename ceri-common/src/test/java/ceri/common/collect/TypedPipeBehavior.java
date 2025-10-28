package ceri.common.collect;

import static ceri.common.test.Testing.threadRun;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;

public class TypedPipeBehavior {

	@Test
	public void shouldWriteToAndReadFromQueue() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		Assert.ordered(pipe.in().readN(3), "a", "b", "c");
	}

	@Test
	public void shouldClearPipe() {
		TypedPipe<String> pipe = TypedPipe.of();
		pipe.out().writeAll("a", "b", "c");
		Assert.equal(pipe.in().available(), 3);
		pipe.clear();
		Assert.equal(pipe.in().available(), 0);
	}

	@Test
	public void shouldAwaitReadWithTimeout() {
		TypedPipe<String> pipe = TypedPipe.of();
		Assert.yes(pipe.in().awaitRead(0, 1000));
		pipe.out().writeAll("a", "b", "c");
		Assert.no(pipe.in().awaitRead(0, 1));
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
