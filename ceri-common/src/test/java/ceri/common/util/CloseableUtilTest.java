package ceri.common.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertIterable;
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
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionCloseable;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.IoUtil;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestProcess;

public class CloseableUtilTest {

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
			assertEquals(CloseableUtil.ref(c).ref, c);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrClose() {
		assertEquals(CloseableUtil.acceptOrClose(null, _ -> {}), null);
		var closer = SyncCloser.io(true);
		assertEquals(CloseableUtil.acceptOrClose(closer, _ -> {}), closer);
		closer.assertClosed(false);
		assertIoe(() -> CloseableUtil.acceptOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrClose() throws Exception {
		assertEquals(CloseableUtil.applyOrClose(null, null), null);
		assertEquals(CloseableUtil.applyOrClose(null, null, "x"), "x");
		var closer = SyncCloser.io(true);
		assertEquals(CloseableUtil.applyOrClose(closer, _ -> null, 1), 1);
		assertEquals(CloseableUtil.applyOrClose(closer, _ -> 0), 0);
		closer.assertClosed(false);
		assertIoe(() -> CloseableUtil.applyOrClose(closer, _ -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrClose() {
		assertEquals(CloseableUtil.runOrClose(null, () -> {}), null);
		var closer = SyncCloser.io(true);
		assertEquals(CloseableUtil.runOrClose(closer, () -> {}), closer);
		closer.assertClosed(false);
		assertIoe(() -> CloseableUtil.runOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrClose() throws Exception {
		assertEquals(CloseableUtil.getOrClose(null, () -> null), null);
		assertEquals(CloseableUtil.getOrClose(null, () -> null, "x"), "x");
		var closer = SyncCloser.io(true);
		assertEquals(CloseableUtil.getOrClose(closer, () -> null, 1), 1);
		assertEquals(CloseableUtil.getOrClose(closer, () -> 0), 0);
		closer.assertClosed(false);
		assertIoe(() -> CloseableUtil.getOrClose(closer, () -> throwIo()));
		closer.assertClosed(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void testAcceptOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(CloseableUtil.acceptOrCloseAll(closers, _ -> {}), closers);
		assertIoe(() -> CloseableUtil.acceptOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testApplyOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(CloseableUtil.applyOrCloseAll(closers, _ -> null), null);
		assertEquals(CloseableUtil.applyOrCloseAll(closers, _ -> null, 3), 3);
		assertEquals(CloseableUtil.applyOrCloseAll(closers, _ -> 1, 3), 1);
		assertIoe(() -> CloseableUtil.applyOrCloseAll(closers, _ -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testRunOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(CloseableUtil.runOrCloseAll(closers, () -> {}), closers);
		assertIoe(() -> CloseableUtil.runOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testGetOrCloseAll() {
		var closers = List.of(SyncCloser.io(true), SyncCloser.io(true));
		assertEquals(CloseableUtil.getOrCloseAll(closers, () -> null), null);
		assertEquals(CloseableUtil.getOrCloseAll(closers, () -> null, 3), 3);
		assertEquals(CloseableUtil.getOrCloseAll(closers, () -> 1, 3), 1);
		assertIoe(() -> CloseableUtil.getOrCloseAll(closers, () -> throwIo()));
		closers.forEach(c -> c.assertClosed(true));
	}

	@Test
	public void testClose() {
		final StringReader in = new StringReader("0123456789");
		assertTrue(CloseableUtil.close());
		assertTrue(CloseableUtil.close((Iterable<Closeable>) null));
		assertTrue(CloseableUtil.close(new AutoCloseable[] { null }));
		assertTrue(CloseableUtil.close(in));
		assertIoe(in::read);
	}

	@Test
	public void testCloseException() {
		@SuppressWarnings("resource")
		Closeable closeable = () -> throwIo();
		assertFalse(CloseableUtil.close(closeable));
	}

	@Test
	public void testCloseWithInterrupt() {
		@SuppressWarnings("resource")
		AutoCloseable closeable = () -> throwInterrupted();
		assertFalse(CloseableUtil.close(closeable));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testCloseProcess() throws IOException {
		assertTrue(CloseableUtil.close((Process) null));
		try (TestProcess p = TestProcess.of()) {
			assertTrue(CloseableUtil.close(p));
			p.waitFor.assertCalls(1);
			p.waitFor.error.setFrom(IOX);
			assertFalse(CloseableUtil.close(p));
		}
	}

	@Test
	public void testCloseExecutor() {
		assertTrue(CloseableUtil.close((ExecutorService) null));
		try (var exec = TestExecutorService.of()) {
			assertTrue(CloseableUtil.close(exec));
			exec.shutdown.assertAuto(true);
			exec.shutdown.error.setFrom(IOX);
			assertFalse(CloseableUtil.close(exec));
		}
	}

	@Test
	public void testCloseFuture() {
		assertTrue(CloseableUtil.close((Future<?>) null));
		var f = TestFuture.of("test");
		assertTrue(CloseableUtil.close(f));
		f.get.assertCalls(1);
		f.get.error.set(new CancellationException());
		assertTrue(CloseableUtil.close(f));
		f.get.error.setFrom(IOX);
		assertFalse(CloseableUtil.close(f));
	}

	@SuppressWarnings("resource")
	@Test
	public void testReverseClose() {
		var captor = Captor.ofInt();
		RuntimeCloseable c0 = () -> captor.accept(0);
		RuntimeCloseable c1 = () -> captor.accept(1);
		RuntimeCloseable c2 = () -> captor.accept(2);
		assertTrue(CloseableUtil.closeReversed(c0, c1, c2));
		captor.verify(2, 1, 0);
	}

	@Test
	public void testReverseCloseWithFailure() {
		assertTrue(CloseableUtil.closeReversed(() -> {}, () -> {}, () -> {}));
		assertFalse(CloseableUtil.closeReversed(() -> {}, () -> {}, () -> throwIo()));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateList() {
		var captor = Captor.of();
		var list = CloseableUtil.create(function(captor), 1, 2, 3);
		assertIterable(list, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateListWithError() {
		var captor = Captor.of();
		assertRte(() -> CloseableUtil.create(function(captor), 1, 2, -1));
		captor.verify(Closer.of(1, true), Closer.of(2, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateFromCount() {
		var captor = Captor.of();
		var list = CloseableUtil.create(supplier(captor), 3);
		assertIterable(list, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateFromCountWithError() {
		var captor = Captor.of();
		assertRte(() -> CloseableUtil.create(supplier(captor), 4));
		captor.verify(Closer.of(1, true), Closer.of(2, true), Closer.of(3, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArray() {
		var captor = Captor.of();
		var array = CloseableUtil.createArray(Closer[]::new, function(captor), 1, 2, 3);
		assertArray(array, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayWithError() {
		var captor = Captor.of();
		assertRte(() -> CloseableUtil.createArray(Closer[]::new, function(captor), 1, 2, -1));
		captor.verify(Closer.of(1, true), Closer.of(2, true));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayFromCount() {
		var captor = Captor.of();
		var array = CloseableUtil.createArray(Closer[]::new, supplier(captor), 3);
		assertArray(array, Closer.of(1, false), Closer.of(2, false), Closer.of(3, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void testCreateArrayFromCountWithError() {
		var captor = Captor.of();
		assertRte(() -> CloseableUtil.createArray(Closer[]::new, supplier(captor), 4));
		captor.verify(Closer.of(1, true), Closer.of(2, true), Closer.of(3, true));
	}

	/**
	 * Simple closer.
	 */
	public static class SyncCloser<E extends Exception> extends CallSync.Runnable
		implements ExceptionCloseable<E> {
		private final ExceptionAdapter<E> adapter;

		public static SyncCloser<RuntimeException> runtime(boolean autoResponse) {
			return new SyncCloser<>(ExceptionAdapter.RUNTIME, autoResponse);
		}

		public static SyncCloser<IOException> io(boolean autoResponse) {
			return new SyncCloser<>(IoUtil.IO_ADAPTER, autoResponse);
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

	private static ExceptionFunction<RuntimeException, Integer, Closer>
		function(Captor<Object> captor) {
		return i -> {
			Closer c = Closer.of(i, false);
			captor.accept(c);
			return c;
		};
	}

	private static ExceptionSupplier<RuntimeException, Closer> supplier(Captor<Object> captor) {
		return () -> {
			Closer c = Closer.of(captor.values.size() + 1, false);
			captor.accept(c);
			return c;
		};
	}
}
