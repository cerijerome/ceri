package ceri.common.util;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class HolderBehavior {

	@Test
	public void shouldInitializeWithOrWithoutValue() {
		Holder<Integer> holder = Holder.init();
		assertNull(holder.get());
		holder = Holder.init(1);
		assertThat(holder.get(), is(1));
	}

	@Test
	public void shouldVerifyValue() {
		Holder<Integer> holder = Holder.init();
		assertNull(holder.get());
		assertException(() -> holder.verify());
		holder.set(0);
		assertThat(holder.get(), is(0));
		holder.verify();
	}

}
