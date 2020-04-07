package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.test.TestUtil;

public class StreamByteWriterBehavior {

	@Test
	public void shouldCreateImmutableArrayFromByteArrayOutputStream() {
		Immutable bytes = StreamByteWriter.immutable(StreamByteWriter.of().writeIntLsb(0xabcdef));
		assertArray(bytes.copy(0), 0xef, 0xcd, 0xab, 0);
	}

	@Test
	public void shouldCreateMutableArrayFromByteArrayOutputStream() {
		Mutable bytes = StreamByteWriter.mutable(StreamByteWriter.of().writeIntLsb(0xabcdef));
		assertArray(bytes.copy(0), 0xef, 0xcd, 0xab, 0);
	}

	@Test
	public void shouldWriteByte() {
		assertBytes(StreamByteWriter.of().writeByte(-1), 0xff);
	}

	@Test
	public void shouldFillBytes() {
		assertBytes(StreamByteWriter.of().fill(3, 0xff), 0xff, 0xff, 0xff);
	}

	@Test
	public void shouldWriteFromByteArray() {
		byte[] bytes = ArrayUtil.bytes(1, 2, 3, 4, 5);
		assertBytes(StreamByteWriter.of().writeFrom(bytes, 2), 3, 4, 5);
	}

	@Test
	public void shouldWriteFromByteProvider() {
		Immutable bytes = Immutable.wrap(1, 2, 3, 4, 5);
		assertBytes(StreamByteWriter.of().writeFrom(bytes, 2), 3, 4, 5);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		try (InputStream in = TestUtil.inputStream(1, 2, 3)) {
			StreamByteWriter<ByteArrayOutputStream> w = StreamByteWriter.of();
			assertThat(w.transferFrom(in, 2), is(2));
			assertBytes(w, 1, 2);
			assertThat(w.transferFrom(in, 2), is(1));
			assertBytes(w, 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFlushWrites() throws IOException {
		OutputStream out = Mockito.mock(OutputStream.class);
		StreamByteWriter.of(out).flush();
		verify(out).flush();
	}

	private static void assertBytes(StreamByteWriter<ByteArrayOutputStream> w, int... bytes) {
		assertArray(StreamByteWriter.bytes(w), bytes);
	}
}
