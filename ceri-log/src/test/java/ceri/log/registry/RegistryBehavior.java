package ceri.log.registry;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class RegistryBehavior {

	@Test
	public void shouldProvideNullInstance() {
		Registry.NULL.queue(p -> {});
		Registry.NULL.queue("x", p -> {});
		Registry.NULL.accept(p -> {});
		assertEquals(Registry.NULL.apply(p -> 1), 1);
		Registry.NULL.sub("a.b.c").queue(p -> {});
	}

}
