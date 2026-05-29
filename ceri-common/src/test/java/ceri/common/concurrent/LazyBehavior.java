package ceri.common.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.util.Counter;

public class LazyBehavior {

	@Test
	public void testWeakClassValue() {
		Assert.equal(Lazy.weakClassValue(null), null);
		var c = Counter.of(0);
		var cv = Lazy.weakClassValue(_ -> {
			c.inc(1);
			return new Object();
		});
		Assert.equal(cv.get(null), null);
		Assert.equal(cv.get(null, -1), -1);
		Assert.same(cv.get(String.class), cv.get(String.class));
		Assert.equal(c.get(), 1);
		Assert.same(cv.get(String.class), cv.get(String.class));
		Assert.equal(c.get(), 1);
		Testing.gc();
		Assert.same(cv.get(String.class), cv.get(String.class));
		Assert.equal(c.get(), 2);
	}

	@Test
	public void testClassValue() {
		Assert.equal(Lazy.classValue(null), null);
		var cv = Lazy.classValue(_ -> new Object());
		Assert.equal(cv.get(null), null);
		Assert.equal(cv.get(null, -1), -1);
		Assert.same(cv.get(String.class), cv.get(String.class));
	}

	@Test
	public void testClassValueRemove() {
		var cv = Lazy.classValue(_ -> new Object());
		var obj = cv.get(String.class);
		Assert.same(cv.get(String.class), obj);
		cv.remove(String.class);
		Assert.notSame(cv.get(String.class), obj);
	}

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
