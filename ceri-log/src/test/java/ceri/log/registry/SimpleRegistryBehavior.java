package ceri.log.registry;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class SimpleRegistryBehavior {

	@Test
	public void shouldWriteProperties() {
		var reg = SimpleRegistry.of("test");
		reg.accept(t -> t.setValue(123, "a", "b"));
		assertEquals(reg.properties.get("test.a.b"), "123");
		reg.queue(t -> t.setValue(125, "a", "b"));
		assertEquals(reg.properties.get("test.a.b"), "125");
	}

	@Test
	public void shouldReadProperties() {
		var reg = SimpleRegistry.of("test");
		reg.properties.setProperty("test.a.b", "123");
		assertEquals(reg.apply(t -> t.intValue("a.b")), 123);
	}

	@Test
	public void shouldProvideSubRegistry() {
		var reg = SimpleRegistry.of("test").sub("a");
		assertEquals(reg.prefix(), "test.a");
		reg.accept(t -> t.setValue(123, "b", "c"));
		assertEquals(reg.properties.get("test.a.b.c"), "123");
	}

}
