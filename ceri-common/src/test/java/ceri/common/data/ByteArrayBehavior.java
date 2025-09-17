package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertBuffer;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.reflect.Reflect;
import ceri.common.test.TestUtil;

public class ByteArrayBehavior {

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(ByteProvider.of().toString(), "[]");
		assertEquals(ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8, 9).toString(),
			"[0x01,0x02,0x03,0x04,0x05,0x06,0x07,...](9)");
		assertEquals(ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8).toString(),
			"[0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08]");
	}

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		var t = ByteArray.Immutable.wrap(1, 2, 3);
		var eq0 = ByteArray.Immutable.wrap(1, 2, 3);
		var eq1 = ByteArray.Immutable.copyOf(ArrayUtil.bytes.of(1, 2, 3));
		var eq2 = ByteArray.Immutable.copyOf(ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		var ne0 = ByteArray.Immutable.wrap(1, 2, 4);
		var ne1 = ByteArray.Immutable.wrap(1, 2, 3, 0);
		var ne2 = ByteArray.Immutable.wrap();
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		var im = ByteArray.Immutable.copyOf(bytes);
		bytes[1] = 0;
		assertByte(im.getByte(1), 2);
	}

	@Test
	public void shouldCreateImmutableByteWrapper() {
		assertTrue(ByteArray.Immutable.wrap(ArrayUtil.bytes.of(1, 2, 3)).isEqualTo(0, 1, 2, 3));
		assertTrue(ByteArray.Immutable.wrap(ArrayUtil.bytes.of(1, 2, 3), 3).isEmpty());
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		var im = ByteArray.Immutable.wrap(bytes);
		bytes[1] = 0;
		assertByte(im.getByte(1), 0);
	}

	@Test
	public void shouldCreateImmutableSlice() {
		assertTrue(ByteArray.Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty());
		assertTrue(ByteArray.Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2));
		assertTrue(ByteArray.Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5));
		var im = ByteArray.Immutable.wrap(1, 2, 3);
		assertTrue(im.slice(0) == im);
	}

	/* Mutable tests */

	@Test
	public void shouldNotBreachMutableEqualsContract() {
		var t = ByteArray.Mutable.of(3);
		var eq0 = ByteArray.Mutable.of(3);
		var eq1 = ByteArray.Mutable.wrap(new byte[3]);
		var eq2 = ByteArray.Mutable.wrap(new byte[5], 1, 3);
		var eq3 = ByteArray.Mutable.wrap(0, 0, 0);
		var ne0 = ByteArray.Mutable.of(4);
		var ne1 = ByteArray.Mutable.wrap(0, 0, 1);
		var ne2 = ByteArray.Mutable.wrap();
		TestUtil.exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateMutableCopy() {
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		var m = ByteArray.Mutable.copyOf(bytes);
		bytes[1] = 0;
		assertByte(m.getByte(1), 2);
		m.setByte(0, 3);
		assertByte(bytes[0], 1);
	}

	@Test
	public void shouldProvideAnImmutableView() {
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		var m = ByteArray.Mutable.wrap(bytes);
		assertArray(m.asImmutable().copy(0), 1, 2, 3);
		assertNull(Reflect.castOrNull(ByteReceiver.class, m.asImmutable()));
		m.setByte(0, -1);
		assertArray(m.asImmutable().copy(0), -1, 2, 3);
	}

	@Test
	public void shouldCreateMutableSlice() {
		var m = ByteArray.Mutable.wrap(1, 2, 3, 4, 5);
		assertTrue(m.slice(5).isEmpty());
		assertTrue(m.slice(0, 2).isEqualTo(0, 1, 2));
		assertTrue(m.slice(5, -2).isEqualTo(0, 4, 5));
		assertTrue(m.slice(0) == m);
	}

	@Test
	public void shouldSetByte() {
		var m = ByteArray.Mutable.of(3);
		assertEquals(m.setByte(1, 0xff), 2);
		assertTrue(m.isEqualTo(0, 0, 0xff, 0));
	}

	@Test
	public void shouldSetEndianBytes() {
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		var m = ByteArray.Mutable.wrap(bytes);
		assertEquals(m.setEndian(1, 2, 0x1234, true), 3);
		assertArray(bytes, 1, 0x12, 0x34);
		assertEquals(m.setEndian(1, 2, 0x1234, false), 3);
		assertArray(bytes, 1, 0x34, 0x12);
	}

	@Test
	public void shouldFillBytes() {
		var m = ByteArray.Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.fill(1, 2, 0xff), 3);
		assertTrue(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5));
		assertThrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		var m = ByteArray.Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.setBytes(3, -4, -5), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, ArrayUtil.bytes.of(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ArrayUtil.bytes.of(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromByteProvider() {
		var m = ByteArray.Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.copyFrom(3, ByteArray.Immutable.wrap(-4, -5)), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, ByteArray.Immutable.wrap(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ByteArray.Immutable.wrap(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		var m = ByteArray.Mutable.of(5);
		var in = new ByteArrayInputStream(ArrayUtil.bytes.of(1, 2, 3));
		assertEquals(m.readFrom(2, in, 2), 4);
		assertTrue(m.isEqualTo(0, 0, 0, 1, 2, 0));
	}

	@Test
	public void shouldCreateMutableBuffer() {
		var b = ByteArray.Mutable.wrap(1, 2, 3, 4, 5);
		assertBuffer(b.toBuffer(1, 3), 2, 3, 4);
		assertBuffer(b.toBuffer(3), 4, 5);
	}

	/* ByteArray base tests */

	@Test
	public void shouldGetEndianBytes() {
		assertEquals(ByteArray.Immutable.wrap(0, 0x7f, 0x80, 0).getEndian(1, 2, true), 0x7f80L);
		assertEquals(ByteArray.Immutable.wrap(0, 0x7f, 0x80, 0).getEndian(1, 2, false), 0x807fL);
		assertThrown(() -> ByteArray.Immutable.wrap(0, 0x7f, 0x80).getEndian(1, 3, false));
	}

	@Test
	public void shouldGetString() {
		assertEquals(ByteArray.Immutable.wrap("abcde".getBytes()).getString(0), "abcde");
		assertThrown(() -> ByteArray.Immutable.wrap("abcde".getBytes()).getString(3, 10));
	}

	@Test
	public void shouldCopyBytes() {
		assertArray(ByteProvider.of().copy(0));
		assertArray(ByteProvider.of(1, 2, 3).copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCopyToByteReceiver() {
		var m = ByteArray.Mutable.of(3);
		assertEquals(ByteArray.Immutable.wrap(1, 2, 3).copyTo(1, m, 1), 3);
		assertTrue(m.isEqualTo(0, 0, 2, 3));
		assertThrown(() -> ByteArray.Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		var out = new ByteArrayOutputStream();
		assertEquals(ByteArray.Immutable.wrap(1, 2, 3).writeTo(1, out, 0), 1);
		assertArray(out.toByteArray());
		assertEquals(ByteArray.Immutable.wrap(1, 2, 3).writeTo(1, out), 3);
		assertArray(out.toByteArray(), 2, 3);
		assertThrown(() -> ByteArray.Immutable.wrap(0, 1, 2).writeTo(0, out, 4));
	}

	@Test
	public void shouldCreateBuffer() {
		var b = ByteArray.Immutable.wrap(1, 2, 3, 4, 5);
		assertBuffer(b.toBuffer(1, 3), 2, 3, 4);
		assertBuffer(b.toBuffer(3), 4, 5);
	}

	@Test
	public void shouldDetermineIfEqualToBytes() {
		assertTrue(
			ByteArray.Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.bytes.of(2, 3, 4)));
		assertFalse(ByteArray.Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.bytes.of(1, 2)));
		assertFalse(ByteArray.Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.bytes.of(1, 2), 0, 3));
	}

	@Test
	public void shouldDetermineIfEqualToProviderBytes() {
		assertTrue(ByteArray.Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1,
			ByteArray.Immutable.wrap(2, 3, 4)));
		assertFalse(ByteArray.Immutable.wrap(1, 2, 3).isEqualTo(2, ByteArray.Immutable.wrap(1, 2)));
		assertFalse(
			ByteArray.Immutable.wrap(1, 2, 3).isEqualTo(0, ByteArray.Immutable.wrap(1, 2), 0, 3));
	}

	/* Encoder tests */

	@Test
	public void shouldEncodeToMinimumSizedArray() {
		assertArray(ByteArray.Encoder.of().bytes());
		assertArray(ByteArray.Encoder.of(5).bytes(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEncodeFixedSize() {
		var en = ByteArray.Encoder.fixed(3);
		en.fill(3, 1);
		assertThrown(() -> en.writeByte(1));
		assertArray(en.bytes(), 1, 1, 1);
	}

	@Test
	public void shouldEncodeToArray() {
		byte[] array = new byte[5];
		assertThrown(() -> ByteArray.Encoder.of(array, 6));
		ByteArray.Encoder.of(array).writeBytes(1, 2, 3);
		assertArray(array, 1, 2, 3, 0, 0);
	}

	@Test
	public void shouldEncodeAsByteArrayWrappers() {
		assertArray(ByteArray.Encoder.of().writeUtf8("abc").bytes(), 'a', 'b', 'c');
		assertArray(ByteArray.Encoder.of().writeUtf8("abc").mutable().copy(0), 'a', 'b', 'c');
		assertArray(ByteArray.Encoder.of().writeUtf8("abc").immutable().copy(0), 'a', 'b', 'c');
	}

	@Test
	public void shouldEncodeAndReadByte() {
		assertEquals(ByteArray.Encoder.of().writeByte(-1).skip(-1).readByte(), (byte) -1);
	}

	@Test
	public void shouldEncodeAndReadEndian() {
		assertEquals(
			ByteArray.Encoder.of().writeEndian(0xfedcba, 3, true).skip(-3).readEndian(3, true),
			0xfedcbaL);
		assertEquals(
			ByteArray.Encoder.of().writeEndian(0xfedcba, 3, false).skip(-3).readEndian(3, false),
			0xfedcbaL);
	}

	@Test
	public void shouldEncodeAndReadString() {
		String s = "abc";
		byte[] bytes = s.getBytes(Charset.defaultCharset());
		int n = bytes.length;
		assertEquals(ByteArray.Encoder.of().writeString(s).skip(-n).readString(n), s);
		assertArray(ByteArray.Encoder.of().writeString(s).bytes(), bytes);
	}

	@Test
	public void shouldEncodeAndReadBytes() {
		assertArray(ByteArray.Encoder.of().writeBytes(1, 2, 3).skip(-3).readBytes(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeAndReadFromIntoByteArray() {
		byte[] bin = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		byte[] bout = new byte[3];
		ByteArray.Encoder.of().writeFrom(bin, 1, 3).skip(-3).readInto(bout, 1, 2);
		assertArray(bout, 0, 2, 3);
	}

	@Test
	public void shouldEncodeToAndReadIntoByteAccessor() {
		var m = ByteArray.Mutable.wrap(1, 2, 3, 0, 0);
		ByteArray.Encoder.of().writeFrom(m, 0, 3).skip(-3).readInto(m, 2);
		assertArray(m.copy(0), 1, 2, 1, 2, 3);
	}

	@Test
	public void shouldEncodeFillBytes() {
		assertArray(ByteArray.Encoder.of().fill(3, -1).skip(2).bytes(), -1, -1, -1, 0, 0);
	}

	@Test
	public void shouldEncodeToOutputStreams() throws IOException {
		var out = new ByteArrayOutputStream();
		assertEquals(ByteArray.Encoder.of().writeBytes(1, 2, 3).skip(-3).transferTo(out, 3), 3);
		assertArray(out.toByteArray(), 1, 2, 3);
	}

	@Test
	public void shouldEncodeFromInputStream() throws IOException {
		var in = new ByteArrayInputStream(ArrayUtil.bytes.of(1, 2, 3));
		var en = ByteArray.Encoder.of();
		assertEquals(en.transferFrom(in, 5), 3);
		assertArray(en.bytes(), 1, 2, 3);
	}

	@Test
	public void shouldEncodeToStream() {
		assertStream(ByteArray.Encoder.of().writeBytes(1, 2, 3).offset(0).ustream(3), 1, 2, 3);
	}

	@Test
	public void shouldNotGrowEncoderIfReading() {
		var en = ByteArray.Encoder.of();
		en.writeBytes(1, 2, 3).skip(-2);
		assertThrown(() -> en.readBytes(3));
		assertThrown(() -> en.readBytes(Integer.MAX_VALUE));
	}

	@Test
	public void shouldFailToGrowEncoderAtomically() {
		var en = ByteArray.Encoder.of(0, 3).writeBytes(1);
		assertThrown(() -> en.writeBytes(1, 2, 3));
		assertThrown(() -> en.fill(3, 0xff));
		assertThrown(() -> en.skip(3));
		var in = new ByteArrayInputStream(ArrayUtil.bytes.of(1, 2, 3));
		assertThrown(() -> en.transferFrom(in, 3));
		en.writeBytes(1, 2);
	}

	@Test
	public void shouldNotGrowEncoderPastMax() {
		var en0 = ByteArray.Encoder.of(0, 3).writeBytes(1);
		assertThrown(() -> en0.writeBytes(1, 2, 3));
		assertThrown(() -> en0.fill(Integer.MAX_VALUE, 0xff));
		var en1 = ByteArray.Encoder.of(3, 5);
		en1.writeBytes(1, 2, 3, 4);
	}

	@Test
	public void shouldNotDoubleEncoderSizePastMax() { // each growth x2 size, but <= max
		var en = ByteArray.Encoder.of(3, 5);
		assertArray(en.writeBytes(1, 2, 3, 4).bytes(), 1, 2, 3, 4);
	}

	@Test
	public void shouldNotGrowEncoderLessThanDefaultSize() { // grow to minimum of 32 (default size)
		var en = ByteArray.Encoder.of(20);
		en.fill(21, 0xff); // grow to 20x2 = 40
		en.fill(60, 1); // grow to 81 (> 40x2)
	}

	/* Encodable tests */

	@Test
	public void shouldReturnEmptyArrayForZeroSizeEncodable() {
		assertArray(encodable(() -> 0, _ -> throwRuntime()).encode());
	}

	@Test
	public void shouldEncodeFixedSizeAsEncodable() {
		assertArray(encodable(() -> 3, enc -> enc.writeBytes(1, 2, 3)).encode(), 1, 2, 3);
	}

	@Test
	public void shouldFailEncodingIfSizeDoesNotMatchBytesAsEncodable() {
		assertThrown(() -> encodable(() -> 2, enc -> enc.writeBytes(1, 2, 3)).encode());
		assertThrown(() -> encodable(() -> 4, enc -> enc.writeBytes(1, 2, 3)).encode());
	}

	private static ByteArray.Encodable encodable(IntSupplier sizeFn,
		Consumer<ByteArray.Encoder> encodeFn) {
		return new ByteArray.Encodable() {
			@Override
			public int size() {
				return sizeFn.getAsInt();
			}

			@Override
			public void encode(ByteArray.Encoder encoder) {
				encodeFn.accept(encoder);
			}
		};
	}
}
