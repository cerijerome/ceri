package ceri.common.io;

import static org.junit.Assert.assertNull;
import java.io.Closeable;
import java.io.IOException;
import org.junit.Test;

public class CloseableWrapperBehavior {

	@Test
	public void shouldExecuteOnClose() throws IOException {
		String[] ss = { "a" };
		try (Closeable c = CloseableWrapper.of(ss, s -> {
			s[0] = null;
		})) {}
		assertNull(ss[0]);
	}

	@Test
	public void shouldNotExecuteForNullSubject() throws IOException {
		String[] ss = null;
		try (Closeable c = CloseableWrapper.of(ss, s -> {
			throw new IOException();
		})) {}
	}

}
