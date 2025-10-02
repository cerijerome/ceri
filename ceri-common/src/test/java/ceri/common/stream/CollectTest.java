package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class CollectTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Collect.class);
		assertPrivateConstructor(Collect.Ints.class);
		assertPrivateConstructor(Collect.Longs.class);
		assertPrivateConstructor(Collect.Doubles.class);
	}

}
