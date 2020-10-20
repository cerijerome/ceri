package ceri.common.util;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class EnclosedBehavior {

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
	public void shouldAllowNullCloser() {
		String[] ss = null;
		try (var c = Enclosed.of(ss, null)) {}
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
