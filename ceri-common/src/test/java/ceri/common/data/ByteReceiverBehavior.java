package ceri.common.data;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionConsumer;

public class ByteReceiverBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final byte[] ascii = "abcde".getBytes(ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	/* ByteReceiver tests */

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertEquals(ByteReceiver.empty().length(), 0);
		assertThrown(() -> ByteReceiver.empty().setByte(0, 0));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(receiver(3).isEmpty());
		assertTrue(receiver(0).isEmpty());
		assertTrue(ByteReceiver.empty().isEmpty());
	}

	@Test
	public void shouldReceivePrimitiveValues() {
		assertBytes(2, br -> assertEquals(br.setBool(0, true), 1), 1, 0);
		assertBytes(2, br -> assertEquals(br.setBool(1, false), 2), 0, 0);
		assertBytes(1, br -> assertEquals(br.setByte(0, -1), 1), 0xff);
		assertBytes(3, br -> assertEquals(br.setByte(1, 0x7f), 2), 0, 0x7f, 0);
		assertBytes(3, br -> assertEquals(br.setShort(1, 0x7ff7), 3),
			msb ? bytes(0, 0x7f, 0xf7) : bytes(0, 0xf7, 0x7f));
		assertBytes(4, br -> assertEquals(br.setInt(0, 0x12345678), 4),
			msb ? bytes(0x12, 0x34, 0x56, 0x78) : bytes(0x78, 0x56, 0x34, 0x12));
		assertBytes(8, br -> assertEquals(br.setLong(0, 0x1234567890L), 8),
			msb ? bytes(0, 0, 0, 0x12, 0x34, 0x56, 0x78, 0x90) :
				bytes(0x90, 0x78, 0x56, 0x34, 0x12, 0, 0, 0));
		assertBytes(4, br -> assertEquals(br.setFloat(0, Float.intBitsToFloat(0x12345678)), 4),
			msb ? bytes(0x12, 0x34, 0x56, 0x78) : bytes(0x78, 0x56, 0x34, 0x12));
		assertBytes(8,
			br -> assertEquals(br.setDouble(0, Double.longBitsToDouble(0x1234567890L)), 8),
			msb ? bytes(0, 0, 0, 0x12, 0x34, 0x56, 0x78, 0x90) :
				bytes(0x90, 0x78, 0x56, 0x34, 0x12, 0, 0, 0));
	}

	@Test
	public void shouldReceiveByteAlignedValues() {
		assertBytes(3, br -> assertEquals(br.setShortMsb(1, 0x7ff7), 3), 0, 0x7f, 0xf7);
		assertBytes(3, br -> assertEquals(br.setShortLsb(1, 0x7ff7), 3), 0, 0xf7, 0x7f);
		assertBytes(4, br -> assertEquals(br.setIntMsb(0, 0x12345678), 4), 0x12, 0x34, 0x56, 0x78);
		assertBytes(4, br -> assertEquals(br.setIntLsb(0, 0x12345678), 4), 0x78, 0x56, 0x34, 0x12);
		assertBytes(8, br -> assertEquals(br.setLongMsb(0, 0x1234567890L), 8), 0, 0, 0, 0x12, 0x34,
			0x56, 0x78, 0x90);
		assertBytes(8, br -> assertEquals(br.setLongLsb(0, 0x1234567890L), 8), 0x90, 0x78, 0x56,
			0x34, 0x12, 0, 0, 0);
		assertBytes(4, br -> assertEquals(br.setFloatMsb(0, Float.intBitsToFloat(0x12345678)), 4),
			0x12, 0x34, 0x56, 0x78);
		assertBytes(4, br -> assertEquals(br.setFloatLsb(0, Float.intBitsToFloat(0x12345678)), 4),
			0x78, 0x56, 0x34, 0x12);
		assertBytes(8,
			br -> assertEquals(br.setDoubleMsb(0, Double.longBitsToDouble(0x1234567890L)), 8), 0, 0,
			0, 0x12, 0x34, 0x56, 0x78, 0x90);
		assertBytes(8,
			br -> assertEquals(br.setDoubleLsb(0, Double.longBitsToDouble(0x1234567890L)), 8), 0x90,
			0x78, 0x56, 0x34, 0x12, 0, 0, 0);
	}

	@Test
	public void shouldReceiveEncodedStrings() {
		assertBytes(5, br -> assertEquals(br.setAscii(0, "abcde"), 5), ascii);
		assertBytes(5, br -> assertEquals(br.setUtf8(0, "abcde"), 5), utf8);
		assertBytes(5, br -> assertEquals(br.setString(0, "abcde"), 5), defCset);
	}

	@Test
	public void shouldSliceReceivingByteRange() {
		byte[] bytes = new byte[5];
		ByteReceiver br = receiver(bytes, 0, bytes.length);
		assertTrue(br.slice(5).isEmpty());
		assertTrue(br.slice(4, 0).isEmpty());
		assertEquals(br.slice(0), br);
		assertThrown(() -> br.slice(1, 4));
		assertThrown(() -> br.slice(0, 4));
	}

	@Test
	public void shouldFillBytes() {
		assertBytes(5, br -> assertEquals(br.fill(2, 0xff), 5), 0, 0, 0xff, 0xff, 0xff);
		assertThrown(() -> receiver(5).fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromByteArray() {
		assertBytes(5, br -> assertEquals(br.setBytes(1, 1, 2, 3), 4), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).setBytes(4, 1, 2, 3));
	}

	@Test
	public void shouldCopyFromByteProvider() {
		ByteProvider bp = ByteProviderBehavior.provider(1, 2, 3);
		assertBytes(5, br -> assertEquals(br.copyFrom(1, bp), 4), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).copyFrom(4, bp));
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertBytes(5, br -> assertEquals(br.readFrom(1, in), 4), 0, 1, 2, 3, 0);
		assertBytes(2, br -> assertEquals(br.readFrom(1, in, 0), 1), 0, 0);
		in.reset();
		assertBytes(5, br -> assertEquals(ByteReceiver.readBufferFrom(br, 1, in, 3), 4), 0, 1, 2, 3,
			0);
	}

	@Test
	public void shouldProvideWriterAccessToBytes() {
		assertBytes(5, br -> br.writer(3).fill(0xff), 0, 0, 0, 0xff, 0xff);
		assertBytes(5, br -> br.writer(3, 0).fill(0xff), 0, 0, 0, 0, 0);
		assertBytes(5, br -> br.writer(5).fill(0xff), 0, 0, 0, 0, 0);
		assertThrown(() -> receiver(5).writer(6));
		assertThrown(() -> receiver(5).writer(1, 5));
		assertThrown(() -> receiver(5).writer(-1));
	}

	/* ByteReceiver.Writer tests */

	@Test
	public void shouldWriteByte() {
		assertBytes(3, br -> br.writer(0).writeByte(1).writeByte(2), 1, 2, 0);
		assertThrown(() -> receiver(3).writer(1, 0).writeByte(2));
	}

	@Test
	public void shouldReadEndian() {
		assertBytes(5, br -> br.writer(0).writeEndian(0xfedcba, 3, true), 0xfe, 0xdc, 0xba, 0, 0);
		assertBytes(5, br -> br.writer(2).writeEndian(0xfedcba, 3, false), 0, 0, 0xba, 0xdc, 0xfe);
	}

	@Test
	public void shouldWriteString() {
		assertBytes(5, br -> br.writer(0).writeString("abc", UTF_8), 'a', 'b', 'c', 0, 0);
	}

	@Test
	public void shouldWriteFromByteArray() {
		byte[] bytes = ArrayUtil.bytes(1, 2, 3, 4, 5);
		assertBytes(5, br -> br.writer(1).writeFrom(bytes, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldWriteFromByteProvider() {
		ByteProvider bp = ByteArray.Immutable.wrap(1, 2, 3, 4, 5);
		assertBytes(5, br -> br.writer(1).writeFrom(bp, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertBytes(5, br -> assertEquals(br.writer(1).transferFrom(in), 3), 0, 1, 2, 3, 0);
	}

	@Test
	public void shouldSkipBytes() {
		assertBytes(5, br -> br.writer(1).skip(2).fill(1), 0, 0, 0, 1, 1);
	}

	@Test
	public void shouldReturnWriterByteProvider() {
		ByteReceiver br = receiver(ArrayUtil.bytes(1, 2, 3, 4, 5));
		assertEquals(br.writer(0).receiver(), br);
		assertTrue(br.writer(5, 0).receiver().isEmpty());
		assertThrown(() -> br.writer(2).receiver());
	}

	@Test
	public void shouldSliceWriter() {
		assertBytes(5, br -> br.writer(2).slice().fill(1), 0, 0, 1, 1, 1);
		assertBytes(5, br -> br.writer(2).slice(2).fill(1), 0, 0, 1, 1, 0);
		assertThrown(() -> receiver(5).writer(2).slice(4));
		assertThrown(() -> receiver(5).writer(2).slice(-1));
	}

	/* Support methods */

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
		};
	}
}
