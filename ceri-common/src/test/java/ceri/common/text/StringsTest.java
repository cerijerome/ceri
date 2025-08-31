package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;

public class StringsTest {
	private static final String nullString = null;
	private static final char char0 = 0;
	
	@Test
	public void testDecimalFormat() {
		assertEquals(Strings.decimalFormat(0).format(0.01), "0");
		assertEquals(Strings.decimalFormat(0).format(0.50001), "1");
		assertEquals(Strings.decimalFormat(2).format(0.5), "0.5");
		assertEquals(Strings.decimalFormat(2).format(10), "10");
		assertEquals(Strings.decimalFormat(2).format(10.011), "10.01");
	}

	@Test
	public void testReverse() {
		assertEquals(Strings.reverse(null), "");
		assertEquals(Strings.reverse(""), "");
		assertEquals(Strings.reverse("\u0000\u00ff\uffff"), "\uffff\u00ff\u0000");
	}

	@Test
	public void testRepeat() {
		assertEquals(Strings.repeat('\0', -1), "");
		assertEquals(Strings.repeat('\0', 0), "");
		assertEquals(Strings.repeat('\0', 1), "\0");
		assertEquals(Strings.repeat('\0', 3), "\0\0\0");
		assertEquals(Strings.repeat(null, -1), "");
		assertEquals(Strings.repeat(null, 0), "");
		assertEquals(Strings.repeat("", 3), "");
		assertEquals(Strings.repeat("abc", 1), "abc");
		assertEquals(Strings.repeat("abc", 3), "abcabcabc");
	}

	@Test
	public void testIsNameBoundary() {
		assertEquals(Strings.isNameBoundary(null, 0), false);
		assertEquals(Strings.isNameBoundary("", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 1), false);
		assertEquals(Strings.isNameBoundary("abc", 2), false);
		assertEquals(Strings.isNameBoundary("abc", 3), true);
		assertEquals(Strings.isNameBoundary("abCde", 1), false);
		assertEquals(Strings.isNameBoundary("abCde", 2), true);
		assertEquals(Strings.isNameBoundary("abCde", 3), false);
		assertEquals(Strings.isNameBoundary("ab123", 1), false);
		assertEquals(Strings.isNameBoundary("ab123", 2), true);
		assertEquals(Strings.isNameBoundary("ab123", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 1), false);
		assertEquals(Strings.isNameBoundary("ab__de", 2), true);
		assertEquals(Strings.isNameBoundary("ab__de", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 4), true);
	}

	@Test
	public void testCompact() {
		assertEquals(Strings.compact(null), "");
		assertEquals(Strings.compact(""), "");
		assertEquals(Strings.compact("  \t\rt\r   \ne s\tt"), "t e s t");
	}

	@Test
	public void testCompactFloatingPoint() {
		assertEquals(Strings.compact(0.15f), "0.15");
		assertEquals(Strings.compact(100.0f), "100");
		assertEquals(Strings.compact(1.2), "1.2");
		assertEquals(Strings.compact(11.0), "11");
		assertEquals(Strings.compact(0.1555555555, 3), "0.156");
		assertEquals(Strings.compact(0.9999, 3), "1");
	}

	@Test
	public void testPrinted() {
		assertEquals(Strings.printed(out -> out.print("test")), "test");
	}
}
