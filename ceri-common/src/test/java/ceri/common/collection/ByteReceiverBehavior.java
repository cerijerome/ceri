package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;

public class ByteReceiverBehavior {

	@Test
	public void shouldCopyBytesFromByteArrays() {
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(r.copyFrom(0xaa, 0xbb, 0xcc), is(3));
		assertArray(b, 0xaa, 0xbb, 0xcc, 0);
		clear(b);
		assertThat(r.copyFrom(ArrayUtil.bytes(0xaa, 0xbb, 0xcc), 1), is(2));
		assertArray(b, 0xbb, 0xcc, 0, 0);
		clear(b);
		assertThat(r.copyFrom(ArrayUtil.bytes(0xaa, 0xbb, 0xcc), 2, 1), is(1));
		assertArray(b, 0xcc, 0, 0, 0);
		clear(b);
		assertThat(r.copyFrom(1, ArrayUtil.bytes(0xaa, 0xbb)), is(3));
		assertArray(b, 0, 0xaa, 0xbb, 0);
	}

	@Test
	public void shouldCopyBytesFromByteProviders() {
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(r.copyFrom(provider(0xaa, 0xbb, 0xcc)), is(3));
		assertArray(b, 0xaa, 0xbb, 0xcc, 0);
		clear(b);
		assertThat(r.copyFrom(provider(0xaa, 0xbb, 0xcc), 1), is(2));
		assertArray(b, 0xbb, 0xcc, 0, 0);
		clear(b);
		assertThat(r.copyFrom(provider(0xaa, 0xbb, 0xcc), 2, 1), is(1));
		assertArray(b, 0xcc, 0, 0, 0);
		clear(b);
		assertThat(r.copyFrom(1, provider(0xaa, 0xbb)), is(3));
		assertArray(b, 0, 0xaa, 0xbb, 0);
		clear(b);
		assertThat(r.copyFrom(1, provider(0xaa, 0xbb, 0xcc), 1), is(3));
		assertArray(b, 0, 0xbb, 0xcc, 0);
		clear(b);
		assertThat(r.copyFrom(1, provider(0xaa, 0xbb, 0xcc), 1, 1), is(2));
		assertArray(b, 0, 0xbb, 0, 0);
	}

	@Test
	public void shouldFillBytes() {
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(r.fill(0xff), is(4));
		assertArray(b, 0xff, 0xff, 0xff, 0xff);
		clear(b);
		assertThat(r.fill(0x77, 2), is(4));
		assertArray(b, 0, 0, 0x77, 0x77);
		clear(b);
		assertThat(r.fill(0x77, 1, 2), is(3));
		assertArray(b, 0, 0x77, 0x77, 0);
	}

	@Test
	public void shouldSetBytesFromInputStream() throws IOException {
		ByteArrayInputStream in =
			new ByteArrayInputStream(ArrayUtil.bytes(0xff, 1, 0x80, 0x7f, 0, 2));
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(r.readFrom(in), is(4));
		assertArray(b, 0xff, 1, 0x80, 0x7f);
		in.reset();
		clear(b);
		assertThat(r.readFrom(in, 3), is(4));
		assertArray(b, 0, 0, 0, 0xff);
		in.reset();
		clear(b);
		assertThat(r.readFrom(in, 1, 2), is(3));
		assertArray(b, 0, 0xff, 1, 0);
	}

	@Test
	public void shouldSetBufferedBytesFromInputStream() throws IOException {
		ByteArrayInputStream in =
			new ByteArrayInputStream(ArrayUtil.bytes(0xff, 1, 0x80, 0x7f, 0, 2));
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(ByteReceiver.readBufferFrom(r, in, 1, 2), is(3));
		assertArray(b, 0, 0xff, 1, 0);
	}

	@Test
	public void shouldShouldStopSettingBytesFromInputStreamEOF() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(0xff, 0x80));
		byte[] b = new byte[4];
		ByteReceiver r = receiver(b);
		assertThat(r.readFrom(in), is(2));
		assertArray(b, 0xff, 0x80, 0, 0);
		in.reset();
		clear(b);
		assertThat(ByteReceiver.readBufferFrom(r, in, 0, 3), is(2));
		assertArray(b, 0xff, 0x80, 0, 0);
		assertThat(ByteReceiver.readBufferFrom(r, in, 0, 3), is(0));
	}

	@Test
	public void shouldWrapByteArrays() throws IOException {
		byte[] b = new byte[4];
		ByteReceiver r = ByteReceiver.wrap(b);
		r.set(1, 0xff);
		assertArray(b, 0, 0xff, 0, 0);
		clear(b);
		assertThat(r.copyFrom(0xaa, 0xbb, 0xcc), is(3));
		assertArray(b, 0xaa, 0xbb, 0xcc, 0);
		clear(b);
		assertThat(r.copyFrom(provider(0xaa, 0xbb, 0xcc)), is(3));
		assertArray(b, 0xaa, 0xbb, 0xcc, 0);
		clear(b);
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(0xff, 0x80));
		assertThat(r.readFrom(in), is(2));
		assertArray(b, 0xff, 0x80, 0, 0);
	}

	private ByteProvider provider(int... values) {
		byte[] bytes = ArrayUtil.bytes(values);
		return new ByteProvider() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public byte get(int index) {
				return bytes[index];
			}
		};
	}

	private ByteReceiver receiver(byte[] bytes) {
		return new ByteReceiver() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public void set(int pos, int b) {
				bytes[pos] = (byte) b;
			}
		};
	}

	private void clear(byte[] array) {
		Arrays.fill(array, (byte) 0);
	}

}
