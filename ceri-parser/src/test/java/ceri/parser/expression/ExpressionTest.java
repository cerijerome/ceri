package ceri.parser.expression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.StringPrintStream;
import ceri.parser.token.Token;
import ceri.parser.token.Tokenizer;

public class ExpressionTest {
	private Expressor expressor;
	private StringPrintStream out;

	@Before
	public void before() {
		out = new StringPrintStream();
		expressor = new Expressor(out);
	}

	@Test
	public void testKeywordRegex() {
		assertRegexMatch("ab", "ab");
		assertRegexMatch("ab", "xabx");
		assertRegexNoMatch("ab", "ba");
	}

	@Test
	public void testQuoteRegex() {
		assertRegexMatch("\"a b\"", "a b");
		assertRegexMatch("\"a b\"", "xa bx");
		assertRegexNoMatch("\"a b\"", "ab");
		assertRegexNoMatch("\"a b\"", "b a");
	}

	@Test
	public void testWildcardRegex() {
		assertRegexMatch("*", "");
		assertRegexMatch("*", "abcdefghijklm");
	}

	@Test
	public void testParenthesesRegex() {
		assertRegexMatch("(ab)", "ab");
		assertRegexMatch("(ab bc)", "bcab");
		assertRegexMatch("(ab OR bc)", "cab");
		assertRegexNoMatch("(ab OR bc)", "ac");
	}

	@Test
	public void testExcludeRegex() {
		assertRegexMatch("-ab", "ba");
		assertRegexMatch("-ab -bc", "ac");
		assertRegexMatch("ab -bc", "ab");
		assertRegexNoMatch("ab -bc", "ac");
	}

	@Test
	public void testConcatRegex() {
		assertRegexMatch("ab\"bc cd\"", "abbc cd");
		assertRegexMatch("ab*bc", "ab bc");
		assertRegexMatch("ab(bc OR cd)", "abbc");
		assertRegexMatch("ab(bc OR cd)", "abcd");
	}

	@Test
	public void testAndOperatorRegex() {
		assertRegexMatch("ab bc", "abbc");
		assertRegexMatch("ab bc", "bcab");
		assertRegexMatch("ab bc", "xabbc");
		assertRegexMatch("ab bc", "bcabx");
		assertRegexNoMatch("ab bc", "");
		assertRegexNoMatch("ab bc", "abc");
		assertRegexNoMatch("ab bc", "abxbc");
	}

	@Test
	public void testOrOperatorRegex() {
		assertRegexMatch("ab OR bc", "ab");
		assertRegexMatch("ab OR bc", "bc");
		assertRegexMatch("ab OR bc", "xab");
		assertRegexNoMatch("ab OR bc", "");
		assertRegexNoMatch("ab OR bc", "x");
	}

	private void assertRegexMatch(String expression, String str) {
		Pattern pattern = regex(expression);
		assertTrue(pattern.matcher(str).find());
	}
	
	private void assertRegexNoMatch(String expression, String str) {
		Pattern pattern = regex(expression);
		assertFalse(pattern.matcher(str).find());
	}
	
	private Pattern regex(String expression) {
		List<Token> tokens = new Tokenizer().tokenize(expression);
		Expression exp = expressor.express(tokens);
		assertTrue(out.toString().isEmpty());
		String regex = exp.asRegex();
		return Pattern.compile(regex);
	}
	
}
