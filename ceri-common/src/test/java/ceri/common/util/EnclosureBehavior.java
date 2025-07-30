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
import ceri.common.function.Functions;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;

public class EnclosureBehavior {

	@Test
	public void shouldProvideRepeatableClosure() {
		var sync = CallSync.runnable(true);
		try (var rep = Enclosure.Repeater.of(() -> enclosure("test", sync))) {
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
		try (var rep = Enclosure.Repeater.unsafe(() -> enclosure("test", sync))) {
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
			try (var _ = Enclosure.of(ec)) {}
			sync.assertCalls(1);
		}
	}

	@Test
	public void shouldTransformCloseable() {
		var sync = CallSync.runnable(true);
		try (var _ = Enclosure.from("test", () -> sync.run())) {}
		sync.assertCalls(1);
	}

	@Test
	public void shouldCreateCloseable() {
		var sync = CallSync.runnable(true);
		try (var c = Enclosure.from(() -> sync, CallSync.Runnable::run)) {
			assertEquals(c.ref, sync);
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAdaptCloseableType() {
		var sync = CallSync.runnable(true);
		Functions.Closeable rc = sync::run;
		try (Enclosure<String> c = Enclosure.adapt(rc, _ -> "test")) {
			assertEquals(c.ref, "test");
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseOnAdaptFailure() {
		var sync = CallSync.runnable(true);
		Functions.Closeable rc = sync::run;
		assertThrown(() -> Enclosure.adapt(rc, _ -> throwRuntime()));
		sync.assertCalls(1);
	}

	@Test
	public void shouldExecuteOnClose() {
		String[] ss = { "a" };
		try (var _ = Enclosure.of(ss, s -> s[0] = null)) {}
		assertNull(ss[0]);
	}

	@Test
	public void shouldNotExecuteForNullSubject() {
		String[] ss = null;
		try (var _ = Enclosure.of(ss, _ -> {
			throw new RuntimeException();
		})) {}
	}

	@Test
	public void shouldAllowNullCloser() throws Exception {
		String[] ss = null;
		try (var _ = Enclosure.of(ss, null)) {}
	}

	@Test
	public void shouldCloseCloseablesInReverseOrder() {
		var captor = Captor.ofInt();
		try (var _ = Enclosure.ofAll(() -> captor.accept(1), () -> captor.accept(2))) {}
		captor.verify(2, 1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfSubjectExists() {
		assertTrue(Enclosure.empty().isEmpty());
		assertTrue(Enclosure.noOp(null).isEmpty());
		assertFalse(Enclosure.noOp(new Object()).isEmpty());
		assertFalse(Enclosure.of(new Object(), null).isEmpty());
		assertTrue(Enclosure.of(null, _ -> {}).isNoOp());
		assertFalse(Enclosure.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfCloseOperationExists() {
		assertTrue(Enclosure.empty().isNoOp());
		assertTrue(Enclosure.noOp(null).isNoOp());
		assertTrue(Enclosure.noOp(new Object()).isNoOp());
		assertTrue(Enclosure.of(new Object(), null).isNoOp());
		assertTrue(Enclosure.of(null, _ -> {}).isNoOp());
		assertFalse(Enclosure.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideStringRepresentation() {
		assertString(Enclosure.of("test", _ -> {}), "[test]");
	}

	private static <T> Enclosure<T> enclosure(T t, CallSync.Runnable closer) {
		return Enclosure.from(t, closer::run);
	}
}
