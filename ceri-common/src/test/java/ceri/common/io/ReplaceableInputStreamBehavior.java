package ceri.common.io;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.RTX;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestInputStream;

public class ReplaceableInputStreamBehavior {
	private ValueCondition<Exception> sync;
	private TestInputStream in;

	@Before
	public void before() {
		sync = ValueCondition.of();
		in = TestInputStream.of();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceStream() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.replace(in);
			rin.replace(in); // does nothing
			rin.replace(TestInputStream.of());
			in.close.assertCalls(1);
		}
	}

	@Test
	public void shouldNotifyListenerOfMarkException() throws IOException, InterruptedException {
		in.mark.error.setFrom(RTX);
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.errors().listen(sync::signal);
			rin.set(in);
			assertThrown(() -> rin.mark(0));
			assertThrowable(sync.await(), RuntimeException.class);
		}
	}

	@Test
	public void shouldNotifyListenerOfMarkSupportedException()
		throws IOException, InterruptedException {
		in.markSupported.error.setFrom(RTX);
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.errors().listen(sync::signal);
			assertFalse(rin.markSupported());
			rin.set(in);
			assertThrown(rin::markSupported);
			assertThrowable(sync.await(), RuntimeException.class);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyListenersOfErrors() throws IOException, InterruptedException {
		in.read.error.setFrom(ErrorGen.IOX);
		in.to.writeBytes(0, 0);
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			rin.set(in);
			rin.errors().listen(sync::signal);
			assertThrown(rin::read);
			assertThrowable(sync.await(), IOException.class);
			assertThrown(rin::read);
			assertThrowable(sync.await(), IOException.class);
		}
	}

	@Test
	public void shouldFailWithAnInvalidStream() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			assertThrown(rin::read);
			assertThrown(rin::available);
			assertThrown(() -> rin.skip(0));
			rin.mark(4);
			assertThrown(rin::reset);
			byte[] buffer = new byte[100];
			assertThrown(() -> rin.read(buffer));
			assertThrown(() -> rin.read(buffer, 1, 99));
		}
	}

	@Test
	public void shouldPassThroughMarkAndReset() throws IOException {
		try (ReplaceableInputStream rin = new ReplaceableInputStream()) {
			try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
				assertFalse(rin.markSupported());
				rin.set(in);
				assertTrue(rin.markSupported());
				assertEquals(rin.available(), 4);
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
					rin.set(in);
					byte[] buffer = new byte[9];
					rin.read(buffer);
					rin.set(in2);
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
				rin.set(in);
				byte[] buffer = new byte[2];
				assertEquals(rin.read(), (int) 't');
				rin.read(buffer);
				assertArray(buffer, 'e', 's');
				rin.read(buffer, 1, 1);
				assertArray(buffer, 'e', 't');
			}
		}
	}

}
