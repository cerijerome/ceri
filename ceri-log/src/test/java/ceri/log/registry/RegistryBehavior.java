package ceri.log.registry;

import org.junit.Test;
import ceri.common.test.Assert;

public class RegistryBehavior {

	@Test
	public void shouldProvideNullInstance() {
		Registry.NULL.queue(_ -> {});
		Registry.NULL.queue("x", _ -> {});
		Registry.NULL.accept(_ -> {});
		Assert.equal(Registry.NULL.apply(_ -> 1), 1);
		Registry.NULL.sub("a.b.c").queue(_ -> {});
	}

}
