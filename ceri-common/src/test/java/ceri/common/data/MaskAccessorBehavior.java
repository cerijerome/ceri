package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class MaskAccessorBehavior {

	@Test
	public void shouldOnlyAccessMaskedBits() {
		int[] ii = new int[] { 0xabcdef };
		IntAccessor accessor = IntAccessor.of(() -> ii[0], i -> ii[0] = i);
		MaskAccessor mask = MaskAccessor.of(accessor, 0xff00);
		assertThat(mask.get(), is(0xcd00));
		mask.set(0x1234);
		assertThat(ii[0], is(0xab12ef));
	}

	@Test
	public void shouldAccessShiftedMaskedBits() {
		int[] ii = new int[] { 0xabcdef };
		IntAccessor accessor = IntAccessor.of(() -> ii[0], i -> ii[0] = i);
		MaskAccessor mask = MaskAccessor.of(accessor, 0xff00, 8);
		assertThat(mask.get(), is(0xcd));
		mask.set(0x1234);
		assertThat(ii[0], is(0xab34ef));
	}

}
