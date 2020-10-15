package ceri.common.util;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class HolderBehavior {

	@Test
	public void shouldInitializeWithOrWithoutValue() {
		Holder<Integer> holder = Holder.of();
		assertNull(holder.get());
		holder = Holder.init(1);
		assertThat(holder.get(), is(1));
	}

	@Test
	public void shouldVerifyValue() {
		Holder<Integer> holder = Holder.of();
		assertNull(holder.get());
		TestUtil.assertThrown(holder::verify);
		holder.set(0);
		assertThat(holder.get(), is(0));
		holder.verify();
	}

}
