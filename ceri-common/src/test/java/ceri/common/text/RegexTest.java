package ceri.common.text;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertString;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class RegexTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final String nullStr = null;

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
	public void testFilterPredicateFind() throws Exception {
		assertEquals(Regex.Filter.FIND.test(null), false);
		assertEquals(Regex.Filter.FIND.test(m("a", "abca")), true);
		assertEquals(Regex.Filter.FIND.test(m("a", "bcd")), false);
	}

	@Test
	public void testFilterPredicateNonFind() throws Exception {
		assertEquals(Regex.Filter.NON_FIND.test(null), false);
		assertEquals(Regex.Filter.NON_FIND.test(m("a", "abca")), false);
		assertEquals(Regex.Filter.NON_FIND.test(m("a", "bcd")), true);
	}

	@Test
	public void testFilterPredicateMatch() throws Exception {
		assertEquals(Regex.Filter.MATCH.test(null), false);
		assertEquals(Regex.Filter.MATCH.test(m("a", "a")), true);
		assertEquals(Regex.Filter.MATCH.test(m("a", "ab")), false);
	}

	@Test
	public void testFilterPredicateNonMatch() throws Exception {
		assertEquals(Regex.Filter.NON_MATCH.test(null), false);
		assertEquals(Regex.Filter.NON_MATCH.test(m("a", "a")), false);
		assertEquals(Regex.Filter.NON_MATCH.test(m("a", "ab")), true);
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
	public void testMapperGroup() throws Exception {
		assertString(Regex.Mapper.group(1).apply(null), null);
		assertString(Regex.Mapper.group(1).apply(find("(.)c", "abcb")), "b");
	}

	@Test
	public void testSplitArray() {
		assertArray(Regex.Split.array(null, "a::b:c:"));
		assertArray(Regex.Split.array(p(":"), null));
		assertArray(Regex.Split.array(p(":"), "a::b:c:"), "a", "", "b", "c");
		assertArray(Regex.Split.array(p(":"), "a::b:c:", 3, null), "a", "", "b:c:");
	}

	@Test
	public void testSplitList() {
		assertOrdered(Regex.Split.list(null, "a::b:c:"));
		assertOrdered(Regex.Split.list(p(":"), null));
		assertOrdered(Regex.Split.list(p(":"), "a::b:c:"), "a", "", "b", "c");
		assertOrdered(Regex.Split.list(p(":"), "a::b:c:", 3, null), "a", "", "b:c:");
	}

	@Test
	public void testSplitStream() {
		assertStream(Regex.Split.stream(null, "a::b:c:"));
		assertStream(Regex.Split.stream(p(":"), null));
		assertStream(Regex.Split.stream(p(":"), "a::b:c:"), "a", "", "b", "c");
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
	public void testSplit() {
		assertOrdered(Regex.Split.of(":").list("a::b:c:", 3), "a", "", "b:c:");
	}

	@Test
	public void testSplitMatcher() {
		var m = Regex.Split.COMMA.matcher("a,,b,c,");
		assertMatcher(Regex.find(m), 1, 2);
	}

	@Test
	public void testChainMatch() {
		var chain = Regex.Chain.of("abc123de45f6");
		var m = chain.matcher("[a-z]+");
		assertFind(m, "abc");
		assertFind(m, "de");
		m = chain.matcher("[0-9]+");
		assertFind(m, "45");
	}

	@Test
	public void testChainIgnorePreviousUnmatched() {
		var chain = Regex.Chain.of("abc123de45f6");
		var m = chain.matcher("[a-z]+");
		m = chain.matcher("[a-z]+");
		assertFind(m, "abc");
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
	public void testValidMatcher() {
		assertIllegalArg(() -> Regex.validMatcher(null));
		assertIllegalArg(() -> Regex.validMatcher(find(".+", "")));
		assertMatcher(Regex.validMatcher(find(".*", "")), "");
		assertMatcher(Regex.validMatcher(find(".", "abc")), "a");
	}

	@Test
	public void testCompile() {
		assertPattern(Regex.compile((Joiner) null), "");
		assertPattern(Regex.compile(Regex.OR), "");
		assertPattern(Regex.compile(Regex.OR, "a+", "b+"), "(?:a+|b+)");
	}

	@Test
	public void testIgnoreCase() {
		assertPattern(Regex.ignoreCase(null), "(?i)\\Q\\E");
		assertPattern(Regex.ignoreCase("aBc"), "(?i)\\QaBc\\E");
	}

	@Test
	public void testSafe() {
		assertPattern(Regex.safe(null), "");
		assertPattern(Regex.safe(Regex.NONE), "(?!)");
	}

	@Test
	public void testMatcher() {
		assertEquals(Regex.matcher(null, "abc"), null);
		assertEquals(Regex.matcher(p(".b"), null).find(), false);
		assertFind(Regex.matcher(p(".b"), "abc"), "ab");
	}

	@Test
	public void testMatch() {
		assertEquals(Regex.match(null), null);
		assertEquals(Regex.match(p(".b."), "abc").hasMatch(), true);
	}

	@Test
	public void testValidMatch() {
		assertIllegalArg(() -> Regex.validMatch(null, ""));
		assertIllegalArg(() -> Regex.validMatch(p(".+"), "", "test"));
		assertMatcher(Regex.validMatch(p(".*"), null), "");
		assertMatcher(Regex.validMatch(p(".*"), ""), "");
		assertMatcher(Regex.validMatch(p(".*"), "abc"), "abc");
	}

	@Test
	public void testMatchGroup() {
		assertString(Regex.matchGroup(null, "abc", 1), null);
		assertString(Regex.matchGroup(p("a(.)c"), null, 1), null);
		assertString(Regex.matchGroup(p("a(.)c"), "abc", 1), "b");
	}

	@Test
	public void testValidFind() {
		assertIllegalArg(() -> Regex.validFind(null, ""));
		assertIllegalArg(() -> Regex.validFind(p(".+"), "", "test"));
		assertMatcher(Regex.validFind(p(".*"), null), "");
		assertMatcher(Regex.validFind(p(".*"), ""), "");
		assertMatcher(Regex.validFind(p("\\w+"), " abc "), "abc");
	}

	@Test
	public void testFindGroup() {
		assertString(Regex.findGroup(null, "abc", 1), null);
		assertString(Regex.findGroup(p(".(.)(.)"), null, 1), null);
		assertString(Regex.findGroup(p(".(.)(.)"), "abc", 3), null);
		assertString(Regex.findGroup(p(".(.)(.)"), "abc", 1), "b");
	}

	@Test
	public void testAccept() {
		assertEquals(Regex.accept(null, _ -> {}), false);
		assertEquals(Regex.accept(find(".+", "abc"), null), false);
		assertEquals(Regex.accept(find(".+", "abc"), m -> assertString(m.group(), "abc")), true);
	}

	@Test
	public void testApply() throws Exception {
		assertEquals(Regex.apply(null, _ -> 1, 0), 0);
		assertEquals(Regex.apply(find(".+", "abc"), null, 0), 0);
		assertEquals(Regex.apply(find(".+", "abc"), m -> m.group(), ""), "abc");
	}

	@Test
	public void testMatchAccept() {
		assertEquals(Regex.matchAccept(null, "", _ -> {}), false);
		assertEquals(Regex.matchAccept(p(".+"), null, _ -> {}), false);
		assertEquals(Regex.matchAccept(p(".+"), "abc", null), false);
		assertEquals(Regex.matchAccept(p(".+"), "abc", m -> assertString(m.group(), "abc")), true);
	}

	@Test
	public void testMatchApply() throws Exception {
		assertEquals(Regex.matchApply(null, "", _ -> 1, 0), 0);
		assertEquals(Regex.matchApply(p(".+"), null, _ -> 1, 0), 0);
		assertEquals(Regex.matchApply(p(".+"), "abc", null, ""), "");
		assertEquals(Regex.matchApply(p(".+"), "abc", m -> m.group(), ""), "abc");
	}

	@Test
	public void testFindAccept() {
		assertEquals(Regex.findAccept(null, "", _ -> {}), false);
		assertEquals(Regex.findAccept(p("."), null, _ -> {}), false);
		assertEquals(Regex.findAccept(p("."), "abc", null), false);
		assertEquals(Regex.findAccept(p("."), "abc", m -> assertString(m.group(), "a")), true);
	}

	@Test
	public void testFindApply() throws Exception {
		assertEquals(Regex.findApply(null, "", _ -> 1, 0), 0);
		assertEquals(Regex.findApply(p("."), null, _ -> 1, 0), 0);
		assertEquals(Regex.findApply(p("."), "abc", null, ""), "");
		assertEquals(Regex.findApply(p("."), "abc", m -> m.group(), ""), "a");
	}

	@Test
	public void testFindAcceptAll() {
		assertEquals(Regex.findAcceptAll(null, "", (_, _) -> {}), false);
		assertEquals(Regex.findAcceptAll(p("."), null, (_, _) -> {}), false);
		assertEquals(Regex.findAcceptAll(p("."), "abc", null), false);
		assertEquals(Regex.findAcceptAll(p("."), "abc",
			(m, i) -> assertString(m.group(), "abc".substring(i, i + 1))), true);
	}

	@Test
	public void testFinds() {
		assertStream(Regex.finds(null, "abc"));
		assertStream(Regex.finds(p("."), null));
		assertStream(Regex.finds(p("."), "abc").map(Matcher::group), "a", "b", "c");
	}

	@Test
	public void testFindsWithGroup() {
		assertStream(Regex.finds(null, "abc", 1));
		assertStream(Regex.finds(p("(.)(.)"), null, 1));
		assertStream(Regex.finds(p("(.)(.)"), "abc", -1), nullStr);
		assertStream(Regex.finds(p("."), "abc").map(Matcher::group), "a", "b", "c");
	}

	@Test
	public void testMatchGroups() {
		assertStream(Regex.matchGroups(null, "abc"));
		assertStream(Regex.matchGroups(p("(.)(.)"), null));
		assertStream(Regex.matchGroups(p("(.)(.)"), "abc"));
		assertStream(Regex.matchGroups(p("(.)(.)"), "ab"), "a", "b");
		assertStream(Regex.matchGroups(p(".."), "ab"));
	}

	@Test
	public void testFindGroups() {
		assertStream(Regex.findGroups(null, "abc"));
		assertStream(Regex.findGroups(p("(.)(.)"), null));
		assertStream(Regex.findGroups(p("(.)(.)"), "a"));
		assertStream(Regex.findGroups(p("(.)(.)"), "abc"), "a", "b");
		assertStream(Regex.findGroups(p(".."), "abc"));
	}

	@Test
	public void testAcceptGroup() {
		assertEquals(Regex.acceptGroup(null, 1, _ -> {}), false);
		assertEquals(Regex.acceptGroup(find(".(.)", "abc"), 2, _ -> {}), false);
		assertEquals(Regex.acceptGroup(find(".(.)", "abc"), 1, null), false);
		assertEquals(Regex.acceptGroup(find(".(.)", "abc"), 1, s -> assertString(s, "b")), true);
	}

	@Test
	public void testApplyGroup() throws Exception {
		assertString(Regex.applyGroup(null, 1, _ -> "x", ""), "");
		assertString(Regex.applyGroup(find(".(.)", "abc"), 2, _ -> "x", ""), "");
		assertString(Regex.applyGroup(find(".(.)", "abc"), 1, null, ""), "");
		assertString(Regex.applyGroup(find(".(.)", "abc"), 1, s -> s.toUpperCase(), ""), "B");
	}

	@Test
	public void testRemoveAll() {
		assertString(Regex.removeAll(null, "abc"), "");
		assertString(Regex.removeAll(p(".*"), null), "");
		assertString(Regex.removeAll(p("\\d"), "a1b23c4"), "abc");
	}

	@Test
	public void testReplaceAll() {
		assertString(Regex.replaceAll(null, "abc", "_"), "");
		assertString(Regex.replaceAll(p(".*"), null, "_"), "");
		assertString(Regex.replaceAll(p("\\d"), "a1b23c4", "_"), "a_b__c_");
		assertString(Regex.replaceAll(null, p("\\d"), "a1b23c4", "_"), null);
		assertString(Regex.replaceAll(b("x"), null, "abc", "_"), "x");
		assertString(Regex.replaceAll(b("x"), p(".*"), null, "_"), "x");
		assertString(Regex.replaceAll(b("x"), p("\\d"), "a1b23c4", "_"), "xa_b__c_");
	}

	@Test
	public void testAppendAll() {
		assertString(Regex.appendAll(null, "abc", (_, _) -> {}), "");
		assertString(Regex.appendAll(p(".."), null, (_, _) -> {}), "");
		assertString(Regex.appendAll(p(".."), "abc", null), "");
		assertString(Regex.appendAll(p(".."), "abc", (_, _) -> {}), "c");
		assertString(Regex.appendAll(null, p(".."), "abc", (_, _) -> {}), null);
		assertString(Regex.appendAll(b("x"), null, "abc", (_, _) -> {}), "x");
		assertString(Regex.appendAll(b("x"), p(".."), null, (_, _) -> {}), "x");
		assertString(Regex.appendAll(b("x"), p(".."), "abc", null), "x");
		assertString(Regex.appendAll(b("x"), p(".."), "abc", (_, _) -> {}), "xc");
	}

	private static StringBuilder b(String s) {
		return new StringBuilder(s);
	}

	private static Pattern p(String format, Object... args) {
		return Regex.compile(format, args);
	}

	private static Matcher m(String pattern, String s) {
		return p(pattern).matcher(s);
	}

	private static Matcher find(String pattern, String s) {
		var m = m(pattern, s);
		m.find();
		return m;
	}

	private static void assertFind(Matcher m, String group) {
		assertMatcher(Regex.find(m), group);
	}

	private static void assertMatcher(Matcher m, String group) {
		assertString(m.group(), group);
	}

	private static void assertMatcher(Matcher m, int start, int end) {
		assertEquals(m.start(), start, "Start");
		assertEquals(m.end(), end, "End");
	}

	private static void assertPattern(Pattern pattern, String s) {
		assertString(pattern.pattern(), s);
	}
}
