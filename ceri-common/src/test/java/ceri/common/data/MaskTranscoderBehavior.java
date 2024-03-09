package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class MaskTranscoderBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		MaskTranscoder m0 = MaskTranscoder.xbits(16, 13);
		MaskTranscoder m1 = MaskTranscoder.xbits(16, 13);
		MaskTranscoder m2 = MaskTranscoder.mask(0x1fffe000, 13);
		MaskTranscoder m3 = MaskTranscoder.mask(0x1fffe000L, 13);
		MaskTranscoder m4 = MaskTranscoder.xbits(16, 12);
		MaskTranscoder m5 = MaskTranscoder.xbits(17, 13);
		MaskTranscoder m6 = MaskTranscoder.xbits(16, 0);
		MaskTranscoder m7 = MaskTranscoder.mask(0x1ffff000, 13);
		MaskTranscoder m8 = MaskTranscoder.mask(0x1fffe000, 12);
		exerciseEquals(m0, m1, m2, m3);
		assertAllNotEqual(m0, m4, m5, m6, m7, m8);
	}

	@Test
	public void shouldProvideNullMask() {
		assertEquals(MaskTranscoder.NULL.encode(Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MaskTranscoder.NULL.encode(Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(MaskTranscoder.NULL.encode(-1), -1L);
		assertEquals(MaskTranscoder.NULL.encodeInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MaskTranscoder.NULL.encodeInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(MaskTranscoder.NULL.encodeInt(-1), -1);
		assertEquals(MaskTranscoder.NULL.encodeInt(0xffffffffL), -1);
	}

	@Test
	public void shouldEncodeValues() {
		assertEquals(MaskTranscoder.xbits(8, 4).encode(0xabcd), 0xcd0L);
		assertEquals(MaskTranscoder.xbits(8, 4).encodeInt(0xabcd), 0xcd0);
	}

	@Test
	public void shouldEncodeWithCurrentValues() {
		assertEquals(MaskTranscoder.xbits(8, 4).encode(0xabcd, 0x1234), 0x1cd4L);
		assertEquals(MaskTranscoder.xbits(8, 4).encodeInt(0xabcd, 0x1234), 0x1cd4);
	}

	@Test
	public void shouldDecodeBitShiftMaskValues() {
		assertEquals(MaskTranscoder.xbits(32, 16).decode(0xff_ffffff00L), 0xffffffL);
		assertEquals(MaskTranscoder.xbits(32, 16).decode(0xff_ff000000L), 0xffff00L);
		assertEquals(MaskTranscoder.xbits(17, 47).decode(-1L), 0x1ffffL);
		assertEquals(MaskTranscoder.xbits(17, 15).decode(-1L), 0x1ffffL);
	}

	@Test
	public void shouldDecodeBitShiftMaskValuesAsInt() {
		assertEquals(MaskTranscoder.xbits(32, 16).decodeInt(0xff_ffffff00L), 0xffffff);
		assertEquals(MaskTranscoder.xbits(32, 16).decodeInt(0xff_ff000000L), 0xffff00);
		assertEquals(MaskTranscoder.xbits(17, 47).decodeInt(-1L), 0x1ffff);
		assertEquals(MaskTranscoder.xbits(17, 15).decodeInt(-1L), 0x1ffff);
	}

	@Test
	public void shouldDecodeMaskedValues() {
		assertEquals(MaskTranscoder.mask(0xffff_ffff0000L, 0).decode(0xff_ffffff00L),
			0xff_ffff0000L);
		assertEquals(MaskTranscoder.mask(0xffff_ffff0000L, 16).decode(0xff_ffffff00L), 0xffffffL);
		assertEquals(MaskTranscoder.mask(0xffff8000_00000000L, 47).decode(-1L), 0x1ffffL);
		assertEquals(MaskTranscoder.mask(0xffff8000L, 15).decode(-1L), 0x1ffffL);
		assertEquals(MaskTranscoder.mask(0xffff8000, 15).decode(-1L), 0x1ffffL);
		assertEquals(MaskTranscoder.mask(0xf0f0, 4).decode(0xeeee), 0xe0eL);
	}

	@Test
	public void shouldDecodeMaskedValuesAsInt() {
		assertEquals(MaskTranscoder.mask(0xffff_ffff0000L, 0).decodeInt(0xff_ffffff00L),
			0xffff0000);
		assertEquals(MaskTranscoder.mask(0xffff_ffff0000L, 16).decodeInt(0xff_ffffff00L), 0xffffff);
		assertEquals(MaskTranscoder.mask(0xffff8000_00000000L, 47).decodeInt(-1L), 0x1ffff);
		assertEquals(MaskTranscoder.mask(0xffff8000L, 15).decodeInt(-1L), 0x1ffff);
		assertEquals(MaskTranscoder.mask(0xffff8000, 15).decodeInt(-1L), 0x1ffff);
		assertEquals(MaskTranscoder.mask(0xf0f0, 4).decodeInt(0xeeee), 0xe0e);
	}

}
