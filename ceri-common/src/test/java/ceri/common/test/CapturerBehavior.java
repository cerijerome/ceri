package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;

public class CapturerBehavior {

	@Test
	public void shouldProvideIntArray() {
		Capturer.Int captor = Capturer.ofInt();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.ints(), 1, 2, 3);
	}

}
