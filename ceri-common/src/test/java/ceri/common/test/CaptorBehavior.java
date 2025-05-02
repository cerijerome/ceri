package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import org.junit.Test;

public class CaptorBehavior {

	@Test
	public void shouldProvideIntArray() {
		var captor = Captor.ofInt();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.ints(), 1, 2, 3);
	}

	@Test
	public void shouldProvideLongArray() {
		var captor = Captor.ofLong();
		captor.accept(1);
		captor.accept(2);
		captor.accept(3);
		assertArray(captor.longs(), 1, 2, 3);
	}

	@Test
	public void shouldReset() {
		var captor = Captor.ofLong();
		captor.accept(1);
		captor.accept(2);
		captor.reset();
		captor.accept(3);
		assertArray(captor.longs(), 3);
	}

	@Test
	public void shouldResetBi() {
		var captor = Captor.<String, Integer>ofBi();
		captor.accept("1", 1);
		captor.accept("2", 2);
		captor.reset();
		captor.accept("3", 3);
		captor.first.verify("3");
		captor.second.verify(3);
	}

}
