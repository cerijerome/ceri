package ceri.common.comparator;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

public class ComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Comparators.class);
	}

	@Test
	public void testUnsignedComparators() {
		assertEquals(Comparators.UINT.compare(0xffffffff, -1), 0);
		assertEquals(Comparators.UINT.compare(0xffffffff, 0), 1);
		assertEquals(Comparators.UINT.compare(0, 0xffffffff), -1);
		assertEquals(Comparators.UINT.compare(0xffffffff, -2), 1);
		assertEquals(Comparators.UINT.compare(-2, 0xffffffff), -1);
		assertEquals(Comparators.UINT.compare(0x7fffffff, 0x80000000), -1);
		assertEquals(Comparators.UINT.compare(0x80000000, 0x7fffffff), 1);
		assertEquals(Comparators.ULONG.compare(0xffffffffffffffffL, -1L), 0);
		assertEquals(Comparators.ULONG.compare(0xffffffffffffffffL, 0L), 1);
		assertEquals(Comparators.ULONG.compare(0L, 0xffffffffffffffffL), -1);
		assertEquals(Comparators.ULONG.compare(0xffffffffffffffffL, -2L), 1);
		assertEquals(Comparators.ULONG.compare(-2L, 0xffffffffffffffffL), -1);
		assertEquals(Comparators.ULONG.compare(0x7fffffffffffffffL, 0x8000000000000000L), -1);
		assertEquals(Comparators.ULONG.compare(0x8000000000000000L, 0x7fffffffffffffffL), 1);
	}

	@Test
	public void testOrder() {
		Comparator<Integer> c = Comparators.order(100, 10, 1000);
		assertEquals(c.compare(0, 0), 0);
		assertEquals(c.compare(100, 100), 0);
		assertEquals(c.compare(10, 100), 1);
		assertEquals(c.compare(1000, 100), 1);
		assertEquals(c.compare(10, 1000), -1);
		assertEquals(c.compare(0, 100), 1);
	}

	@Test
	public void testTransform() {
		Comparator<String> c = Comparators.transform(Comparators.INT, String::length);
		assertFalse(c.compare(null, null) > 0);
		assertTrue(c.compare("001", null) > 0);
		assertFalse(c.compare(null, "2") > 0);
		assertTrue(c.compare("001", "2") > 0);
	}

	@Test
	public void testSequence() {
		Comparator<String> comparator =
			Comparators.sequence(Comparators.nonNullComparator(), Comparators.STRING);
		List<String> list = Arrays.asList(null, "2", "1", null);
		list.sort(comparator);
		assertOrdered(list, null, null, "1", "2");
	}

	@Test
	public void testGroupComparator() {
		Comparator<Integer> comparator = Comparators.group(Comparators.INT, 3, 4, 5);
		assertEquals(comparator.compare(1, 2), -1);
		assertEquals(comparator.compare(1, 6), -1);
		assertEquals(comparator.compare(6, 1), 1);
		assertEquals(comparator.compare(7, 6), 1);
		assertEquals(comparator.compare(1, 3), 1);
		assertEquals(comparator.compare(7, 3), 1);
		assertEquals(comparator.compare(3, 2), -1);
		assertEquals(comparator.compare(3, 5), -1);
		assertEquals(comparator.compare(5, 4), 1);
	}

	@Test
	public void testPrimitiveComparator() {
		assertEquals(Comparators.BOOL.compare(null, null), 0);
		assertTrue(Comparators.BOOL.compare(null, true) < 0);
		assertTrue(Comparators.BOOL.compare(null, false) < 0);
		assertTrue(Comparators.BOOL.compare(true, null) > 0);
		assertTrue(Comparators.BOOL.compare(false, null) > 0);
		assertTrue(Comparators.BOOL.compare(false, true) < 0);
		assertTrue(Comparators.BOOL.compare(true, false) > 0);
	}

	@Test
	public void testByComparable() {
		Comparator<String> comparator = Comparators.comparable();
		assertEquals(comparator.compare(null, null), 0);
		assertTrue(comparator.compare("A", null) > 0);
		assertTrue(comparator.compare(null, "A") < 0);
		assertTrue(comparator.compare("A", "B") < 0);
		assertEquals(comparator.compare("A", "A"), 0);
		assertTrue(comparator.compare("B", "A") > 0);
	}

	@Test
	public void testByString() {
		Comparator<String> comparator = Comparators.string();
		assertEquals(comparator.compare(null, null), 0);
		assertTrue(comparator.compare("A", null) > 0);
		assertTrue(comparator.compare(null, "A") < 0);
		assertTrue(comparator.compare("A", "B") < 0);
		assertEquals(comparator.compare("A", "A"), 0);
		assertTrue(comparator.compare("B", "A") > 0);
	}

	@Test
	public void testNullComparator() {
		assertEquals(Comparators.nullComparator().compare("A", "B"), 0);
		assertEquals(Comparators.nullComparator().compare("A", null), 0);
		assertEquals(Comparators.nullComparator().compare(null, null), 0);
	}

	@Test
	public void testNonNullComparator() {
		assertEquals(Comparators.nonNull(null).compare("A", "B"), 0);
		assertEquals(Comparators.nonNullComparator().compare("A", "B"), 0);
		assertTrue(Comparators.nonNullComparator().compare("A", null) > 0);
		assertTrue(Comparators.nonNullComparator().compare(null, "A") < 0);
		assertEquals(Comparators.nonNullComparator().compare(null, null), 0);
	}

	@Test
	public void testReverse() {
		Comparator<Integer> comparator = Comparators.comparable();
		Comparator<Integer> reverseComparator = Comparators.reverse(comparator);
		assertEquals(reverseComparator.compare(0, 0), -comparator.compare(0, 0));
		assertEquals(reverseComparator.compare(0, 1), -comparator.compare(0, 1));
		assertEquals(reverseComparator.compare(1, 0), -comparator.compare(1, 0));
		assertEquals(reverseComparator.compare(1, 1), -comparator.compare(1, 1));
	}

}
