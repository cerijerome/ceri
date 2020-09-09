package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
		assertThat(Enclosed.empty().isEmpty(), is(true));
		assertThat(Enclosed.noOp(null).isEmpty(), is(true));
		assertThat(Enclosed.noOp(new Object()).isEmpty(), is(false));
		assertThat(Enclosed.of(new Object(), null).isEmpty(), is(false));
		assertThat(Enclosed.of(null, x -> {}).isNoOp(), is(true));
		assertThat(Enclosed.of(new Object(), x -> {}).isNoOp(), is(false));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfCloseOperationExists() {
		assertThat(Enclosed.empty().isNoOp(), is(true));
		assertThat(Enclosed.noOp(null).isNoOp(), is(true));
		assertThat(Enclosed.noOp(new Object()).isNoOp(), is(true));
		assertThat(Enclosed.of(new Object(), null).isNoOp(), is(true));
		assertThat(Enclosed.of(null, x -> {}).isNoOp(), is(true));
		assertThat(Enclosed.of(new Object(), x -> {}).isNoOp(), is(false));
	}

}
