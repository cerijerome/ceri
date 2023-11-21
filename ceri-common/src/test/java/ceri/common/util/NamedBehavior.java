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

}
