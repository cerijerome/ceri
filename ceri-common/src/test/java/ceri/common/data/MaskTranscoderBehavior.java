package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
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
	public void shouldEncodeValues() {
		assertThat(MaskTranscoder.xbits(8, 4).encode(0xabcd), is(0xcd0L));
		assertThat(MaskTranscoder.xbits(8, 4).encodeInt(0xabcd), is(0xcd0));
	}

	@Test
	public void shouldEncodeWithCurrentValues() {
		assertThat(MaskTranscoder.xbits(8, 4).encode(0xabcd, 0x1234), is(0x1cd4L));
		assertThat(MaskTranscoder.xbits(8, 4).encodeInt(0xabcd, 0x1234), is(0x1cd4));
	}

	@Test
	public void shouldDecodeBitShiftMaskValues() {
		assertThat(MaskTranscoder.xbits(32, 16).decode(0xff_ffffff00L), is(0xffffffL));
		assertThat(MaskTranscoder.xbits(32, 16).decode(0xff_ff000000L), is(0xffff00L));
		assertThat(MaskTranscoder.xbits(17, 47).decode(-1L), is(0x1ffffL));
		assertThat(MaskTranscoder.xbits(17, 15).decode(-1L), is(0x1ffffL));
	}

	@Test
	public void shouldDecodeBitShiftMaskValuesAsInt() {
		assertThat(MaskTranscoder.xbits(32, 16).decodeInt(0xff_ffffff00L), is(0xffffff));
		assertThat(MaskTranscoder.xbits(32, 16).decodeInt(0xff_ff000000L), is(0xffff00));
		assertThat(MaskTranscoder.xbits(17, 47).decodeInt(-1L), is(0x1ffff));
		assertThat(MaskTranscoder.xbits(17, 15).decodeInt(-1L), is(0x1ffff));
	}

	@Test
	public void shouldDecodeMaskedValues() {
		assertThat(MaskTranscoder.mask(0xffff_ffff0000L, 0).decode(0xff_ffffff00L),
			is(0xff_ffff0000L));
		assertThat(MaskTranscoder.mask(0xffff_ffff0000L, 16).decode(0xff_ffffff00L), is(0xffffffL));
		assertThat(MaskTranscoder.mask(0xffff8000_00000000L, 47).decode(-1L), is(0x1ffffL));
		assertThat(MaskTranscoder.mask(0xffff8000L, 15).decode(-1L), is(0x1ffffL));
		assertThat(MaskTranscoder.mask(0xffff8000, 15).decode(-1L), is(0x1ffffL));
		assertThat(MaskTranscoder.mask(0xf0f0, 4).decode(0xeeee), is(0xe0eL));
	}

	@Test
	public void shouldDecodeMaskedValuesAsInt() {
		assertThat(MaskTranscoder.mask(0xffff_ffff0000L, 0).decodeInt(0xff_ffffff00L),
			is(0xffff0000));
		assertThat(MaskTranscoder.mask(0xffff_ffff0000L, 16).decodeInt(0xff_ffffff00L),
			is(0xffffff));
		assertThat(MaskTranscoder.mask(0xffff8000_00000000L, 47).decodeInt(-1L), is(0x1ffff));
		assertThat(MaskTranscoder.mask(0xffff8000L, 15).decodeInt(-1L), is(0x1ffff));
		assertThat(MaskTranscoder.mask(0xffff8000, 15).decodeInt(-1L), is(0x1ffff));
		assertThat(MaskTranscoder.mask(0xf0f0, 4).decodeInt(0xeeee), is(0xe0e));
	}

}
