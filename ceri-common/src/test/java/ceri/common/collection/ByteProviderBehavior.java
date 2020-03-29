package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertByte;
import static ceri.common.test.TestUtil.assertStream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.TestUtil;

public class ByteProviderBehavior {

	@Test
	public void shouldProvideByteAtGivenIndex() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertByte(b.getByte(0), 0);
		assertByte(b.getByte(3), 0x7f);
		TestUtil.assertThrown(() -> b.getByte(4));
	}

	@Test
	public void shouldMakeCopies() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertArray(b.copy(), 0, 0xff, 0x80, 0x7f);
		assertArray(b.copy(2), 0x80, 0x7f);
		assertArray(b.copy(1, 2), 0xff, 0x80);
		assertArray(b.copy(2, 0));
	}

	@Test
	public void shouldCopyToByteArrays() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		byte[] a = new byte[3];
		assertThat(b.copyTo(a), is(3));
		assertArray(a, 0, 0xff, 0x80);
		a = new byte[5];
		assertThat(b.copyTo(a, 2, 2), is(2));
		assertArray(a, 0, 0, 0, 0xff, 0);
		clear(a);
		assertThat(b.copyTo(1, a, 0, 3), is(4));
		assertArray(a, 0xff, 0x80, 0x7f, 0, 0);
		clear(a);
		assertThat(b.copyTo(2, a, 2, 2), is(4));
		assertArray(a, 0, 0, 0x80, 0x7f, 0);
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		b.writeTo(out);
		assertArray(out.toByteArray(), 0, 0xff, 0x80, 0x7f);
		out.reset();
		b.writeTo(2, out);
		assertArray(out.toByteArray(), 0x80, 0x7f);
		out.reset();
		b.writeTo(1, out, 1);
		assertArray(out.toByteArray(), 0xff);
	}

	@Test
	public void shouldWrapByteArrays() throws IOException {
		ByteProvider b = ByteArray.Immutable.wrap(0, 0xff, 0x80, 0x7f);
		assertThat(b.length(), is(4));
		assertByte(b.getByte(2), 0x80);
		byte[] a = new byte[5];
		b.copyTo(1, a, 1, 3);
		assertArray(a, 0, 0xff, 0x80, 0x7f, 0);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		b.writeTo(out, 3);
		assertArray(out.toByteArray(), 0, 0xff, 0x80);
	}

	@Test
	public void shouldStreamBytesAsIntegers() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertStream(b.ustream(), 0, 0xff, 0x80, 0x7f);
		assertStream(b.ustream(3), 0x7f);
		assertStream(b.ustream(2, 2), 0x80, 0x7f);
	}

	@Test
	public void shouldDetermineMatchingByteArrays() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertThat(b.matches(0, 0xff, 0x80, 0x7f), is(true));
		assertThat(b.matches(0, 0xff, 0x80), is(true));
		assertThat(b.matches(0, 0xff, 0x80, 0x7f, 0), is(false));

		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xff, 0x80, 0x7f), 1), is(true));
		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xff, 0x80), 1), is(true));
		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xff, 0x80, 0x7f, 0), 1), is(false));

		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xff), 1, 2), is(true));
		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xff, 0), 1, 2), is(true));
		assertThat(b.matches(ArrayUtil.bytes(0x11, 0), 1, 2), is(false));
		assertThat(b.matches(ArrayUtil.bytes(0x11, 0, 0xfe), 1, 2), is(false));

		assertThat(b.matches(1, ArrayUtil.bytes(0xff, 0x80, 0x7f)), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0xff, 0x80)), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0xff, 0x80, 0x7f, 0)), is(false));

		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x80, 0x7f), 1), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x80), 1), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x80, 0x7f, 0), 1), is(false));

		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x80), 1, 2), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x80, 0), 1, 2), is(true));
		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff), 1, 2), is(false));
		assertThat(b.matches(1, ArrayUtil.bytes(0x11, 0xff, 0x81), 1, 2), is(false));

		assertThat(b.matches(0, ArrayUtil.bytes(0), 0, 0), is(true));
		assertThat(b.matches(5, ArrayUtil.bytes(0), 0, 0), is(false));
		assertThat(b.matches(0, ArrayUtil.bytes(0), 2, 0), is(false));
	}

	@Test
	public void shouldDetermineMatchingByteProviders() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertThat(b.matches(provider(0, 0xff, 0x80, 0x7f)), is(true));
		assertThat(b.matches(provider(0, 0xff, 0x80)), is(true));
		assertThat(b.matches(provider(0, 0xff, 0x80, 0x7f, 0)), is(false));

		assertThat(b.matches(provider(0x11, 0, 0xff, 0x80, 0x7f), 1), is(true));
		assertThat(b.matches(provider(0x11, 0, 0xff, 0x80), 1), is(true));
		assertThat(b.matches(provider(0x11, 0, 0xff, 0x80, 0x7f, 0), 1), is(false));

		assertThat(b.matches(provider(0x11, 0, 0xff), 1, 2), is(true));
		assertThat(b.matches(provider(0x11, 0, 0xff, 0), 1, 2), is(true));
		assertThat(b.matches(provider(0x11, 0), 1, 2), is(false));
		assertThat(b.matches(provider(0x11, 0, 0xfe), 1, 2), is(false));

		assertThat(b.matches(1, provider(0xff, 0x80, 0x7f)), is(true));
		assertThat(b.matches(1, provider(0xff, 0x80)), is(true));
		assertThat(b.matches(1, provider(0xff, 0x80, 0x7f, 0)), is(false));

		assertThat(b.matches(1, provider(0x11, 0xff, 0x80, 0x7f), 1), is(true));
		assertThat(b.matches(1, provider(0x11, 0xff, 0x80), 1), is(true));
		assertThat(b.matches(1, provider(0x11, 0xff, 0x80, 0x7f, 0), 1), is(false));

		assertThat(b.matches(1, provider(0x11, 0xff, 0x80), 1, 2), is(true));
		assertThat(b.matches(1, provider(0x11, 0xff, 0x80, 0), 1, 2), is(true));
		assertThat(b.matches(1, provider(0x11, 0xff), 1, 2), is(false));
		assertThat(b.matches(1, provider(0x11, 0xff, 0x81), 1, 2), is(false));

		assertThat(b.matches(0, provider(0), 0, 0), is(true));
		assertThat(b.matches(5, provider(0), 0, 0), is(false));
		assertThat(b.matches(0, provider(0), 2, 0), is(false));
	}

	@Test
	public void shouldDetermineIndexOfMatchingByteArray() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertThat(b.indexOf(0, 0xff, 0x80, 0x7f), is(0));
		assertThat(b.indexOf(0x80, 0x7f), is(2));
		assertThat(b.indexOf(0x80, 0x7f, 0), is(-1));
		assertThat(b.indexOf(0, 0xff, 0x80, 0x7f, 0), is(-1));

		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0xff, 0x80, 0x7f), 1), is(1));
		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0x80, 0x7f), 1), is(2));
		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0xff, 0x80, 0x7f, 0), 1), is(-1));

		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0xff, 0x80, 0), 1, 2), is(1));
		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0xff, 0x80, 0), 1, 3), is(-1));
		assertThat(b.indexOf(ArrayUtil.bytes(0xff, 0xff, 0x80, 0), 1, 4), is(-1));

		assertThat(b.indexOf(1, ArrayUtil.bytes(0xff, 0x80)), is(1));
		assertThat(b.indexOf(2, ArrayUtil.bytes(0xff, 0x80)), is(-1));
	}

	@Test
	public void shouldDetermineIndexOfMatchingByteProvider() {
		ByteProvider b = provider(0, 0xff, 0x80, 0x7f);
		assertThat(b.indexOf(provider(0, 0xff, 0x80, 0x7f)), is(0));
		assertThat(b.indexOf(provider(0x80, 0x7f)), is(2));
		assertThat(b.indexOf(provider(0x80, 0x7f, 0)), is(-1));
		assertThat(b.indexOf(provider(0, 0xff, 0x80, 0x7f, 0)), is(-1));

		assertThat(b.indexOf(provider(0xff, 0xff, 0x80, 0x7f), 1), is(1));
		assertThat(b.indexOf(provider(0xff, 0x80, 0x7f), 1), is(2));
		assertThat(b.indexOf(provider(0xff, 0xff, 0x80, 0x7f, 0), 1), is(-1));

		assertThat(b.indexOf(provider(0xff, 0xff, 0x80, 0), 1, 2), is(1));
		assertThat(b.indexOf(provider(0xff, 0xff, 0x80, 0), 1, 3), is(-1));
		assertThat(b.indexOf(provider(0xff, 0xff, 0x80, 0), 1, 4), is(-1));

		assertThat(b.indexOf(1, provider(0xff, 0x80)), is(1));
		assertThat(b.indexOf(2, provider(0xff, 0x80)), is(-1));
	}

	private ByteProvider provider(int... values) {
		byte[] bytes = ArrayUtil.bytes(values);
		return new ByteProvider() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public byte getByte(int index) {
				return bytes[index];
			}
		};
	}

	private void clear(byte[] array) {
		Arrays.fill(array, (byte) 0);
	}

}
