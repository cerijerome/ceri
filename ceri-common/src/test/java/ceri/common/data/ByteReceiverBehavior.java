package ceri.common.data;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionConsumer;

public class ByteReceiverBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldReceivePrimitiveValues() {
		assertBytes(2, br -> assertThat(br.setBool(0, true), is(1)), 1, 0);
		assertBytes(2, br -> assertThat(br.setBool(1, false), is(2)), 0, 0);
		assertBytes(1, br -> assertThat(br.setByte(0, -1), is(1)), 0xff);
		assertBytes(3, br -> assertThat(br.setByte(1, 0x7f), is(2)), 0, 0x7f, 0);
		assertBytes(3, br -> assertThat(br.setShort(1, 0x7ff7), is(3)),
			msb ? bytes(0, 0x7f, 0xf7) : bytes(0, 0xf7, 0x7f));
		assertBytes(4, br -> assertThat(br.setInt(0, 0x12345678), is(4)),
			msb ? bytes(0x12, 0x34, 0x56, 0x78) : bytes(0x78, 0x56, 0x34, 0x12));
		assertBytes(8, br -> assertThat(br.setLong(0, 0x1234567890L), is(8)),
			msb ? bytes(0, 0, 0, 0x12, 0x34, 0x56, 0x78, 0x90) :
				bytes(0x90, 0x78, 0x56, 0x34, 0x12, 0, 0, 0));
		assertBytes(4, br -> assertThat(br.setFloat(0, Float.intBitsToFloat(0x12345678)), is(4)),
			msb ? bytes(0x12, 0x34, 0x56, 0x78) : bytes(0x78, 0x56, 0x34, 0x12));
		assertBytes(8,
			br -> assertThat(br.setDouble(0, Double.longBitsToDouble(0x1234567890L)), is(8)),
			msb ? bytes(0, 0, 0, 0x12, 0x34, 0x56, 0x78, 0x90) :
				bytes(0x90, 0x78, 0x56, 0x34, 0x12, 0, 0, 0));
	}

	@Test
	public void shouldReceiveByteAlignedValues() {
		assertBytes(3, br -> assertThat(br.setShortMsb(1, 0x7ff7), is(3)), 0, 0x7f, 0xf7);
		assertBytes(3, br -> assertThat(br.setShortLsb(1, 0x7ff7), is(3)), 0, 0xf7, 0x7f);
		assertBytes(4, br -> assertThat(br.setIntMsb(0, 0x12345678), is(4)), 0x12, 0x34, 0x56,
			0x78);
		assertBytes(4, br -> assertThat(br.setIntLsb(0, 0x12345678), is(4)), 0x78, 0x56, 0x34,
			0x12);
		assertBytes(8, br -> assertThat(br.setLongMsb(0, 0x1234567890L), is(8)), 0, 0, 0, 0x12,
			0x34, 0x56, 0x78, 0x90);
		assertBytes(8, br -> assertThat(br.setLongLsb(0, 0x1234567890L), is(8)), 0x90, 0x78, 0x56,
			0x34, 0x12, 0, 0, 0);
		assertBytes(4, br -> assertThat(br.setFloatMsb(0, Float.intBitsToFloat(0x12345678)), is(4)),
			0x12, 0x34, 0x56, 0x78);
		assertBytes(4, br -> assertThat(br.setFloatLsb(0, Float.intBitsToFloat(0x12345678)), is(4)),
			0x78, 0x56, 0x34, 0x12);
		assertBytes(8,
			br -> assertThat(br.setDoubleMsb(0, Double.longBitsToDouble(0x1234567890L)), is(8)), 0,
			0, 0, 0x12, 0x34, 0x56, 0x78, 0x90);
		assertBytes(8,
			br -> assertThat(br.setDoubleLsb(0, Double.longBitsToDouble(0x1234567890L)), is(8)),
			0x90, 0x78, 0x56, 0x34, 0x12, 0, 0, 0);
	}

	@Test
	public void shouldReceiveEncodedStrings() {
		assertBytes(5, br -> assertThat(br.setAscii(0, "abcde"), is(5)), ascii);
		assertBytes(5, br -> assertThat(br.setUtf8(0, "abcde"), is(5)), utf8);
		assertBytes(5, br -> assertThat(br.setString(0, "abcde"), is(5)), defCset);
	}

	@Test
	public void shouldSliceReceivingByteRange() {
		byte[] bytes = new byte[5];
		ByteReceiver br = receiver(bytes, 0, bytes.length);
		assertThat(br.slice(5).isEmpty(), is(true));
		assertThat(br.slice(4).isEmpty(), is(false));
		assertThat(br.slice(3).setByte(1, 0xff), is(2));
		assertArray(bytes, 0, 0, 0, 0, 0xff);
	}

	@Test
	public void shouldFillBytes() {
		assertBytes(5, br -> assertThat(br.fill(2, 0xff), is(5)), 0, 0, 0xff, 0xff, 0xff);
		assertThrown(() -> receiver(5).fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromByteArray() {
		assertBytes(5, br -> assertThat(br.setBytes(1, 1, 2, 3), is(4)), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).setBytes(4, 1, 2, 3));
	}

	@Test
	public void shouldCopyFromByteProvider() {
		ByteProvider bp = ByteProviderBehavior.provider(1, 2, 3);
		assertBytes(5, br -> assertThat(br.copyFrom(1, bp), is(4)), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).copyFrom(4, bp));
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertBytes(5, br -> assertThat(br.readFrom(1, in), is(4)), 0, 1, 2, 3, 0);
		assertBytes(2, br -> assertThat(br.readFrom(1, in, 0), is(1)), 0, 0);
		in.reset();
		assertBytes(5, br -> assertThat(ByteReceiver.readBufferFrom(br, 1, in, 3), is(4)), 0, 1, 2,
			3, 0);
	}

	private static <E extends Exception> void assertBytes(int size,
		ExceptionConsumer<E, ByteReceiver> action, int... bytes) throws E {
		assertBytes(size, action, ArrayUtil.bytes(bytes));
	}

	/**
	 * Creates a ByteReceiver wrapping a fixed-size byte array, executes the action on the
	 * ByteReceiver, and asserts the bytes in the array.
	 */
	private static <E extends Exception> void assertBytes(int size,
		ExceptionConsumer<E, ByteReceiver> action, byte[] bytes) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.receiver);
		assertArray(holder.bytes, bytes);
	}

	/**
	 * Class to hold a byte array and a simple ByteReceiver wrapper.
	 */
	public static class Holder {
		public final byte[] bytes;
		public final ByteReceiver receiver;

		public static ByteReceiverBehavior.Holder of(int size) {
			byte[] bytes = new byte[size];
			return new Holder(bytes, receiver(bytes));
		}

		private Holder(byte[] bytes, ByteReceiver receiver) {
			this.bytes = bytes;
			this.receiver = receiver;
		}

		public Holder clear() {
			Arrays.fill(bytes, (byte) 0);
			return this;
		}
	}

	private static ByteReceiver receiver(int size) {
		return receiver(new byte[size]);
	}

	/**
	 * Simple ByteReceiver implementation, wrapping bytes.
	 */
	public static ByteReceiver receiver(byte[] bytes) {
		return receiver(bytes, 0, bytes.length);
	}

	/**
	 * Simple ByteReceiver implementation, wrapping bytes.
	 */
	public static ByteReceiver receiver(byte[] bytes, int offset, int length) {
		return new ByteReceiver() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public int setByte(int index, int value) {
				bytes[offset + index] = (byte) value;
				return index + 1;
			}

			@Override
			public ByteReceiver slice(int index, int len) {
				ArrayUtil.validateSlice(length, index, len);
				return receiver(bytes, offset + index, len);
			}
		};
	}
}
