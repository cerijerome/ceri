package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class MaskBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Mask m0 = Mask.ofBits(13, 16);
		Mask m1 = Mask.ofBits(13, 16);
		Mask m2 = Mask.ofInt(13, 0x1fffe000);
		Mask m3 = Mask.of(13, 0x1fffe000L);
		Mask m4 = Mask.ofBits(12, 16);
		Mask m5 = Mask.ofBits(13, 17);
		Mask m6 = Mask.ofBits(0, 16);
		Mask m7 = Mask.ofInt(13, 0x1ffff000);
		Mask m8 = Mask.ofInt(12, 0x1fffe000);
		exerciseEquals(m0, m1, m2, m3);
		assertAllNotEqual(m0, m4, m5, m6, m7, m8);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(Mask.NULL, "0xffffffffffffffff");
		assertString(Mask.ofInt(0, 0xabcd), "0xabcd");
		assertString(Mask.ofInt(4, 0xabcd), "0xabcd>>4");
	}

	@Test
	public void shouldProvideNullMask() {
		assertEquals(Mask.NULL.encode(Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(Mask.NULL.encode(Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(Mask.NULL.encode(-1), -1L);
		assertEquals(Mask.NULL.encodeInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(Mask.NULL.encodeInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(Mask.NULL.encodeInt(-1), -1);
		assertEquals(Mask.NULL.encodeInt(0xffffffffL), -1);
	}

	@Test
	public void shouldEncodeValues() {
		assertEquals(Mask.ofBits(4, 8).encode(0xabcd), 0xcd0L);
		assertEquals(Mask.ofBits(4, 8).encodeInt(0xabcd), 0xcd0);
	}

	@Test
	public void shouldEncodeWithCurrentValues() {
		assertEquals(Mask.ofBits(4, 8).encode(0x1234, 0xabcd), 0x1cd4L);
		assertEquals(Mask.ofBits(4, 8).encodeInt(0x1234, 0xabcd), 0x1cd4);
	}

	@Test
	public void shouldDecodeBitShiftMaskValues() {
		assertEquals(Mask.ofBits(16, 32).decode(0xff_ffffff00L), 0xffffffL);
		assertEquals(Mask.ofBits(16, 32).decode(0xff_ff000000L), 0xffff00L);
		assertEquals(Mask.ofBits(47, 17).decode(-1L), 0x1ffffL);
		assertEquals(Mask.ofBits(15, 17).decode(-1L), 0x1ffffL);
	}

	@Test
	public void shouldDecodeBitShiftMaskValuesAsInt() {
		assertEquals(Mask.ofBits(16, 32).decodeInt(0xff_ffffff00L), 0xffffff);
		assertEquals(Mask.ofBits(16, 32).decodeInt(0xff_ff000000L), 0xffff00);
		assertEquals(Mask.ofBits(47, 17).decodeInt(-1L), 0x1ffff);
		assertEquals(Mask.ofBits(15, 17).decodeInt(-1L), 0x1ffff);
	}

	@Test
	public void shouldDecodeMaskedValues() {
		assertEquals(Mask.of(0, 0xffff_ffff0000L).decode(0xff_ffffff00L),
			0xff_ffff0000L);
		assertEquals(Mask.of(16, 0xffff_ffff0000L).decode(0xff_ffffff00L), 0xffffffL);
		assertEquals(Mask.of(47, 0xffff8000_00000000L).decode(-1L), 0x1ffffL);
		assertEquals(Mask.of(15, 0xffff8000L).decode(-1L), 0x1ffffL);
		assertEquals(Mask.ofInt(15, 0xffff8000).decode(-1L), 0x1ffffL);
		assertEquals(Mask.ofInt(4, 0xf0f0).decode(0xeeee), 0xe0eL);
	}

	@Test
	public void shouldDecodeMaskedValuesAsInt() {
		assertEquals(Mask.of(0, 0xffff_ffff0000L).decodeInt(0xff_ffffff00L),
			0xffff0000);
		assertEquals(Mask.of(16, 0xffff_ffff0000L).decodeInt(0xff_ffffff00L), 0xffffff);
		assertEquals(Mask.of(47, 0xffff8000_00000000L).decodeInt(-1L), 0x1ffff);
		assertEquals(Mask.of(15, 0xffff8000L).decodeInt(-1L), 0x1ffff);
		assertEquals(Mask.ofInt(15, 0xffff8000).decodeInt(-1L), 0x1ffff);
		assertEquals(Mask.ofInt(4, 0xf0f0).decodeInt(0xeeee), 0xe0e);
	}

}
