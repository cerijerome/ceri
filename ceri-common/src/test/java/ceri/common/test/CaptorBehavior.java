package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import org.junit.Test;

public class CaptorBehavior {

	@Test
	public void shouldProvideIntArray() {
		Captor.OfInt captor = Captor.ofInt();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.ints(), 1, 2, 3);
	}

	@Test
	public void shouldProvideLongArray() {
		Captor.OfLong captor = Captor.ofLong();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.longs(), 1, 2, 3);
	}

	@Test
	public void shouldReset() {
		Captor.OfLong captor = Captor.ofLong();
		captor.accept(1);
		captor.accept(2);
		captor.reset();
		captor.accept(3);
		assertArray(captor.longs(), 3);
	}

}
