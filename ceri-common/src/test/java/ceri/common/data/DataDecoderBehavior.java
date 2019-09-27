package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.test.TestUtil;
import ceri.common.text.Utf8Util;

public class DataDecoderBehavior {

	@Test
	public void shouldDecodeDecodableTypes() {
		ImmutableByteArray b = ByteUtil.toAscii("abcde");
		assertThat(DataDecoder.decode(b, d -> d.decodeAscii(3)), is("abc"));
	}

	@Test
	public void shouldDecodePrimitives() {
		DataDecoder dec = decoder(0x9c, //
			0x12, 0x34, 0x56, 0x78, 0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x10);
		assertThat(dec.decodeByte(), is(0x9c));
		assertThat(dec.decodeShortLsb(), is(0x3412));
		assertThat(dec.decodeShortMsb(), is(0x5678));
		assertThat(dec.decodeIntLsb(), is(0x98badcfe));
		assertThat(dec.decodeIntMsb(), is(0x76543210));
	}

	@Test
	public void shouldDecodeText() {
		byte[] b = new byte[6];
		int i = Utf8Util.encodeTo("\u2205", b, 0);
		ByteUtil.toAscii("abc").copyTo(b, i);
		DataDecoder dec = DataDecoder.of(ImmutableByteArray.wrap(b));
		assertThat(dec.decodeUtf8(i), is("\u2205"));
		assertThat(dec.decodeAscii(dec.remaining()), is("abc"));
	}

	@Test
	public void shouldValidateAscii() {
		ImmutableByteArray b0 = ByteUtil.toAscii("abc");
		ImmutableByteArray b1 = ByteUtil.toAscii("abC");
		DataDecoder dec = DataDecoder.of(b0, 0).mark();
		dec.validateAscii("abc").reset();
		TestUtil.assertThrown(() -> dec.validateAscii("abC"));
		dec.reset().validateAscii(b0).reset();
		TestUtil.assertThrown(() -> dec.validateAscii(b1));
	}

	@Test
	public void shouldValidateBytes() {
		ImmutableByteArray b0 = ByteUtil.toAscii("abcde");
		ImmutableByteArray b1 = ByteUtil.toAscii("bcdef");
		ImmutableByteArray b2 = ByteUtil.toAscii("cde");
		ImmutableByteArray b3 = ByteUtil.toAscii("cdE");
		DataDecoder dec = DataDecoder.of(b0, 1, 4);
		dec.skip(1).validate(b2);
		dec.rewind(4).validate(b1, 0, 3).rewind(2);
		TestUtil.assertThrown(() -> dec.validate(b3));
	}

	@Test
	public void shouldSliceRemainingBytes() {
		DataDecoder dec = decoder(1, 2, 3, 4, 5, 6, 7, 8);
		dec.skip(3);
		assertArray(dec.slice(2).copy(), 4, 5);
		assertArray(dec.slice().copy(), 6, 7, 8);
	}

	@Test
	public void shouldCreateASubArrayDecoder() {
		DataDecoder dec = decoder(1, 2, 3, 4, 5, 6, 7, 8);
		dec.skip(3);
		DataDecoder sub = dec.sub(3);
		assertArray(sub.slice().copy(), 4, 5, 6);
		assertThat(sub.total(), is(3));
		assertThat(dec.offset(), is(6));
	}

	private DataDecoder decoder(int... bytes) {
		return DataDecoder.of(ImmutableByteArray.wrap(bytes));
	}

}
