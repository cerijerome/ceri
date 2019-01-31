package ceri.common.data;

import static ceri.common.data.ByteUtil.toAscii;
import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.BitSet;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.DataEncoder.Encodable;
import ceri.common.data.DataEncoder.EncodableField;

public class DataEncoderBehavior {

	@Test
	public void shouldSliceEncodedData() {
		assertArray(DataEncoder.of(6).encodeAscii("abcdef").slice(3).copy(), 'd', 'e', 'f');
	}

	@Test
	public void shouldSupportMarkAndReset() {
		DataEncoder enc = DataEncoder.of(6).encodeAscii("abc").mark().encodeAscii("def").reset()
			.encodeAscii("DE");
		assertArray(enc.data().copy(), toAscii("abcDEf").copy());
	}

	@Test
	public void shouldSupportSkipAndRewind() {
		DataEncoder enc = DataEncoder.of(6).encodeAscii("abcdef").rewind(5).encodeAscii("BC")
			.skip(1).encodeAscii("E");
		assertArray(enc.data().copy(), toAscii("aBCdEf").copy());
		assertThat(enc.offset(), is(5));
		assertThat(enc.remaining(), is(1));
		assertThat(enc.total(), is(6));
	}

	@Test
	public void shouldEncodeEncodableObjects() {
		DataEncoder enc = DataEncoder.of(1);
		new Encodable() {}.encode(enc);
		assertArray(enc.data().copy(), 0);
		assertArray(new Encodable() {}.encode().copy());
		assertArray(encodableUpperAscii("").encode().copy());
		assertArray(encodableUpperAscii("abc").encode().copy(), 'A', 'B', 'C');
	}

	@Test
	public void shouldEncodePrimitives() {
		byte[] b = DataEncoder.of(16).encodeByte(0xab).encodeShortLsb(0x1234).encodeShortMsb(0x5678)
			.encodeIntLsb(0x12345678).encodeIntMsb(0x9abcdef0).data().copy();
		assertArray(b, 0xab, 0x34, 0x12, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12, 0x9a, 0xbc, 0xde, 0xf0,
			0, 0, 0);
	}

	@Test
	public void shouldEncodeText() {
		byte[] b = DataEncoder.of(8).encodeAscii("\0abc\n").encodeUtf8("\u2205").data().copy();
		assertArray(b, 0x00, 'a', 'b', 'c', '\n', 0xe2, 0x88, 0x85);
	}

	@Test
	public void shouldEncodeBitSets() {
		BitSet bs = BitSet.valueOf(ByteUtil.bytes(0x00, 0x0f, 0xff, 0xf0));
		byte[] b = DataEncoder.of(4).encode(bs).data().copy();
		assertArray(b, 0x00, 0x0f, 0xff, 0xf0);
	}

	@Test
	public void shouldEncodeField() {
		EncodableField f = (data, offset) -> encodeUpperAscii("abc", data, offset);
		byte[] b = DataEncoder.of(3).encode(f).data().copy();
		assertArray(b, 'A', 'B', 'C');
	}

	private int encodeUpperAscii(String s, byte[] data, int offset) {
		return ByteUtil.toAscii(s.toUpperCase()).copyTo(data, offset);
	}

	private Encodable encodableUpperAscii(String s) {
		ImmutableByteArray b = ByteUtil.toAscii(s.toUpperCase());
		return new Encodable() {
			@Override
			public int size() {
				return b.length;
			}

			@Override
			public void encode(DataEncoder encoder) {
				encoder.copy(b);
			}
		};
	}

}
