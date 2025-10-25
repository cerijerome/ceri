package ceri.log.registry;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class RegistryBehavior {

	@Test
	public void shouldProvideNullInstance() {
		Registry.NULL.queue(_ -> {});
		Registry.NULL.queue("x", _ -> {});
		Registry.NULL.accept(_ -> {});
		assertEquals(Registry.NULL.apply(_ -> 1), 1);
		Registry.NULL.sub("a.b.c").queue(_ -> {});
	}

}
