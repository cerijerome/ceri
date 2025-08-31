package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.regex.Pattern;
import org.junit.Test;

public class PatternsTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final Pattern MULTI_PATTERN =
		Pattern.compile("(\\w+)\\W+(\\w+)\\W+([\\w\\" + ".]+)");

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(Patterns.class);
	}

	@Test
	public void testCommonDecodeInt() {
		assertEquals(Patterns.Common.decodeInt("0b001001"), 0b001001);
		assertEquals(Patterns.Common.decodeInt("0B111"), 0B111);
		assertEquals(Patterns.Common.decodeInt("0123"), 0123);
		assertEquals(Patterns.Common.decodeInt("0x0123"), 0x123);
		assertEquals(Patterns.Common.decodeInt("0X0123"), 0x123);
		assertEquals(Patterns.Common.decodeInt("#0123"), 0x123);
		assertEquals(Patterns.Common.decodeInt("123"), 123);
		assertEquals(Patterns.Common.decodeInt("0"), 0);
	}

	@Test
	public void testCommonDecodeLong() {
		assertEquals(Patterns.Common.decodeLong("0b001001"), 0b001001L);
		assertEquals(Patterns.Common.decodeLong("0B111"), 0B111L);
		assertEquals(Patterns.Common.decodeLong("0123"), 0123L);
		assertEquals(Patterns.Common.decodeLong("0x0123"), 0x123L);
		assertEquals(Patterns.Common.decodeLong("0X0123"), 0x123L);
		assertEquals(Patterns.Common.decodeLong("#0123"), 0x123L);
		assertEquals(Patterns.Common.decodeLong("123"), 123L);
		assertEquals(Patterns.Common.decodeLong("0"), 0L);
	}

	@Test
	public void testFilterFind() throws Exception {
		assertFalse(Patterns.Filter.find(INT_PATTERN).test(null));
		assertTrue(Patterns.Filter.find("(\\d+)").test("abc123def456"));
		assertTrue(Patterns.Filter.find(LSTRING_PATTERN).test("abc123def456"));
		assertFalse(Patterns.Filter.find(USTRING_PATTERN).test("abc123def456"));
	}

	@Test
	public void testFilterMatch() throws Exception {
		assertFalse(Patterns.Filter.match(INT_PATTERN).test(null));
		assertTrue(Patterns.Filter.match(INT_PATTERN).test("123"));
		assertFalse(Patterns.Filter.match("(\\d+)").test("123def456"));
		assertTrue(Patterns.Filter.match(LSTRING_PATTERN).test("abc"));
		assertFalse(Patterns.Filter.match(USTRING_PATTERN).test("abc"));
	}
	
	@Test
	public void testLines() {
		assertOrdered(Patterns.Split.LINE.list(""));
		assertOrdered(Patterns.Split.LINE.list(" "), " ");
		assertOrdered(Patterns.Split.LINE.list("\n"));
		assertOrdered(Patterns.Split.LINE.list(" \n\t"), " ", "\t");
	}

	@Test
	public void testSplitComma() {
		assertOrdered(Patterns.Split.COMMA.list(null));
		assertOrdered(Patterns.Split.COMMA.list(""));
		assertOrdered(Patterns.Split.COMMA.list(" "), "");
		assertOrdered(Patterns.Split.COMMA.list("a"), "a");
		assertOrdered(Patterns.Split.COMMA.list(" a "), "a");
		assertOrdered(Patterns.Split.COMMA.list(",,a"), "", "", "a");
		assertOrdered(Patterns.Split.COMMA.list("a,,"), "a");
		assertOrdered(Patterns.Split.COMMA.list(" , a "), "", "a");
		assertOrdered(Patterns.Split.COMMA.list("a,b"), "a", "b");
		assertOrdered(Patterns.Split.COMMA.list(" a , b "), "a", "b");
	}

	@Test
	public void testSplitSpace() {
		assertOrdered(Patterns.Split.SPACE.list(null));
		assertOrdered(Patterns.Split.SPACE.list(""));
		assertOrdered(Patterns.Split.SPACE.list(" "));
		assertOrdered(Patterns.Split.SPACE.list("a"), "a");
		assertOrdered(Patterns.Split.SPACE.list(" a b "), "", "a", "b");
	}

	@Test
	public void testHashCode() {
		Pattern p0 = Pattern.compile("(?m).*");
		Pattern p1 = Pattern.compile("(?m).+");
		Pattern p2 = Pattern.compile("(?m).*", 1);
		assertEquals(Patterns.hash(null), Patterns.hash(null));
		assertEquals(Patterns.hash(p0), Patterns.hash(p0));
		assertEquals(Patterns.hash(p1), Patterns.hash(p1));
		assertEquals(Patterns.hash(p2), Patterns.hash(p2));
	}

	@Test
	public void testEquals() {
		Pattern p = Pattern.compile("test.*");
		assertTrue(Patterns.equals(null, null));
		assertFalse(Patterns.equals(p, null));
		assertFalse(Patterns.equals(null, p));
		assertTrue(Patterns.equals(p, p));
		assertTrue(Patterns.equals(p, Pattern.compile("test.*")));
		assertFalse(Patterns.equals(p, Pattern.compile("test.+")));
		assertTrue(Patterns.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 1)));
		assertFalse(Patterns.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 2)));
	}

}
