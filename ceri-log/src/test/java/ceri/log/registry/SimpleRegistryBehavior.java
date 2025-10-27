package ceri.log.registry;

import org.junit.Test;
import ceri.common.test.Assert;

public class SimpleRegistryBehavior {

	@Test
	public void shouldWriteProperties() {
		var reg = SimpleRegistry.of("test");
		reg.accept(t -> t.set(123, "a", "b"));
		Assert.equal(reg.properties.get("test.a.b"), "123");
		reg.queue(t -> t.set(125, "a", "b"));
		Assert.equal(reg.properties.get("test.a.b"), "125");
	}

	@Test
	public void shouldReadProperties() {
		var reg = SimpleRegistry.of("test");
		reg.properties.setProperty("test.a.b", "123");
		Assert.equal(reg.apply(t -> t.parse("a.b").toInt()), 123);
	}

	@Test
	public void shouldProvideSubRegistry() {
		var reg = SimpleRegistry.of("test").sub("a");
		Assert.equal(reg.prefix(), "test.a");
		reg.accept(t -> t.set(123, "b", "c"));
		Assert.equal(reg.properties.get("test.a.b.c"), "123");
	}

}
