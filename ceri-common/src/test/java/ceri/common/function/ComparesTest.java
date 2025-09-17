package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Comparator;
import org.junit.Test;

public class ComparesTest {

	@Test
	public void testUnsignedComparators() {
		assertEquals(Compares.UINT.compare(0xffffffff, -1), 0);
		assertEquals(Compares.UINT.compare(0xffffffff, 0), 1);
		assertEquals(Compares.UINT.compare(0, 0xffffffff), -1);
		assertEquals(Compares.UINT.compare(0xffffffff, -2), 1);
		assertEquals(Compares.UINT.compare(-2, 0xffffffff), -1);
		assertEquals(Compares.UINT.compare(0x7fffffff, 0x80000000), -1);
		assertEquals(Compares.UINT.compare(0x80000000, 0x7fffffff), 1);
		assertEquals(Compares.ULONG.compare(0xffffffffffffffffL, -1L), 0);
		assertEquals(Compares.ULONG.compare(0xffffffffffffffffL, 0L), 1);
		assertEquals(Compares.ULONG.compare(0L, 0xffffffffffffffffL), -1);
		assertEquals(Compares.ULONG.compare(0xffffffffffffffffL, -2L), 1);
		assertEquals(Compares.ULONG.compare(-2L, 0xffffffffffffffffL), -1);
		assertEquals(Compares.ULONG.compare(0x7fffffffffffffffL, 0x8000000000000000L), -1);
		assertEquals(Compares.ULONG.compare(0x8000000000000000L, 0x7fffffffffffffffL), 1);
	}

	@Test
	public void testOfNull() {
		assertEquals(Compares.ofNull().compare("A", "B"), 0);
		assertEquals(Compares.ofNull().compare("A", null), 0);
		assertEquals(Compares.ofNull().compare(null, null), 0);
	}

	@Test
	public void testPrimitiveComparator() {
		assertEquals(Compares.BOOL.compare(null, null), 0);
		assertTrue(Compares.BOOL.compare(null, true) < 0);
		assertTrue(Compares.BOOL.compare(null, false) < 0);
		assertTrue(Compares.BOOL.compare(true, null) > 0);
		assertTrue(Compares.BOOL.compare(false, null) > 0);
		assertTrue(Compares.BOOL.compare(false, true) < 0);
		assertTrue(Compares.BOOL.compare(true, false) > 0);
	}

	@Test
	public void testByComparable() {
		Comparator<String> comparator = Compares.comparable();
		assertEquals(comparator.compare(null, null), 0);
		assertTrue(comparator.compare("A", null) > 0);
		assertTrue(comparator.compare(null, "A") < 0);
		assertTrue(comparator.compare("A", "B") < 0);
		assertEquals(comparator.compare("A", "A"), 0);
		assertTrue(comparator.compare("B", "A") > 0);
	}

	@Test
	public void testByString() {
		Comparator<String> comparator = Compares.string();
		assertEquals(comparator.compare(null, null), 0);
		assertTrue(comparator.compare("A", null) > 0);
		assertTrue(comparator.compare(null, "A") < 0);
		assertTrue(comparator.compare("A", "B") < 0);
		assertEquals(comparator.compare("A", "A"), 0);
		assertTrue(comparator.compare("B", "A") > 0);
	}
}
