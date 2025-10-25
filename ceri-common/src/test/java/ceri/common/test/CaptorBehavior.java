package ceri.common.test;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import java.util.List;
import org.junit.Test;

public class CaptorBehavior {

	@Test
	public void shouldCaptureInts() {
		var captor = Captor.ofInt();
		captor.accept(1);
		assertEquals(captor.accept(2, "x"), "x");
		captor.accept(3L);
		captor.apply(null);
		captor.apply(c -> c.verify(1, 2, 3));
		assertArray(captor.ints(), 1, 2, 3);
	}

	@Test
	public void shouldCaptureLongs() {
		var captor = Captor.ofLong();
		captor.accept(1);
		assertEquals(captor.accept(2L, "x"), "x");
		captor.accept(3L);
		captor.apply(c -> c.verify(1L, 2L, 3L));
		assertArray(captor.longs(), 1, 2, 3);
	}

	@Test
	public void shouldCaptureBiValues() {
		var captor = Captor.<Integer, String>ofBi();
		captor.verify();
		captor.accept(1, "a");
		captor.apply(null);
		captor.apply(c -> c.verify(1, "a"));
		captor.accept(2, "b");
		captor.verify(1, "a", 2, "b");
		assertEquals(captor.accept(3, "c", 0.1), 0.1);
		captor.verify(1, "a", 2, "b", 3, "c");
		captor.accept(4, "d");
		captor.verify(1, "a", 2, "b", 3, "c", 4, "d");
		captor.accept(5, "e");
		captor.verify(1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
		captor.verify(List.of(1, 2, 3, 4, 5), List.of("a", "b", "c", "d", "e"));
	}

	@Test
	public void shouldCaptureLists() {
		var captor = Captor.<Integer>ofN();
		captor.acceptAll(1, 2, 3);
		assertOrdered(captor.values, List.of(1, 2, 3));
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
