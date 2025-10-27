package ceri.common.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import ceri.common.test.Assert;

public class LazyBehavior {

	@Test
	public void shouldInstantiateOnce() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		Assert.equal(c.get(), 3);
		Assert.equal(i.intValue(), 4);
		Assert.equal(c.get(), 3);
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldInstantiateOnceWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		Assert.equal(c.get(i::getAndIncrement), 3);
		Assert.equal(i.intValue(), 4);
		Assert.equal(c.get(i::getAndIncrement), 3);
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldInstantiateOnceUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		Assert.equal(c.get(i::getAndIncrement), 3);
		Assert.equal(i.intValue(), 4);
		Assert.equal(c.get(i::getAndIncrement), 3);
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnce() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		c.init();
		Assert.equal(i.intValue(), 4);
		c.init();
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnceWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		c.init(i::getAndIncrement);
		Assert.equal(i.intValue(), 4);
		c.init(i::getAndIncrement);
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldInitializeOnceUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		c.init(i::getAndIncrement);
		Assert.equal(i.intValue(), 4);
		c.init(i::getAndIncrement);
		Assert.equal(i.intValue(), 4);
	}

	@Test
	public void shouldGetValueWithoutInstantiation() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.of(i::getAndIncrement);
		Assert.equal(c.value(), null);
		Assert.equal(i.intValue(), 3);
	}

	@Test
	public void shouldGetValueWithoutInstantiationWithSupplier() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>of();
		Assert.equal(c.value(), null);
		Assert.equal(i.intValue(), 3);
	}

	@Test
	public void shouldGetValueWithoutInstantiationUnsafe() {
		AtomicInteger i = new AtomicInteger(3);
		var c = Lazy.<RuntimeException, Integer>unsafe();
		Assert.equal(c.value(), null);
		Assert.equal(i.intValue(), 3);
	}

	@Test
	public void shouldManuallyInitializeValueType() {
		AtomicInteger i = new AtomicInteger(3);
		var value = Lazy.Value.of(i::getAndIncrement);
		value.init(7);
		Assert.equal(value.get(), 7);
		value.init(3);
		Assert.equal(value.get(), 7);
	}

	@Test
	public void shouldNotManuallyInitializeValueType() {
		AtomicInteger i = new AtomicInteger(3);
		var value = Lazy.Value.unsafe(i::getAndIncrement);
		Assert.equal(value.get(), 3);
		value.init(7);
		Assert.equal(value.get(), 3);
	}

	@Test
	public void shouldOverrideValueType() {
		var value = Lazy.Value.of(3);
		Assert.equal(value.get(), 3);
		try (var _ = value.override(7)) {
			Assert.equal(value.get(), 7);
		}
		Assert.equal(value.get(), 3);
	}

}
