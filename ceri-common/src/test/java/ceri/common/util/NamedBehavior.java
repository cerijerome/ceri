package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import org.junit.Test;

public class NamedBehavior {

	@Test
	public void shouldProvideDefaultName() {
		var named = new Named() {};
		assertFind(named.name(), "%s\\.1@", getClass().getSimpleName());
	}

	@Test
	public void testName() {
		assertEquals(Named.name("test", "abc"), "abc");
		assertFind(Named.name("test", null), "String@");
	}

	@Test
	public void testNameOf() {
		assertEquals(Named.nameOf(null), "null");
		assertEquals(Named.nameOf(named(null)), null);
		assertEquals(Named.nameOf(named("test")), "test");
	}

	@Test
	public void testBy() {
		var predicate = Named.by(s -> s.length() > 1);
		assertEquals(predicate.test(named("")), false);
		assertEquals(predicate.test(named("a")), false);
		assertEquals(predicate.test(named("test")), true);
		assertEquals(predicate.test(new Named() {}), true);
	}

	private static Named named(String name) {
		return new Named() {
			@Override
			public String name() {
				return name;
			}
		};
	}
}
