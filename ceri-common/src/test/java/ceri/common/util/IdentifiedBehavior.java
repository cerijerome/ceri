package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class IdentifiedBehavior {

	@Test
	public void shouldProvideDefaultIntId() {
		var id = new Identified.ByInt() {};
		assertEquals(Identified.ByInt.id(null, null), 0);
		assertEquals(Identified.ByInt.id(id, 100), 100);
		assertEquals(Identified.ByInt.id(id, null), id.id());
	}

	@Test
	public void shouldCompareByInt() {
		assertEquals(id(0).compareTo(id(-1)), 1);
		assertEquals(id(0).compareTo(id(0)), 0);
		assertEquals(id(0).compareTo(id(1)), -1);
	}

	@Test
	public void testIntPredicate() {
		var predicate = Identified.ByInt.by(i -> i > 0);
		assertEquals(predicate.test(null), false);
		assertEquals(predicate.test(id(0)), false);
		assertEquals(predicate.test(id(1)), true);
	}

	@Test
	public void shouldProvideDefaultLongId() {
		var id = new Identified.ByLong() {};
		assertEquals(Identified.ByLong.id(null, null), 0L);
		assertEquals(Identified.ByLong.id(id, 100L), 100L);
		assertEquals(Identified.ByLong.id(id, null), id.id());
	}

	@Test
	public void shouldCompareByLong() {
		assertEquals(id(0L).compareTo(id(-1L)), 1);
		assertEquals(id(0L).compareTo(id(0L)), 0);
		assertEquals(id(0L).compareTo(id(1L)), -1);
	}

	@Test
	public void testLongPredicate() {
		var predicate = Identified.ByLong.by(l -> l > 0L);
		assertEquals(predicate.test(null), false);
		assertEquals(predicate.test(id(0L)), false);
		assertEquals(predicate.test(id(1L)), true);
	}

	private static Identified.ByInt id(int id) {
		return new Identified.ByInt() {
			@Override
			public int id() {
				return id;
			}
		};
	}

	private static Identified.ByLong id(long id) {
		return new Identified.ByLong() {
			@Override
			public long id() {
				return id;
			}
		};
	}
}
