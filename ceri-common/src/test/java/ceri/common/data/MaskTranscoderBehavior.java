package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class MaskTranscoderBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		MaskTranscoder m0 = MaskTranscoder.bits(13, 16);
		MaskTranscoder m1 = MaskTranscoder.bits(13, 16);
		MaskTranscoder m2 = MaskTranscoder.mask(0x1fffe000);
		MaskTranscoder m3 = MaskTranscoder.bits(12, 16);
		MaskTranscoder m4 = MaskTranscoder.bits(13, 17);
		MaskTranscoder m5 = MaskTranscoder.bits(13);
		MaskTranscoder m6 = MaskTranscoder.mask(0x1ffff000);
		MaskTranscoder m7 = MaskTranscoder.shiftBits(13, 16);
		exerciseEquals(m0, m1, m2);
		assertAllNotEqual(m0, m3, m4, m5, m6, m7);
	}

	@Test
	public void shouldDecodeMaskedValues() {
		MaskTranscoder mask = MaskTranscoder.bits(47, 17);
		assertThat(mask.decode(-1L), is(0xffff8000_00000000L));
		assertThat(mask.decode(0xfedcba98_76543210L), is(0xfedc8000_00000000L));
		mask = MaskTranscoder.bits(11, 33);
		assertThat(mask.decode(-1L), is(0x00000fff_fffff800L));
		assertThat(mask.decodeInt(-1L), is(0xfffff800));
		assertThat(mask.decodeInt(0xfedcba98_76543210L), is(0x76543000));
	}

	@Test
	public void shouldDecodeMaskedValuesWithShiftedBits() {
		MaskTranscoder mask = MaskTranscoder.shiftBits(47, 17);
		assertThat(mask.decode(-1L), is(0x1ffffL));
		assertThat(mask.decode(0xf0f0fa98_76543210L), is(0x1e1e1L));
	}

	@Test
	public void shouldEncodeMaskedValues() {
		MaskTranscoder mask = MaskTranscoder.bits(47, 17);
		assertThat(mask.encode(-1L), is(0xffff8000_00000000L));
		assertThat(mask.encode(0xfedcba98_76543210L), is(0xfedc8000_00000000L));
		mask = MaskTranscoder.bits(17, 17);
		assertThat(mask.encode(0xfe_dcba9876L), is(0x2_dcba0000L));
		assertThat(mask.encodeInt(0xfe_dcba9876L), is(0xdcba0000));
	}

	@Test
	public void shouldEncodeMaskedValueIntoCurrentValue() {
		MaskTranscoder mask = MaskTranscoder.bits(26, 8);
		assertThat(mask.encode(0x2_84000000L, 0xff_00ff00ffL), is(0xfe_84ff00ffL));
		assertThat(mask.encodeInt(0x2_84000000L, 0xff_00ff00ffL), is(0x84ff00ff));
	}

	@Test
	public void shouldEncodeMaskedValuesWithShiftedBits() {
		MaskTranscoder mask = MaskTranscoder.shiftBits(47, 17);
		assertThat(mask.encode(-1L), is(0xffff8000_00000000L));
		assertThat(mask.encode(0xfff, 0xff), is(0x7ff8000_000000ffL));
		assertThat(mask.encode(0xfedcba98_76543210L), is(0x19080000_00000000L));
	}
}
