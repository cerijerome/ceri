package ceri.common.stream;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class CollectTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Collect.class);
		assertPrivateConstructor(Collect.Ints.class);
		assertPrivateConstructor(Collect.Longs.class);
		assertPrivateConstructor(Collect.Doubles.class);
	}

}
