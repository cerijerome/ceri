package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import ceri.common.concurrent.ConcurrentUtil;

public class TypedPipe<T> {
	private static final int DEFAULT_SIZE = 1024;
	private final BlockingQueue<T> queue;
	private final In<T> in;
	private final Out<T> out;

	public static class In<T> {
		private final BlockingQueue<T> queue;

		private In(BlockingQueue<T> queue) {
			this.queue = queue;
		}

		public int available() {
			return queue.size();
		}

		public T read() {
			return ConcurrentUtil.executeGetInterruptible(queue::take);
		}

		public List<T> readN(int n) {
			List<T> list = new ArrayList<>();
			for (int i = 0; i < n; i++)
				list.add(read());
			return list;
		}
	}

	public static class Out<T> {
		private final BlockingQueue<T> queue;

		private Out(BlockingQueue<T> queue) {
			this.queue = queue;
		}

		public void write(T value) {
			Objects.requireNonNull(value);
			ConcurrentUtil.executeInterruptible(() -> queue.put(value));
		}

		@SafeVarargs
		public final void writeAll(T... values) {
			writeAll(Arrays.asList(values));
		}

		public void writeAll(Collection<T> values) {
			for (T value : values)
				write(value);
		}
	}

	public static class Bi<T> {
		public final TypedPipe<T> pipedIn;
		public final TypedPipe<T> pipedOut;

		private Bi(int inSize, int outSize) {
			pipedIn = TypedPipe.of(inSize);
			pipedOut = TypedPipe.of(outSize);
		}

		public In<T> in() {
			return pipedIn.in();
		}

		public Out<T> out() {
			return pipedOut.out();
		}

		public Out<T> inFeed() {
			return pipedIn.out();
		}

		public In<T> outSink() {
			return pipedOut.in();
		}

		public void clear() {
			pipedIn.clear();
			pipedOut.clear();
		}
	}

	public static <T> Bi<T> bi() {
		return bi(DEFAULT_SIZE, DEFAULT_SIZE);
	}

	public static <T> Bi<T> bi(int inSize, int outSize) {
		return new Bi<>(inSize, outSize);
	}

	public static <T> TypedPipe<T> of() {
		return of(DEFAULT_SIZE);
	}

	public static <T> TypedPipe<T> of(int size) {
		return new TypedPipe<>(size);
	}

	private TypedPipe(int size) {
		queue = new ArrayBlockingQueue<>(size);
		in = new In<>(queue);
		out = new Out<>(queue);
	}

	public void clear() {
		queue.clear();
	}

	public In<T> in() {
		return in;
	}

	public Out<T> out() {
		return out;
	}

	/**
	 * Wait for PipedInputStream to read available bytes.
	 */
	public void awaitRead(int pollMs) {
		while (in.available() > 0)
			ConcurrentUtil.delay(pollMs);
	}

	/**
	 * Wait for PipedInputStream to read available bytes. Returns false if timeout exceeded.
	 */
	public boolean awaitRead(int pollMs, int timeoutMs) {
		long t = System.currentTimeMillis() + timeoutMs;
		while (true) {
			if (in.available() == 0) return true;
			if (System.currentTimeMillis() >= t) return false;
			ConcurrentUtil.delay(pollMs);
		}
	}

}
