package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.test.TestUtil;

public class DataUtilTest {

	enum E {
		a(1),
		b(2),
		c(3);

		public static final TypeTranscoder<E> xcoder =
			TypeTranscoder.of(t -> t.value, E.class);
		public final int value;

		E(int value) {
			this.value = value;
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(DataUtil.class);
	}

	@Test
	public void testValidateAscii() {
		ImmutableByteArray data = ByteUtil.toAscii("test");
		DataUtil.validateAscii(s -> s.equals("test"), data);
		assertThat(DataUtil.validateAscii("test", data), is(4));
		assertThat(DataUtil.validateAscii("tes", data), is(3));
		assertThat(DataUtil.validateAscii("st", data, 2), is(4));
		TestUtil.assertThrown(() -> DataUtil.validateAscii("st\0", data, 2));
		TestUtil.assertThrown(() -> DataUtil.validateAscii("s\0", data, 2));
	}

	@Test
	public void testValidateByteArray() {
		ImmutableByteArray d0 = ByteUtil.toAscii("test");
		ImmutableByteArray d1 = ByteUtil.toAscii("test");
		ImmutableByteArray d2 = ByteUtil.toAscii("\0test");
		ImmutableByteArray d3 = ByteUtil.toAscii("\0tests");
		ImmutableByteArray d4 = ByteUtil.toAscii("\0tesT");
		assertThat(DataUtil.validate(d0, d1), is(4));
		assertThat(DataUtil.validate(d0, d2, 1), is(5));
		assertThat(DataUtil.validate(d0, d3, 1), is(5));
		TestUtil.assertThrown(() -> DataUtil.validate(d0, d4, 1));
	}

	@Test
	public void testValidateType() {
		DataUtil.validate(DataTestType.intValue(255));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.intValue(0), "test"));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.intValue(101)));
		DataUtil.validate(DataTestType.shortValue(255));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.shortValue(0), "test"));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.shortValue(101)));
		DataUtil.validate(DataTestType.byteValue(255));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.byteValue(0), "test"));
		TestUtil.assertThrown(() -> DataUtil.validate(DataTestType.byteValue(101)));
	}

	@Test
	public void testValidatePrimitive() {
		DataUtil.validateInt(-1, 0xffffffff);
		DataUtil.validateInt(Integer.MIN_VALUE, Integer.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> DataUtil.validateInt(-1, -2));
		DataUtil.validateShort(-1, 0xffff);
		DataUtil.validateShort(Short.MIN_VALUE, Short.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> DataUtil.validateShort(-1, -2));
		DataUtil.validateByte(-1, 0xff);
		DataUtil.validateByte(Byte.MIN_VALUE, Byte.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> DataUtil.validateByte(-1, -2));
	}

	@Test
	public void testValidateFromIntFunction() {
		DataUtil.validate(E.xcoder::decode, 1);
		TestUtil.assertThrown(() -> DataUtil.validate(E.xcoder::decode, 4));
		DataUtil.validate(E.xcoder::decode, E.b, 2);
		TestUtil.assertThrown(() -> DataUtil.validate(E.xcoder::decode, E.c, 2));
	}

	@Test
	public void testValidateByteRange() {
		DataUtil.validateUnsignedByteRange(0);
		DataUtil.validateUnsignedByteRange(0xff);
		TestUtil.assertThrown(() -> DataUtil.validateUnsignedByteRange(0x100));
		TestUtil.assertThrown(() -> DataUtil.validateUnsignedByteRange(-1));
	}

	@Test
	public void testValidateShortRange() {
		DataUtil.validateUnsignedShortRange(0);
		DataUtil.validateUnsignedShortRange(0xffff);
		TestUtil.assertThrown(() -> DataUtil.validateUnsignedShortRange(0x10000));
		TestUtil.assertThrown(() -> DataUtil.validateUnsignedShortRange(-1));
	}

	@Test
	public void testEncode() {
		byte[] data = new byte[13];
		assertThat(DataUtil.encodeIntLsb(0xfedc1234, data, 0), is(4));
		assertThat(DataUtil.encodeIntMsb(0xfedc1234, data, 4), is(8));
		assertThat(DataUtil.encodeShortLsb(0xa98765, data, 8), is(10));
		assertThat(DataUtil.encodeShortMsb(0xa98765, data, 10), is(12));
		assertThat(DataUtil.encodeByte(0xabcde, data, 12), is(13));
		assertArray(data, //
			0x34, 0x12, 0xdc, 0xfe, 0xfe, 0xdc, 0x12, 0x34, 0x65, 0x87, 0x87, 0x65, 0xde);
	}

	@Test
	public void testDecode() {
		ImmutableByteArray data = ImmutableByteArray.wrap(0x34, 0x12, 0xdc, 0xfe, 0xfe, 0xdc, 0x12,
			0x34, 0x65, 0x87, 0x87, 0x65, 0xde);
		assertThat(DataUtil.decodeIntLsb(data, 0), is(0xfedc1234));
		assertThat(DataUtil.decodeIntMsb(data, 4), is(0xfedc1234));
		assertThat(DataUtil.decodeShortLsb(data, 8), is(0x8765));
		assertThat(DataUtil.decodeShortMsb(data, 10), is(0x8765));
		assertThat(DataUtil.decodeByte(data, 12), is(0xde));
	}

}
