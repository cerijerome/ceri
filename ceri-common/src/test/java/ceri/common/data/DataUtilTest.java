package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;

public class DataUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(DataUtil.class);
	}

	@Test
	public void testValidateType() {
		DataUtil.validate(DataTestType.intValue(255));
		assertException(() -> DataUtil.validate(DataTestType.intValue(0), "test"));
		assertException(() -> DataUtil.validate(DataTestType.intValue(101)));
		DataUtil.validate(DataTestType.shortValue(255));
		assertException(() -> DataUtil.validate(DataTestType.shortValue(0), "test"));
		assertException(() -> DataUtil.validate(DataTestType.shortValue(101)));
		DataUtil.validate(DataTestType.byteValue(255));
		assertException(() -> DataUtil.validate(DataTestType.byteValue(0), "test"));
		assertException(() -> DataUtil.validate(DataTestType.byteValue(101)));
	}

	@Test
	public void testValidatePrimitive() {
		DataUtil.validateInt(-1, 0xffffffff);
		DataUtil.validateInt(Integer.MIN_VALUE, Integer.MIN_VALUE, "test");
		assertException(() -> DataUtil.validateInt(-1, -2));
		DataUtil.validateShort(-1, 0xffff);
		DataUtil.validateShort(Short.MIN_VALUE, Short.MIN_VALUE, "test");
		assertException(() -> DataUtil.validateShort(-1, -2));
		DataUtil.validateByte(-1, 0xff);
		DataUtil.validateByte(Byte.MIN_VALUE, Byte.MIN_VALUE, "test");
		assertException(() -> DataUtil.validateByte(-1, -2));
	}

	@Test
	public void testEncode() {
		byte[] data = new byte[12];
		assertThat(DataUtil.encodeIntLsb(0xfedc1234, data, 0), is(4));
		assertThat(DataUtil.encodeIntMsb(0xfedc1234, data, 4), is(8));
		assertThat(DataUtil.encodeShortLsb(0xa98765, data, 8), is(10));
		assertThat(DataUtil.encodeShortMsb(0xa98765, data, 10), is(12));
		assertArray(data, 0x34, 0x12, 0xdc, 0xfe, 0xfe, 0xdc, 0x12, 0x34, 0x65, 0x87, 0x87, 0x65);
	}

	@Test
	public void testDecode() {
		ImmutableByteArray data = ImmutableByteArray.wrap(0x34, 0x12, 0xdc, 0xfe, 0xfe, 0xdc, 0x12,
			0x34, 0x65, 0x87, 0x87, 0x65);
		assertThat(DataUtil.decodeIntLsb(data, 0), is(0xfedc1234));
		assertThat(DataUtil.decodeIntMsb(data, 4), is(0xfedc1234));
		assertThat(DataUtil.decodeShortLsb(data, 8), is(0x8765));
		assertThat(DataUtil.decodeShortMsb(data, 10), is(0x8765));
	}

}
