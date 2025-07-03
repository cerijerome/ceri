package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class EnablableBehavior {
	private static final Enablable TRUE = () -> true;
	private static final Enablable FALSE = () -> false;

	@Test
	public void testPredicate() {
		assertEquals(Enablable.PREDICATE.test(null), false);
		assertEquals(Enablable.PREDICATE.test(FALSE), false);
		assertEquals(Enablable.PREDICATE.test(TRUE), true);
	}

	@Test
	public void testComparator() {
		assertEquals(Enablable.COMPARATOR.compare(null, null), 0);
		assertEquals(Enablable.COMPARATOR.compare(null, FALSE), 0);
		assertEquals(Enablable.COMPARATOR.compare(null, TRUE), -1);
		assertEquals(Enablable.COMPARATOR.compare(FALSE, null), 0);
		assertEquals(Enablable.COMPARATOR.compare(FALSE, FALSE), 0);
		assertEquals(Enablable.COMPARATOR.compare(FALSE, TRUE), -1);
		assertEquals(Enablable.COMPARATOR.compare(TRUE, null), 1);
		assertEquals(Enablable.COMPARATOR.compare(TRUE, FALSE), 1);
		assertEquals(Enablable.COMPARATOR.compare(TRUE, TRUE), 0);
	}

	@Test
	public void shouldProvideConditionalValue() {
		assertEquals(Enablable.conditional(null, 1, 2), 2);
		assertEquals(Enablable.conditional(() -> false, 1, 2), 2);
		assertEquals(Enablable.conditional(() -> true, 1, 2), 1);
	}
}
