package ceri.common.function;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwInterrupted;
import static ceri.common.test.AssertUtil.throwIo;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.ErrorGen.IOX;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.Test;
import ceri.common.except.ExceptionAdapter;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestProcess;

public class CloseablesTest {

	private static class Closer implements Closeable {
		private boolean closed = false;
		private int i = 0;

		private static Closer of(int i, boolean closed) {
			if (i < 1 || i > 3) throwRuntime(); // only allow i = 1..3
			var c = new Closer();
			c.i = i;
			c.closed = closed;
			return c;

		}

		@Override
		public void close() throws IOException {
			closed = true;
		}

		@Override
		public int hashCode() {
			return Objects.hash(i, closed);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Closer other)) return false;
			return i == other.i && closed == other.closed;
		}
	}

	@Test
	public void testRef() throws IOException {
		try (Closeable c = () -> {}) {
			assertEquals(Closeables.ref(c).ref, c);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrClose() {
		assertEquals(Closeables.acceptOrClose(null, _ -> {}), null);
		var closer = SyncCloser.io(true);
		assertEquals(Closeables.acceptOrClose(closer, _ -> {}), closer);
		closer.assertClosed(false);
		assertIoe(() -> Closeables.acceptOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrClose() throws Exception {
		assertEquals(Closeables.applyOrClose(null, null), null);
		assertEquals(Closeables.applyOrClose(null, null, "x"), "x");
		var closer = SyncCloser.io(true);
		assertEquals(Closeables.applyOrClose(closer, _ -> null, 1), 1);
		assertEquals(Closeables.applyOrClose(closer, _ -> 0), 0);
		closer.assertClosed(false);
		assertIoe(() -> Closeables.applyOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrClose() {
		assertEquals(Closeables.runOrClose(null, () -> {}), null);
		var closer = SyncCloser.io(true);
		assertEquals(Closeables.runOrClose(closer, () -> {}), closer);
		closer.assertClosed(false);
		assertIoe(() -> Closeables.runOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrClose() throws Exception {
		assertEquals(Closeables.getOrClose(null, () -> null), null);
		assertEquals(Closeables.getOrClose(null, () -> null, "x"), "x");
		var closer = SyncCloser.io(true);
		assertEquals(Closeables.getOrClose(closer, () -> null, 1), 1);
		assertEquals(Closeables.getOrClose(closer, () -> 0), 0);
		closer.assertClosed(false);
		assertIoe(() -> Closeables.getOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(Closeables.acceptOrCloseAll(closers, _ -> {}), closers);
		assertIoe(() -> Closeables.acceptOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(Closeables.applyOrCloseAll(closers, _ -> null), null);
		assertEquals(Closeables.applyOrCloseAll(closers, _ -> null, 3), 3);
		assertEquals(Closeables.applyOrCloseAll(closers, _ -> 1, 3), 1);
		assertIoe(() -> Closeables.applyOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(Closeables.runOrCloseAll(closers, () -> {}), closers);
		assertIoe(() -> Closeables.runOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(Closeables.getOrCloseAll(closers, () -> null), null);
		assertEquals(Closeables.getOrCloseAll(closers, () -> null, 3), 3);
		assertEquals(Closeables.getOrCloseAll(closers, () -> 1, 3), 1);
		assertIoe(() -> Closeables.getOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@Test
	public void testClose() {
		final StringReader in = new StringReader("0123456789");
		assertTrue(Closeables.close());
		assertTrue(Closeables.close((Iterable<Closeable>) null));
		assertTrue(Closeables.close(new AutoCloseable[] { null }));
		assertTrue(Closeables.close(in));
		assertIoe(in::read);
	}

	@Test
	public void testCloseException() {
		@SuppressWarnings("resource")
		Closeable closeable = () -> throwIo();
		assertFalse(Closeables.close(closeable));
	}

	@Test
	public void testCloseWithInterrupt() {
		@SuppressWarnings("resource")
		AutoCloseable closeable = () -> throwInterrupted();
		assertFalse(Closeables.close(closeable));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testCloseProcess() throws IOException {
		assertTrue(Closeables.close((Process) null));
		try (TestProcess p = TestProcess.of()) {
			assertTrue(Closeables.close(p));
			p.waitFor.assertCalls(1);
			p.waitFor.error.setFrom(IOX);
			assertFalse(Closeables.close(p));
		}
	}

	@Test
	public void testCloseExecutor() {
		assertTrue(Closeables.close((ExecutorService) null));
		try (var exec = TestExecutorService.of()) {
			assertTrue(Closeables.close(exec));
			exec.shutdown.assertAuto(true);
			exec.shutdown.error.setFrom(IOX);
			assertFalse(Closeables.close(exec));
		}
	}

	@Test
	public void testCloseFuture() {
		assertTrue(Closeables.close((Future<?>) null));
		var f = TestFuture.of("test");
		assertTrue(Closeables.close(f));
		f.get.assertCalls(1);
		f.get.error.set(new CancellationException());
		assertTrue(Closeables.close(f));
		f.get.error.setFrom(IOX);
		assertFalse(Closeables.close(f));
	}

	@SuppressWarnings("resource")
	@Test
	public void testReverseClose() {
		var captor = Captor.ofInt();
		Functions.Closeable c0 = () -> captor.accept(0);
		Functions.Closeable c1 = () -> captor.accept(1);
		Functions.Closeable c2 = () -> captor.accept(2);
		assertTrue(Closeables.closeReversed(c0, c1, c2));
		captor.verify(2, 1, 0);
	}

	@Test
	public void testReverseCloseWithFailure() {
		assertTrue(Closeables.closeReversed(() -> {}, () -> {}, () -> {}));
		assertFalse(Closeables.closeReversed(() -> {}, () -> {}, () -> throwIo()));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateList() {
		var captor = Captor.of();
		var list = Closeables.create(function(captor), 1, 2, 3);
		assertOrdered(list, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateListWithError() {
		var captor = Captor.of();
		assertRte(() -> Closeables.create(function(captor), 1, 2, -1));
		captor.verify(Closer.of(1, true), Closer.of(2, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateFromCount() {
		var captor = Captor.of();
		var list = Closeables.create(supplier(captor), 3);
		assertOrdered(list, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateFromCountWithError() {
		var captor = Captor.of();
		assertRte(() -> Closeables.create(supplier(captor), 4));
		captor.verify(Closer.of(1, true), Closer.of(2, true), Closer.of(3, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		var captor = Captor.of();
		var array = Closeables.createArray(Closer[]::new, function(captor), 1, 2, 3);
		assertArray(array, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayWithError() {
		var captor = Captor.of();
		assertRte(() -> Closeables.createArray(Closer[]::new, function(captor), 1, 2, -1));
		captor.verify(Closer.of(1, true), Closer.of(2, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayFromCount() {
		var captor = Captor.of();
		var array = Closeables.createArray(Closer[]::new, supplier(captor), 3);
		assertArray(array, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayFromCountWithError() {
		var captor = Captor.of();
		assertRte(() -> Closeables.createArray(Closer[]::new, supplier(captor), 4));
		captor.verify(Closer.of(1, true), Closer.of(2, true), Closer.of(3, true));
	}

	/**
	 * Simple closer.
	 */
	public static class SyncCloser<E extends Exception> extends CallSync.Runnable
		implements Excepts.Closeable<E> {
		private final ExceptionAdapter<E> adapter;

		public static SyncCloser<RuntimeException> runtime(boolean autoResponse) {
			return new SyncCloser<>(ExceptionAdapter.runtime, autoResponse);
		}

		public static SyncCloser<IOException> io(boolean autoResponse) {
			return new SyncCloser<>(ExceptionAdapter.io, autoResponse);
		}

		public SyncCloser(ExceptionAdapter<E> adapter, boolean autoResponse) {
			super(autoResponse);
			this.adapter = adapter;
		}

		public void assertClosed(boolean closed) {
			if (!closed) assertNoCall();
			else assertCalls(1);
		}

		@Override
		public void close() throws E {
			run(adapter);
		}
	}

	private static Excepts.Function<RuntimeException, Integer, Closer>
		function(Captor<Object> captor) {
		return i -> {
			Closer c = Closer.of(i, false);
			captor.accept(c);
			return c;
		};
	}

	private static Excepts.Supplier<RuntimeException, Closer> supplier(Captor<Object> captor) {
		return () -> {
			Closer c = Closer.of(captor.values.size() + 1, false);
			captor.accept(c);
			return c;
		};
	}
}
