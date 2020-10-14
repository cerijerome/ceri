package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.test.TestUtil;

public class ReplaceableInputStreamBehavior {
	private @Mock Consumer<Exception> listener;
	private @Mock InputStream in;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyListenerOfMarkException() throws IOException {
		RuntimeException ex = new RuntimeException();
		doThrow(ex).when(in).mark(anyInt());
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.listeners().listen(listener);
			rin.setInputStream(in);
			TestUtil.assertThrown(() -> rin.mark(0));
		}
		verify(listener).accept(ex);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyListenerOfMarkSupportedException() throws IOException {
		RuntimeException ex = new RuntimeException();
		doThrow(ex).when(in).markSupported();
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.listeners().listen(listener);
			assertFalse(rin.markSupported());
			rin.setInputStream(in);
			TestUtil.assertThrown(rin::markSupported);
		}
		verify(listener).accept(ex);
	}

	@Test
	public void shouldNotifyListenersOfErrors() throws IOException {
		IOException e0 = new IOException();
		IOException e1 = new IOException();
		when(in.read()).thenThrow(e0, e1);
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.setInputStream(in);
			rin.listeners().listen(listener);
			TestUtil.assertThrown(rin::read);
			TestUtil.assertThrown(rin::read);
			rin.listeners().unlisten(listener);
		}
		verify(listener).accept(e0);
		verify(listener).accept(e1);
	}

	@Test
	public void shouldFailWithAnInvalidStream() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			TestUtil.assertThrown(rin::read);
			TestUtil.assertThrown(rin::available);
			TestUtil.assertThrown(() -> rin.skip(0));
			rin.mark(4);
			TestUtil.assertThrown(rin::reset);
			byte[] buffer = new byte[100];
			TestUtil.assertThrown(() -> rin.read(buffer));
			TestUtil.assertThrown(() -> rin.read(buffer, 1, 99));
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
