package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import org.junit.Test;

public class CaptorBehavior {

	@Test
	public void shouldProvideIntArray() {
		Captor.Int captor = Captor.ofInt();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.ints(), 1, 2, 3);
	}

}
