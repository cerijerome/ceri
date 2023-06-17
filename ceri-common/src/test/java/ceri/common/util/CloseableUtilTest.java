package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.junit.Test;
import ceri.common.test.TestInputStream;

public class CloseableUtilTest {

	@SuppressWarnings("resource")
	@Test
	public void testExecOrClose() throws IOException {
		assertEquals(CloseableUtil.execOrClose(null, InputStream::close), null);
		TestInputStream in = TestInputStream.of();
		assertEquals(CloseableUtil.execOrClose(in, InputStream::close), in);
		in.close.error.setFrom(IOX);
		assertThrown(() -> CloseableUtil.execOrClose(in, InputStream::close));
	}

	@Test
	public void testClose() {
		final StringReader in = new StringReader("0123456789");
		assertFalse(CloseableUtil.close(null));
		assertTrue(CloseableUtil.close(in));
		assertThrown(IOException.class, in::read);
	}

	@Test
	public void testCloseException() {
		@SuppressWarnings("resource")
		Closeable closeable = () -> {
			throw new IOException();
		};
		assertFalse(CloseableUtil.close(closeable));
	}

	@Test
	public void testCloseWithInterrupt() {
		@SuppressWarnings("resource")
		AutoCloseable closeable = () -> {
			throw new InterruptedException();
		};
		assertFalse(CloseableUtil.close(closeable));
		assertTrue(Thread.interrupted());
	}

}
