package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Excepts.Closeable;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;

public class EnclosedBehavior {

	@Test
	public void shouldProvideRepeatableClosure() {
		var sync = CallSync.runnable(true);
		try (var rep = Enclosed.Repeater.of(() -> enclosed("test", sync))) {
			assertEquals(rep.ref(), null);
			assertEquals(rep.get(), "test");
			sync.assertNoCall();
			assertEquals(rep.init(), "test");
			assertEquals(rep.get(), "test");
			sync.assertCalls(1);
			assertEquals(rep.ref(), "test");
		}
	}

	@Test
	public void shouldProvideUnsafeRepeatableClosure() {
		var sync = CallSync.runnable(true);
		try (var rep = Enclosed.Repeater.unsafe(() -> enclosed("test", sync))) {
			assertEquals(rep.get(), "test");
			sync.assertNoCall();
			assertEquals(rep.init(), "test");
			assertEquals(rep.get(), "test");
			sync.assertCalls(1);
		}
	}

	@Test
	public void shouldWrapCloseable() throws IOException {
		var sync = CallSync.runnable(true);
		try (Closeable<IOException> ec = () -> sync.run()) {
			try (var _ = Enclosed.of(ec)) {}
			sync.assertCalls(1);
		}
	}

	@Test
	public void shouldTransformCloseable() {
		var sync = CallSync.runnable(true);
		try (var _ = Enclosed.from("test", () -> sync.run())) {}
		sync.assertCalls(1);
	}

	@Test
	public void shouldCreateCloseable() {
		var sync = CallSync.runnable(true);
		try (var c = Enclosed.from(() -> sync, CallSync.Runnable::run)) {
			assertEquals(c.ref, sync);
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAdaptCloseableType() {
		var sync = CallSync.runnable(true);
		RuntimeCloseable rc = sync::run;
		try (Enclosed<RuntimeException, String> c = Enclosed.adaptOrClose(rc, _ -> "test")) {
			assertEquals(c.ref, "test");
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseOnAdaptFailure() {
		var sync = CallSync.runnable(true);
		RuntimeCloseable rc = sync::run;
		assertThrown(() -> Enclosed.adaptOrClose(rc, _ -> throwRuntime()));
		sync.assertCalls(1);
	}

	@Test
	public void shouldExecuteOnClose() {
		String[] ss = { "a" };
		try (var _ = Enclosed.of(ss, s -> s[0] = null)) {}
		assertNull(ss[0]);
	}

	@Test
	public void shouldNotExecuteForNullSubject() {
		String[] ss = null;
		try (var _ = Enclosed.of(ss, _ -> {
			throw new RuntimeException();
		})) {}
	}

	@Test
	public void shouldAllowNullCloser() throws Exception {
		String[] ss = null;
		try (var _ = Enclosed.of(ss, null)) {}
	}

	@Test
	public void shouldCloseCloseablesInReverseOrder() {
		var captor = Captor.ofInt();
		try (var _ = Enclosed.ofAll(() -> captor.accept(1), () -> captor.accept(2))) {}
		captor.verify(2, 1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfSubjectExists() {
		assertTrue(Enclosed.empty().isEmpty());
		assertTrue(Enclosed.noOp(null).isEmpty());
		assertFalse(Enclosed.noOp(new Object()).isEmpty());
		assertFalse(Enclosed.of(new Object(), null).isEmpty());
		assertTrue(Enclosed.of(null, _ -> {}).isNoOp());
		assertFalse(Enclosed.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfCloseOperationExists() {
		assertTrue(Enclosed.empty().isNoOp());
		assertTrue(Enclosed.noOp(null).isNoOp());
		assertTrue(Enclosed.noOp(new Object()).isNoOp());
		assertTrue(Enclosed.of(new Object(), null).isNoOp());
		assertTrue(Enclosed.of(null, _ -> {}).isNoOp());
		assertFalse(Enclosed.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideStringRepresentation() {
		assertString(Enclosed.of("test", _ -> {}), "[test]");
	}

	private static <T> Enclosed<RuntimeException, T> enclosed(T t, Runnable closer) {
		return Enclosed.from(t, closer::run);
	}
}
