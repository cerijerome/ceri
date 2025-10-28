package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(m0, m1, m2, m3);
		Assert.notEqualAll(m0, m4, m5, m6, m7, m8);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(Mask.NULL, "0xffffffffffffffff");
		Assert.string(Mask.ofInt(0, 0xabcd), "0xabcd");
		Assert.string(Mask.ofInt(4, 0xabcd), "0xabcd>>4");
	}

	@Test
	public void shouldProvideNullMask() {
		Assert.equal(Mask.NULL.encode(Long.MAX_VALUE), Long.MAX_VALUE);
		Assert.equal(Mask.NULL.encode(Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(Mask.NULL.encode(-1), -1L);
		Assert.equal(Mask.NULL.encodeInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
		Assert.equal(Mask.NULL.encodeInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(Mask.NULL.encodeInt(-1), -1);
		Assert.equal(Mask.NULL.encodeInt(0xffffffffL), -1);
	}

	@Test
	public void shouldEncodeValues() {
		Assert.equal(Mask.ofBits(4, 8).encode(0xabcd), 0xcd0L);
		Assert.equal(Mask.ofBits(4, 8).encodeInt(0xabcd), 0xcd0);
	}

	@Test
	public void shouldEncodeWithCurrentValues() {
		Assert.equal(Mask.ofBits(4, 8).encode(0x1234, 0xabcd), 0x1cd4L);
		Assert.equal(Mask.ofBits(4, 8).encodeInt(0x1234, 0xabcd), 0x1cd4);
	}

	@Test
	public void shouldDecodeBitShiftMaskValues() {
		Assert.equal(Mask.ofBits(16, 32).decode(0xff_ffffff00L), 0xffffffL);
		Assert.equal(Mask.ofBits(16, 32).decode(0xff_ff000000L), 0xffff00L);
		Assert.equal(Mask.ofBits(47, 17).decode(-1L), 0x1ffffL);
		Assert.equal(Mask.ofBits(15, 17).decode(-1L), 0x1ffffL);
	}

	@Test
	public void shouldDecodeBitShiftMaskValuesAsInt() {
		Assert.equal(Mask.ofBits(16, 32).decodeInt(0xff_ffffff00L), 0xffffff);
		Assert.equal(Mask.ofBits(16, 32).decodeInt(0xff_ff000000L), 0xffff00);
		Assert.equal(Mask.ofBits(47, 17).decodeInt(-1L), 0x1ffff);
		Assert.equal(Mask.ofBits(15, 17).decodeInt(-1L), 0x1ffff);
	}

	@Test
	public void shouldDecodeMaskedValues() {
		Assert.equal(Mask.of(0, 0xffff_ffff0000L).decode(0xff_ffffff00L), 0xff_ffff0000L);
		Assert.equal(Mask.of(16, 0xffff_ffff0000L).decode(0xff_ffffff00L), 0xffffffL);
		Assert.equal(Mask.of(47, 0xffff8000_00000000L).decode(-1L), 0x1ffffL);
		Assert.equal(Mask.of(15, 0xffff8000L).decode(-1L), 0x1ffffL);
		Assert.equal(Mask.ofInt(15, 0xffff8000).decode(-1L), 0x1ffffL);
		Assert.equal(Mask.ofInt(4, 0xf0f0).decode(0xeeee), 0xe0eL);
	}

	@Test
	public void shouldDecodeMaskedValuesAsInt() {
		Assert.equal(Mask.of(0, 0xffff_ffff0000L).decodeInt(0xff_ffffff00L), 0xffff0000);
		Assert.equal(Mask.of(16, 0xffff_ffff0000L).decodeInt(0xff_ffffff00L), 0xffffff);
		Assert.equal(Mask.of(47, 0xffff8000_00000000L).decodeInt(-1L), 0x1ffff);
		Assert.equal(Mask.of(15, 0xffff8000L).decodeInt(-1L), 0x1ffff);
		Assert.equal(Mask.ofInt(15, 0xffff8000).decodeInt(-1L), 0x1ffff);
		Assert.equal(Mask.ofInt(4, 0xf0f0).decodeInt(0xeeee), 0xe0e);
	}

}
