package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class LazyBehavior {

	@Test
	public void shouldInstantiateOnce() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		assertEquals(c.get(), 3);
		assertEquals(i.intValue(), 4);
		assertEquals(c.get(), 3);
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldInstantiateOnceWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(i.intValue(), 4);
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldInstantiateOnceUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(i.intValue(), 4);
		assertEquals(c.get(i::getAndIncrement), 3);
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnce() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		c.init();
		assertEquals(i.intValue(), 4);
		c.init();
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnceWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		c.init(i::getAndIncrement);
		assertEquals(i.intValue(), 4);
		c.init(i::getAndIncrement);
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnceUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		c.init(i::getAndIncrement);
		assertEquals(i.intValue(), 4);
		c.init(i::getAndIncrement);
		assertEquals(i.intValue(), 4);
	}

	@Test
	public void shouldGetValueWithoutInstantiation() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		assertEquals(c.value(), null);
		assertEquals(i.intValue(), 3);
	}

	@Test
	public void shouldGetValueWithoutInstantiationWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		assertEquals(c.value(), null);
		assertEquals(i.intValue(), 3);
	}

	@Test
	public void shouldGetValueWithoutInstantiationUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		assertEquals(c.value(), null);
		assertEquals(i.intValue(), 3);
	}

	@Test
	public void shouldManuallyInitializeValueType() {
		AtomicInteger i = new AtomicInteger(3);
		var value = Lazy.Value.of(i::getAndIncrement);
		value.init(7);
		assertEquals(value.get(), 7);
		value.init(3);
		assertEquals(value.get(), 7);
	}

	@Test
	public void shouldNotManuallyInitializeValueType() {
		AtomicInteger i = new AtomicInteger(3);
		var value = Lazy.Value.unsafe(i::getAndIncrement);
		assertEquals(value.get(), 3);
		value.init(7);
		assertEquals(value.get(), 3);
	}

	@Test
	public void shouldOverrideValueType() {
		var value = Lazy.Value.of(3);
		assertEquals(value.get(), 3);
		try (var override = value.override(7)) {
			assertEquals(value.get(), 7);
		}
		assertEquals(value.get(), 3);
	}

}
