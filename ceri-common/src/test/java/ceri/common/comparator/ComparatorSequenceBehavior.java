package ceri.common.comparator;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Collections;
import java.util.Comparator;
import org.junit.Test;

public class ComparatorSequenceBehavior {
	private static final A A1 = new A(0, 0);
	private static final A A2 = new A(0, 1);
	private static final A A3 = new A(1, 0);
	private static final A A4 = new A(1, 1);

	private static class A {
		public final int i;
		public final int j;

		public A(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}

	private static final Comparator<A> aIComparator =
		(a1, a2) -> Comparators.INT.compare(a1.i, a2.i);

	private static final Comparator<A> aJComparator =
		(a1, a2) -> Comparators.INT.compare(a1.j, a2.j);

	@Test
	public void should() {
		ComparatorSequence<String> comparator = ComparatorSequence.<String>builder().build();
		assertEquals(comparator.comparators(), Collections.emptyList());
		assertEquals(comparator.compare("1", ""), 0);
	}

	@Test
	public void shouldHaveSameBehaviorWithSingleComparator() {
		Comparator<A> comparator = ComparatorSequence.<A>builder().add(aIComparator).build();
		assertTrue(comparator.compare(A1, A2) == 0);
		assertTrue(comparator.compare(A2, A3) < 0);
		assertTrue(comparator.compare(A3, A4) == 0);
		assertTrue(comparator.compare(A4, A1) > 0);
	}

	@Test
	public void shouldCompareUsingComparatorsInSequence() {
		Comparator<A> comparator =
			ComparatorSequence.<A>builder().add(aIComparator, aJComparator).build();
		assertTrue(comparator.compare(A1, A2) < 0);
		assertTrue(comparator.compare(A2, A3) < 0);
		assertTrue(comparator.compare(A3, A4) < 0);
		assertTrue(comparator.compare(A4, A1) > 0);
		comparator = ComparatorSequence.<A>builder().add(aJComparator, aIComparator).build();
		assertTrue(comparator.compare(A1, A2) < 0);
		assertTrue(comparator.compare(A2, A3) > 0);
		assertTrue(comparator.compare(A3, A4) < 0);
		assertTrue(comparator.compare(A4, A1) > 0);
	}

}
