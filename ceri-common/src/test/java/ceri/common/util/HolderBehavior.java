package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class HolderBehavior {

	@Test
	public void shouldInitializeWithOrWithoutValue() {
		Holder<Integer> holder = Holder.of();
		assertNull(holder.get());
		holder = Holder.init(1);
		assertEquals(holder.get(), 1);
	}

	@Test
	public void shouldVerifyValue() {
		Holder<Integer> holder = Holder.of();
		assertNull(holder.get());
		assertThrown(holder::verify);
		holder.set(0);
		assertEquals(holder.get(), 0);
		holder.verify();
	}

}
