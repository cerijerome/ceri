package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ConstantBehavior {

	@Test
	public void shouldInstantiateOnce() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Constant.of(i::getAndIncrement);
		assertEquals(c.get(), 3);
		assertEquals(c.get(), 3);
	}

	@Test
	public void shouldInstantiateOnceWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Constant.<RuntimeException, Integer>of();
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(c.get(i::getAndIncrement), 3);
	}

	@Test
	public void shouldInstantiateOnceUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Constant.<RuntimeException, Integer>unsafe();
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(c.get(i::getAndIncrement), 3);
	}

}
