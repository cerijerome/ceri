package ceri.common.stream;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import org.junit.Test;
import ceri.common.test.CallSync;

public class ReduceTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Reduce.class);
		assertPrivateConstructor(Reduce.Ints.class);
		assertPrivateConstructor(Reduce.Longs.class);
		assertPrivateConstructor(Reduce.Doubles.class);
	}

}
