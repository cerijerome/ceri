package ceri.common.io;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class BufferedOutputStreamBehavior {
	private final ByteArrayOutputStream bout = new ByteArrayOutputStream();

	@Before
	public void before() {
		bout.reset();
	}

	@Test
	public void shouldWriteOnFlush() throws IOException {
		try (var out = new BufferedOutputStream(bout)) {
			out.write(bytes(1, 2, 3));
			assertBytes();
			out.flush();
			assertBytes(1, 2, 3);
		}
	}

	@Test
	public void shouldBypassBufferIfLargeWrite() throws IOException {
		try (var out = new BufferedOutputStream(bout, 5)) {
			out.write(bytes(1, 2, 3, 4, 5, 6, 7, 8));
			assertBytes(1, 2, 3, 4, 5, 6, 7, 8);
		}
	}

	@Test
	public void shouldFlushBufferOnSingleWriteIfBufferExceeded() throws IOException {
		try (var out = new BufferedOutputStream(bout, 3)) {
			out.write(1);
			out.write(2);
			out.write(3);
			assertBytes();
			out.write(4);
			assertBytes(1, 2, 3);
		}
	}

	@Test
	public void shouldFlushBufferIfBufferExceeded() throws IOException {
		try (var out = new BufferedOutputStream(bout, 5)) {
			out.write(bytes(1, 2, 3));
			assertBytes();
			out.write(bytes(4, 5, 6));
			assertBytes(1, 2, 3);
		}
	}

	private void assertBytes(int... bytes) {
		assertArray(bout.toByteArray(), bytes);
	}
}
