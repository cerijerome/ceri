package ceri.common.io;

import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.Mockito;

public class ReplaceableInputStreamBehavior {

	@Test
	public void shouldNotifyListenersOfErrors() throws IOException {
		List<String> list = new ArrayList<>();
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.read()).thenThrow(new IOException("1"), new IOException("2"));
			try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
				rin.setInputStream(in);
				Consumer<Exception> consumer = e -> list.add(e.getMessage());
				rin.listen(consumer);
				assertException(() -> rin.read());
				assertException(() -> rin.read());
				rin.unlisten(consumer);
				assertIterable(list, "1", "2");
			}
		}
	}

	@Test
	public void shouldFailWithAnInvalidStream() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			assertException(() -> rin.read());
			assertException(() -> rin.available());
			assertException(() -> rin.skip(0));
			rin.mark(4);
			assertException(() -> rin.reset());
			byte[] buffer = new byte[100];
			assertException(() -> rin.read(buffer));
			assertException(() -> rin.read(buffer, 1, 99));
		}
	}

	@Test
	public void shouldPassThroughMarkAndReset() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
				assertFalse(rin.markSupported());
				rin.setInputStream(in);
				assertTrue(rin.markSupported());
				assertThat(rin.available(), is(4));
				byte[] buffer = new byte[6];
				rin.read(buffer, 0, 2);
				rin.mark(2);
				rin.read(buffer, 2, 2);
				rin.reset();
				rin.skip(1);
				rin.read(buffer, 4, 2);
				assertArray(buffer, "testt\0".getBytes());
			}
		}
	}

	@Test
	public void shouldAllowInputStreamToBeReplaced() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
				try (InputStream in2 = new ByteArrayInputStream("again".getBytes())) {
					rin.setInputStream(in);
					byte[] buffer = new byte[9];
					rin.read(buffer);
					rin.setInputStream(in2);
					rin.read(buffer, 4, 5);
					assertArray(buffer, "testagain".getBytes());
				}
			}
		}
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
				rin.setInputStream(in);
				byte[] buffer = new byte[2];
				assertThat(rin.read(), is((int) 't'));
				rin.read(buffer);
				assertArray(buffer, 'e', 's');
				rin.read(buffer, 1, 1);
				assertArray(buffer, 'e', 't');
			}
		}
	}

}
