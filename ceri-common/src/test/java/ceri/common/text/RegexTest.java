package ceri.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.test.Assert;

public class RegexTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final String nullStr = null;

	@Test
	public void testPrivateConstructor() {
		Assert.privateConstructor(Regex.class, Regex.Common.class, Regex.Filter.class);
	}

	@Test
	public void testCommonDecodeInt() {
		Assert.equal(Regex.Common.decodeInt("0b001001"), 0b001001);
		Assert.equal(Regex.Common.decodeInt("0B111"), 0B111);
		Assert.equal(Regex.Common.decodeInt("0123"), 0123);
		Assert.equal(Regex.Common.decodeInt("0x0123"), 0x123);
		Assert.equal(Regex.Common.decodeInt("0X0123"), 0x123);
		Assert.equal(Regex.Common.decodeInt("#0123"), 0x123);
		Assert.equal(Regex.Common.decodeInt("123"), 123);
		Assert.equal(Regex.Common.decodeInt("0"), 0);
	}

	@Test
	public void testCommonDecodeLong() {
		Assert.equal(Regex.Common.decodeLong("0b001001"), 0b001001L);
		Assert.equal(Regex.Common.decodeLong("0B111"), 0B111L);
		Assert.equal(Regex.Common.decodeLong("0123"), 0123L);
		Assert.equal(Regex.Common.decodeLong("0x0123"), 0x123L);
		Assert.equal(Regex.Common.decodeLong("0X0123"), 0x123L);
		Assert.equal(Regex.Common.decodeLong("#0123"), 0x123L);
		Assert.equal(Regex.Common.decodeLong("123"), 123L);
		Assert.equal(Regex.Common.decodeLong("0"), 0L);
	}

	@Test
	public void testFilterPredicateFind() throws Exception {
		Assert.equal(Regex.Filter.FIND.test(null), false);
		Assert.equal(Regex.Filter.FIND.test(m("a", "abca")), true);
		Assert.equal(Regex.Filter.FIND.test(m("a", "bcd")), false);
	}

	@Test
	public void testFilterPredicateNonFind() throws Exception {
		Assert.equal(Regex.Filter.NON_FIND.test(null), false);
		Assert.equal(Regex.Filter.NON_FIND.test(m("a", "abca")), false);
		Assert.equal(Regex.Filter.NON_FIND.test(m("a", "bcd")), true);
	}

	@Test
	public void testFilterPredicateMatch() throws Exception {
		Assert.equal(Regex.Filter.MATCH.test(null), false);
		Assert.equal(Regex.Filter.MATCH.test(m("a", "a")), true);
		Assert.equal(Regex.Filter.MATCH.test(m("a", "ab")), false);
	}

	@Test
	public void testFilterPredicateNonMatch() throws Exception {
		Assert.equal(Regex.Filter.NON_MATCH.test(null), false);
		Assert.equal(Regex.Filter.NON_MATCH.test(m("a", "a")), false);
		Assert.equal(Regex.Filter.NON_MATCH.test(m("a", "ab")), true);
	}

	@Test
	public void testFilterFind() throws Exception {
		Assert.equal(Regex.Filter.find(INT_PATTERN).test(null), false);
		Assert.equal(Regex.Filter.find("(\\d+)").test("abc123def456"), true);
		Assert.equal(Regex.Filter.find(LSTRING_PATTERN).test("abc123def456"), true);
		Assert.equal(Regex.Filter.find(USTRING_PATTERN).test("abc123def456"), false);
	}

	@Test
	public void testFilterMatch() throws Exception {
		Assert.equal(Regex.Filter.match(INT_PATTERN).test(null), false);
		Assert.equal(Regex.Filter.match(INT_PATTERN).test("123"), true);
		Assert.equal(Regex.Filter.match("(\\d+)").test("123def456"), false);
		Assert.equal(Regex.Filter.match(LSTRING_PATTERN).test("abc"), true);
		Assert.equal(Regex.Filter.match(USTRING_PATTERN).test("abc"), false);
	}

	@Test
	public void testFilterMatching() throws Exception {
		Assert.equal(Regex.Filter.matching(null, _ -> true).test(""), false);
		Assert.equal(Regex.Filter.matching(Regex.ALL, null).test(""), false);
		Assert.equal(Regex.Filter.matching(Regex.ALL, Matcher::find).test(null), false);
		Assert.equal(Regex.Filter.matching(Regex.ALL, Matcher::find).test("a"), true);
	}

	@Test
	public void testMapperMatchGroup() throws Exception {
		Assert.string(Regex.Mapper.matchGroup(INT_PATTERN, 1).apply(null), null);
		Assert.string(Regex.Mapper.matchGroup(INT_PATTERN, 1).apply("abc"), null);
		Assert.string(Regex.Mapper.matchGroup(INT_PATTERN, 1).apply("123abc"), null);
		Assert.string(Regex.Mapper.matchGroup(INT_PATTERN, 1).apply("123"), "123");
	}

	@Test
	public void testMapperFindGroup() throws Exception {
		Assert.string(Regex.Mapper.findGroup(INT_PATTERN, 1).apply(null), null);
		Assert.string(Regex.Mapper.findGroup(INT_PATTERN, 1).apply("abc"), null);
		Assert.string(Regex.Mapper.findGroup(INT_PATTERN, 1).apply("123abc"), "123");
		Assert.string(Regex.Mapper.findGroup(INT_PATTERN, 1).apply("123"), "123");
	}

	@Test
	public void testMapperGroup() throws Exception {
		Assert.string(Regex.Mapper.group(1).apply(null), null);
		Assert.string(Regex.Mapper.group(1).apply(find("(.)c", "abcb")), "b");
	}

	@Test
	public void testSplitArray() {
		Assert.array(Regex.Split.array(null, "a::b:c:"));
		Assert.array(Regex.Split.array(p(":"), null));
		Assert.array(Regex.Split.array(p(":"), "a::b:c:"), "a", "", "b", "c");
		Assert.array(Regex.Split.array(p(":"), "a::b:c:", 3, null), "a", "", "b:c:");
	}

	@Test
	public void testSplitList() {
		Assert.ordered(Regex.Split.list(null, "a::b:c:"));
		Assert.ordered(Regex.Split.list(p(":"), null));
		Assert.ordered(Regex.Split.list(p(":"), "a::b:c:"), "a", "", "b", "c");
		Assert.ordered(Regex.Split.list(p(":"), "a::b:c:", 3, null), "a", "", "b:c:");
	}

	@Test
	public void testSplitStream() {
		Assert.stream(Regex.Split.stream(null, "a::b:c:"));
		Assert.stream(Regex.Split.stream(p(":"), null));
		Assert.stream(Regex.Split.stream(p(":"), "a::b:c:"), "a", "", "b", "c");
	}

	@Test
	public void testSplitLine() {
		Assert.ordered(Regex.Split.LINE.list(""));
		Assert.ordered(Regex.Split.LINE.list(" "), " ");
		Assert.ordered(Regex.Split.LINE.list("\n"));
		Assert.ordered(Regex.Split.LINE.list(" \n\t"), " ", "\t");
	}

	@Test
	public void testSplitComma() {
		Assert.ordered(Regex.Split.COMMA.list(null));
		Assert.ordered(Regex.Split.COMMA.list(""));
		Assert.ordered(Regex.Split.COMMA.list(" "), "");
		Assert.ordered(Regex.Split.COMMA.list("a"), "a");
		Assert.ordered(Regex.Split.COMMA.list(" a "), "a");
		Assert.ordered(Regex.Split.COMMA.list(",,a"), "", "", "a");
		Assert.ordered(Regex.Split.COMMA.list("a,,"), "a");
		Assert.ordered(Regex.Split.COMMA.list(" , a "), "", "a");
		Assert.ordered(Regex.Split.COMMA.list("a,b"), "a", "b");
		Assert.ordered(Regex.Split.COMMA.list(" a , b "), "a", "b");
	}

	@Test
	public void testSplitSpace() {
		Assert.ordered(Regex.Split.SPACE.list(null));
		Assert.ordered(Regex.Split.SPACE.list(""));
		Assert.ordered(Regex.Split.SPACE.list(" "));
		Assert.ordered(Regex.Split.SPACE.list("a"), "a");
		Assert.ordered(Regex.Split.SPACE.list(" a b "), "", "a", "b");
	}

	@Test
	public void testSplit() {
		Assert.ordered(Regex.Split.of(":").list("a::b:c:", 3), "a", "", "b:c:");
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
		Assert.equal(Regex.hash(null), Regex.hash(null));
		Assert.equal(Regex.hash(p0), Regex.hash(p0));
		Assert.equal(Regex.hash(p1), Regex.hash(p1));
		Assert.equal(Regex.hash(p2), Regex.hash(p2));
	}

	@Test
	public void testEquals() {
		var p = Pattern.compile("test.*");
		Assert.equal(Regex.equals(null, null), true);
		Assert.equal(Regex.equals(p, null), false);
		Assert.equal(Regex.equals(null, p), false);
		Assert.equal(Regex.equals(p, p), true);
		Assert.equal(Regex.equals(p, Pattern.compile("test.*")), true);
		Assert.equal(Regex.equals(p, Pattern.compile("test.+")), false);
		Assert.equal(Regex.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 1)), true);
		Assert.equal(Regex.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 2)), false);
	}

	@Test
	public void testHasMatch() {
		Assert.equal(Regex.hasMatch(null), false);
		Assert.equal(Regex.hasMatch(Regex.ALL.matcher("")), false);
		Assert.equal(Regex.hasMatch(find(".+", "")), false);
		Assert.equal(Regex.hasMatch(find(".*", "")), true);
	}

	@Test
	public void testValidMatcher() {
		Assert.illegalArg(() -> Regex.validMatcher(null));
		Assert.illegalArg(() -> Regex.validMatcher(find(".+", "")));
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
		Assert.equal(Regex.matcher(null, "abc"), null);
		Assert.equal(Regex.matcher(p(".b"), null).find(), false);
		assertFind(Regex.matcher(p(".b"), "abc"), "ab");
	}

	@Test
	public void testMatch() {
		Assert.equal(Regex.match(null), null);
		Assert.equal(Regex.match(p(".b."), "abc").hasMatch(), true);
	}

	@Test
	public void testValidMatch() {
		Assert.illegalArg(() -> Regex.validMatch(null, ""));
		Assert.illegalArg(() -> Regex.validMatch(p(".+"), "", "test"));
		assertMatcher(Regex.validMatch(p(".*"), null), "");
		assertMatcher(Regex.validMatch(p(".*"), ""), "");
		assertMatcher(Regex.validMatch(p(".*"), "abc"), "abc");
	}

	@Test
	public void testMatchGroup() {
		Assert.string(Regex.matchGroup(null, "abc", 1), null);
		Assert.string(Regex.matchGroup(p("a(.)c"), null, 1), null);
		Assert.string(Regex.matchGroup(p("a(.)c"), "abc", 1), "b");
	}

	@Test
	public void testValidFind() {
		Assert.illegalArg(() -> Regex.validFind(null, ""));
		Assert.illegalArg(() -> Regex.validFind(p(".+"), "", "test"));
		assertMatcher(Regex.validFind(p(".*"), null), "");
		assertMatcher(Regex.validFind(p(".*"), ""), "");
		assertMatcher(Regex.validFind(p("\\w+"), " abc "), "abc");
	}

	@Test
	public void testFindGroup() {
		Assert.string(Regex.findGroup(null, "abc", 1), null);
		Assert.string(Regex.findGroup(p(".(.)(.)"), null, 1), null);
		Assert.string(Regex.findGroup(p(".(.)(.)"), "abc", 3), null);
		Assert.string(Regex.findGroup(p(".(.)(.)"), "abc", 1), "b");
	}

	@Test
	public void testAccept() {
		Assert.equal(Regex.accept(null, _ -> {}), false);
		Assert.equal(Regex.accept(find(".+", "abc"), null), false);
		Assert.equal(Regex.accept(find(".+", "abc"), m -> Assert.string(m.group(), "abc")), true);
	}

	@Test
	public void testApply() throws Exception {
		Assert.equal(Regex.apply(null, _ -> 1, 0), 0);
		Assert.equal(Regex.apply(find(".+", "abc"), null, 0), 0);
		Assert.equal(Regex.apply(find(".+", "abc"), m -> m.group(), ""), "abc");
	}

	@Test
	public void testMatchAccept() {
		Assert.equal(Regex.matchAccept(null, "", _ -> {}), false);
		Assert.equal(Regex.matchAccept(p(".+"), null, _ -> {}), false);
		Assert.equal(Regex.matchAccept(p(".+"), "abc", null), false);
		Assert.equal(Regex.matchAccept(p(".+"), "abc", m -> Assert.string(m.group(), "abc")), true);
	}

	@Test
	public void testMatchApply() throws Exception {
		Assert.equal(Regex.matchApply(null, "", _ -> 1, 0), 0);
		Assert.equal(Regex.matchApply(p(".+"), null, _ -> 1, 0), 0);
		Assert.equal(Regex.matchApply(p(".+"), "abc", null, ""), "");
		Assert.equal(Regex.matchApply(p(".+"), "abc", m -> m.group(), ""), "abc");
	}

	@Test
	public void testFindAccept() {
		Assert.equal(Regex.findAccept(null, "", _ -> {}), false);
		Assert.equal(Regex.findAccept(p("."), null, _ -> {}), false);
		Assert.equal(Regex.findAccept(p("."), "abc", null), false);
		Assert.equal(Regex.findAccept(p("."), "abc", m -> Assert.string(m.group(), "a")), true);
	}

	@Test
	public void testFindApply() throws Exception {
		Assert.equal(Regex.findApply(null, "", _ -> 1, 0), 0);
		Assert.equal(Regex.findApply(p("."), null, _ -> 1, 0), 0);
		Assert.equal(Regex.findApply(p("."), "abc", null, ""), "");
		Assert.equal(Regex.findApply(p("."), "abc", m -> m.group(), ""), "a");
	}

	@Test
	public void testFindAcceptAll() {
		Assert.equal(Regex.findAcceptAll(null, "", (_, _) -> {}), false);
		Assert.equal(Regex.findAcceptAll(p("."), null, (_, _) -> {}), false);
		Assert.equal(Regex.findAcceptAll(p("."), "abc", null), false);
		Assert.equal(Regex.findAcceptAll(p("."), "abc",
			(m, i) -> Assert.string(m.group(), "abc".substring(i, i + 1))), true);
	}

	@Test
	public void testFinds() {
		Assert.stream(Regex.finds(null, "abc"));
		Assert.stream(Regex.finds(p("."), null));
		Assert.stream(Regex.finds(p("."), "abc").map(Matcher::group), "a", "b", "c");
	}

	@Test
	public void testFindsWithGroup() {
		Assert.stream(Regex.finds(null, "abc", 1));
		Assert.stream(Regex.finds(p("(.)(.)"), null, 1));
		Assert.stream(Regex.finds(p("(.)(.)"), "abc", -1), nullStr);
		Assert.stream(Regex.finds(p("."), "abc").map(Matcher::group), "a", "b", "c");
	}

	@Test
	public void testMatchGroups() {
		Assert.stream(Regex.matchGroups(null, "abc"));
		Assert.stream(Regex.matchGroups(p("(.)(.)"), null));
		Assert.stream(Regex.matchGroups(p("(.)(.)"), "abc"));
		Assert.stream(Regex.matchGroups(p("(.)(.)"), "ab"), "a", "b");
		Assert.stream(Regex.matchGroups(p(".."), "ab"));
	}

	@Test
	public void testFindGroups() {
		Assert.stream(Regex.findGroups(null, "abc"));
		Assert.stream(Regex.findGroups(p("(.)(.)"), null));
		Assert.stream(Regex.findGroups(p("(.)(.)"), "a"));
		Assert.stream(Regex.findGroups(p("(.)(.)"), "abc"), "a", "b");
		Assert.stream(Regex.findGroups(p(".."), "abc"));
	}

	@Test
	public void testAcceptGroup() {
		Assert.equal(Regex.acceptGroup(null, 1, _ -> {}), false);
		Assert.equal(Regex.acceptGroup(find(".(.)", "abc"), 2, _ -> {}), false);
		Assert.equal(Regex.acceptGroup(find(".(.)", "abc"), 1, null), false);
		Assert.equal(Regex.acceptGroup(find(".(.)", "abc"), 1, s -> Assert.string(s, "b")), true);
	}

	@Test
	public void testApplyGroup() throws Exception {
		Assert.string(Regex.applyGroup(null, 1, _ -> "x", ""), "");
		Assert.string(Regex.applyGroup(find(".(.)", "abc"), 2, _ -> "x", ""), "");
		Assert.string(Regex.applyGroup(find(".(.)", "abc"), 1, null, ""), "");
		Assert.string(Regex.applyGroup(find(".(.)", "abc"), 1, s -> s.toUpperCase(), ""), "B");
	}

	@Test
	public void testRemoveAll() {
		Assert.string(Regex.removeAll(null, "abc"), "");
		Assert.string(Regex.removeAll(p(".*"), null), "");
		Assert.string(Regex.removeAll(p("\\d"), "a1b23c4"), "abc");
	}

	@Test
	public void testReplaceAll() {
		Assert.string(Regex.replaceAll(null, "abc", "_"), "");
		Assert.string(Regex.replaceAll(p(".*"), null, "_"), "");
		Assert.string(Regex.replaceAll(p("\\d"), "a1b23c4", "_"), "a_b__c_");
		Assert.string(Regex.replaceAll(null, p("\\d"), "a1b23c4", "_"), null);
		Assert.string(Regex.replaceAll(b("x"), null, "abc", "_"), "x");
		Assert.string(Regex.replaceAll(b("x"), p(".*"), null, "_"), "x");
		Assert.string(Regex.replaceAll(b("x"), p("\\d"), "a1b23c4", "_"), "xa_b__c_");
	}

	@Test
	public void testAppendAll() {
		Assert.string(Regex.appendAll(null, "abc", (_, _) -> {}), "");
		Assert.string(Regex.appendAll(p(".."), null, (_, _) -> {}), "");
		Assert.string(Regex.appendAll(p(".."), "abc", null), "");
		Assert.string(Regex.appendAll(p(".."), "abc", (_, _) -> {}), "c");
		Assert.string(Regex.appendAll(null, p(".."), "abc", (_, _) -> {}), null);
		Assert.string(Regex.appendAll(b("x"), null, "abc", (_, _) -> {}), "x");
		Assert.string(Regex.appendAll(b("x"), p(".."), null, (_, _) -> {}), "x");
		Assert.string(Regex.appendAll(b("x"), p(".."), "abc", null), "x");
		Assert.string(Regex.appendAll(b("x"), p(".."), "abc", (_, _) -> {}), "xc");
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
		Assert.string(m.group(), group);
	}

	private static void assertMatcher(Matcher m, int start, int end) {
		Assert.equal(m.start(), start, "Start");
		Assert.equal(m.end(), end, "End");
	}

	private static void assertPattern(Pattern pattern, String s) {
		Assert.string(pattern.pattern(), s);
	}
}
