package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class RegexTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(Regex.class, Regex.Common.class, Regex.Filter.class);
	}

	@Test
	public void testCommonDecodeInt() {
		assertEquals(Regex.Common.decodeInt("0b001001"), 0b001001);
		assertEquals(Regex.Common.decodeInt("0B111"), 0B111);
		assertEquals(Regex.Common.decodeInt("0123"), 0123);
		assertEquals(Regex.Common.decodeInt("0x0123"), 0x123);
		assertEquals(Regex.Common.decodeInt("0X0123"), 0x123);
		assertEquals(Regex.Common.decodeInt("#0123"), 0x123);
		assertEquals(Regex.Common.decodeInt("123"), 123);
		assertEquals(Regex.Common.decodeInt("0"), 0);
	}

	@Test
	public void testCommonDecodeLong() {
		assertEquals(Regex.Common.decodeLong("0b001001"), 0b001001L);
		assertEquals(Regex.Common.decodeLong("0B111"), 0B111L);
		assertEquals(Regex.Common.decodeLong("0123"), 0123L);
		assertEquals(Regex.Common.decodeLong("0x0123"), 0x123L);
		assertEquals(Regex.Common.decodeLong("0X0123"), 0x123L);
		assertEquals(Regex.Common.decodeLong("#0123"), 0x123L);
		assertEquals(Regex.Common.decodeLong("123"), 123L);
		assertEquals(Regex.Common.decodeLong("0"), 0L);
	}

	@Test
	public void testFilterFind() throws Exception {
		assertEquals(Regex.Filter.find(INT_PATTERN).test(null), false);
		assertEquals(Regex.Filter.find("(\\d+)").test("abc123def456"), true);
		assertEquals(Regex.Filter.find(LSTRING_PATTERN).test("abc123def456"), true);
		assertEquals(Regex.Filter.find(USTRING_PATTERN).test("abc123def456"), false);
	}

	@Test
	public void testFilterMatch() throws Exception {
		assertEquals(Regex.Filter.match(INT_PATTERN).test(null), false);
		assertEquals(Regex.Filter.match(INT_PATTERN).test("123"), true);
		assertEquals(Regex.Filter.match("(\\d+)").test("123def456"), false);
		assertEquals(Regex.Filter.match(LSTRING_PATTERN).test("abc"), true);
		assertEquals(Regex.Filter.match(USTRING_PATTERN).test("abc"), false);
	}

	@Test
	public void testFilterMatching() throws Exception {
		assertEquals(Regex.Filter.matching(null, _ -> true).test(""), false);
		assertEquals(Regex.Filter.matching(Regex.ALL, null).test(""), false);
		assertEquals(Regex.Filter.matching(Regex.ALL, Matcher::find).test(null), false);
		assertEquals(Regex.Filter.matching(Regex.ALL, Matcher::find).test("a"), true);
	}
	
	@Test
	public void testSplitLine() {
		assertOrdered(Regex.Split.LINE.list(""));
		assertOrdered(Regex.Split.LINE.list(" "), " ");
		assertOrdered(Regex.Split.LINE.list("\n"));
		assertOrdered(Regex.Split.LINE.list(" \n\t"), " ", "\t");
	}

	@Test
	public void testSplitComma() {
		assertOrdered(Regex.Split.COMMA.list(null));
		assertOrdered(Regex.Split.COMMA.list(""));
		assertOrdered(Regex.Split.COMMA.list(" "), "");
		assertOrdered(Regex.Split.COMMA.list("a"), "a");
		assertOrdered(Regex.Split.COMMA.list(" a "), "a");
		assertOrdered(Regex.Split.COMMA.list(",,a"), "", "", "a");
		assertOrdered(Regex.Split.COMMA.list("a,,"), "a");
		assertOrdered(Regex.Split.COMMA.list(" , a "), "", "a");
		assertOrdered(Regex.Split.COMMA.list("a,b"), "a", "b");
		assertOrdered(Regex.Split.COMMA.list(" a , b "), "a", "b");
	}

	@Test
	public void testSplitSpace() {
		assertOrdered(Regex.Split.SPACE.list(null));
		assertOrdered(Regex.Split.SPACE.list(""));
		assertOrdered(Regex.Split.SPACE.list(" "));
		assertOrdered(Regex.Split.SPACE.list("a"), "a");
		assertOrdered(Regex.Split.SPACE.list(" a b "), "", "a", "b");
	}

	@Test
	public void testChainMatch() {
		var chain = Regex.Chain.of("abc123de45f6");
		var m = chain.matcher("[a-z]+");
		assertEquals(m.find(), true);
		assertEquals(m.group(), "abc");
		assertEquals(m.find(), true);
		assertEquals(m.group(), "de");
		m = chain.matcher("[0-9]+");
		assertEquals(m.find(), true);
		assertEquals(m.group(), "45");
	}

	@Test
	public void testChainIgnorePreviousUnmatched() {
		var chain = Regex.Chain.of("abc123de45f6");
		var m = chain.matcher("[a-z]+");
		m = chain.matcher("[a-z]+");
		assertEquals(m.find(), true);
		assertEquals(m.group(), "abc");
	}
	
	@Test
	public void testHash() {
		var p0 = Pattern.compile("(?m).*");
		var p1 = Pattern.compile("(?m).+");
		var p2 = Pattern.compile("(?m).*", 1);
		assertEquals(Regex.hash(null), Regex.hash(null));
		assertEquals(Regex.hash(p0), Regex.hash(p0));
		assertEquals(Regex.hash(p1), Regex.hash(p1));
		assertEquals(Regex.hash(p2), Regex.hash(p2));
	}

	@Test
	public void testEquals() {
		var p = Pattern.compile("test.*");
		assertEquals(Regex.equals(null, null), true);
		assertEquals(Regex.equals(p, null), false);
		assertEquals(Regex.equals(null, p), false);
		assertEquals(Regex.equals(p, p), true);
		assertEquals(Regex.equals(p, Pattern.compile("test.*")), true);
		assertEquals(Regex.equals(p, Pattern.compile("test.+")), false);
		assertEquals(Regex.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 1)), true);
		assertEquals(Regex.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 2)), false);
	}
	
	@Test
	public void testHasMatch() {
		assertEquals(Regex.hasMatch(null), false);
		assertEquals(Regex.hasMatch(Regex.ALL.matcher("")), false);
		assertEquals(Regex.hasMatch(find(".+", "")), false);
		assertEquals(Regex.hasMatch(find(".*", "")), true);
	}
	
	@Test
	public void testCompile() {
		//assertPattern(Regex.compile((Joiner) null), "");
		assertPattern(Regex.compile(Regex.OR), "(?:)");
		assertPattern(Regex.compile(Regex.OR, "a+", "b+"), "(?:a+|b+)");
	}
	
	private static Matcher find(String pattern, String s) {
		var m = Pattern.compile(pattern).matcher(s);
		m.find();
		return m;
	}
	
	private static void assertPattern(Pattern pattern, String s) {
		assertString(pattern.pattern(), s);
	}
}
