package ceri.log.registry;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class SimpleRegistryBehavior {

	@Test
	public void shouldWriteProperties() {
		var reg = SimpleRegistry.of("test");
		reg.accept(t -> t.set(123, "a", "b"));
		assertEquals(reg.properties.get("test.a.b"), "123");
		reg.queue(t -> t.set(125, "a", "b"));
		assertEquals(reg.properties.get("test.a.b"), "125");
	}

	@Test
	public void shouldReadProperties() {
		var reg = SimpleRegistry.of("test");
		reg.properties.setProperty("test.a.b", "123");
		assertEquals(reg.apply(t -> t.parse("a.b").toInt()), 123);
	}

	@Test
	public void shouldProvideSubRegistry() {
		var reg = SimpleRegistry.of("test").sub("a");
		assertEquals(reg.prefix(), "test.a");
		reg.accept(t -> t.set(123, "b", "c"));
		assertEquals(reg.properties.get("test.a.b.c"), "123");
	}

}
