package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.io.IoStreamUtil.ByteReader;
import ceri.common.io.IoStreamUtil.ByteWriter;

public class IoStreamUtilTest {

	@Test
	public void testInClosed() throws IOException {
		try (InputStream in = IoStreamUtil.in((buffer, offset, len) -> -1)) {
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void testInAvailable() throws IOException {
		try (InputStream in = IoStreamUtil.in(reader(-3, -1, 1, 3))) {
			assertThat(in.available(), is(0));
		}
		try (InputStream in = IoStreamUtil.in(reader(-3, -1, 1, 3), () -> 4)) {
			assertThat(in.available(), is(4));
		}
	}

	@Test
	public void testIn() throws IOException {
		try (InputStream in = IoStreamUtil.in(reader(-3, -1, 1, 3))) {
			assertThat(in.read(), is(0xfd));
			byte[] buffer = new byte[5];
			int count = in.read(buffer);
			assertThat(count, is(4));
			assertArray(buffer, -3, -1, 1, 3, 0);
			count = in.read(new byte[1], 0, 0);
			assertThat(count, is(0));
			assertArray(buffer, -3, -1, 1, 3, 0);
		}
	}

	@Test
	public void testOut() throws IOException {
		byte[] buffer = new byte[5];
		try (OutputStream out = IoStreamUtil.out(writer(buffer))) {
			out.write(-1);
			assertArray(buffer, 0xff, 0, 0, 0, 0);
			out.write(new byte[1], 0, 0);
			assertArray(buffer, 0xff, 0, 0, 0, 0);
		}
	}

	/**
	 * Each read starts at the beginning of the array of bytes.
	 */
	private ByteReader reader(int... bytes) {
		return (buffer, offset, len) -> {
			len = Math.min(len, bytes.length);
			for (int i = 0; i < len; i++)
				buffer[offset + i] = (byte) bytes[i];
			return len;
		};
	}

	/**
	 * Each write starts at the beginning of the buffer.
	 */
	private ByteWriter writer(byte[] buffer) {
		return (b, offset, len) -> {
			for (int i = 0; i < len; i++)
				buffer[i] = b[offset + i];
		};
	}

}
