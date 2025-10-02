package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class ReduceTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Reduce.class);
		assertPrivateConstructor(Reduce.Ints.class);
		assertPrivateConstructor(Reduce.Longs.class);
		assertPrivateConstructor(Reduce.Doubles.class);
	}

}
