package ceri.common.util;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;

public class EnclosedBehavior {

	@Test
	public void shouldWrapCloseMethod() {
		var sync = CallSync.runnable(true);
		try (var x = Enclosed.of(sync::run)) {
			sync.assertCalls(0);
		}
		sync.awaitAuto();
	}

	@Test
	public void shouldTransformCloseable() throws IOException {
		var sync = CallSync.runnable(true);
		try (Enclosed<IOException, String> c = Enclosed.from("test", () -> sync.run())) {}
		sync.assertCalls(1);
	}

	@Test
	public void shouldExecuteOnClose() {
		String[] ss = { "a" };
		try (var c = Enclosed.of(ss, s -> s[0] = null)) {}
		assertNull(ss[0]);
	}

	@Test
	public void shouldNotExecuteForNullSubject() {
		String[] ss = null;
		try (var c = Enclosed.of(ss, s -> {
			throw new RuntimeException();
		})) {}
	}

	@Test
	public void shouldAllowNullCloser() throws Exception {
		String[] ss = null;
		try (var c = Enclosed.of(ss, null)) {}
	}

	@Test
	public void shouldCloseCloseablesInReverseOrder() {
		var captor = Captor.ofInt();
		try (var enc = Enclosed.ofAll(() -> captor.accept(1), () -> captor.accept(2))) {}
		captor.verify(2, 1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfSubjectExists() {
		assertTrue(Enclosed.empty().isEmpty());
		assertTrue(Enclosed.noOp(null).isEmpty());
		assertFalse(Enclosed.noOp(new Object()).isEmpty());
		assertFalse(Enclosed.of(new Object(), null).isEmpty());
		assertTrue(Enclosed.of(null, x -> {}).isNoOp());
		assertFalse(Enclosed.of(new Object(), x -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfCloseOperationExists() {
		assertTrue(Enclosed.empty().isNoOp());
		assertTrue(Enclosed.noOp(null).isNoOp());
		assertTrue(Enclosed.noOp(new Object()).isNoOp());
		assertTrue(Enclosed.of(new Object(), null).isNoOp());
		assertTrue(Enclosed.of(null, x -> {}).isNoOp());
		assertFalse(Enclosed.of(new Object(), x -> {}).isNoOp());
	}

}
