package ceri.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.parser.expression.Expression;

public class ParserBehavior {
	private Parser parser = new Parser();
	
	@Test
	public void shouldMatchWithBinaryOperators() {
		assertMatch("ab bc", ".bc.ab.");
		assertMatch("aaa (bbb OR ccc)", ".bbb.aaa.");
		assertMatch("aaa (bbb OR ccc)", ".aaa.ccc.");
		assertMatch("aaa bbb OR ccc", ".ccc.aaa.");
		assertMatch("(aaa bbb) OR ccc", ".ccc.");
		assertNoMatch("aaa bbb OR ccc", ".ccc.");
		assertNoMatch("(aaa bbb) OR ccc", ".bbb.");
	}

	@Test
	public void shouldMatchWithExclusions() {
		assertMatch("ab -bc", ".ab.");
		assertNoMatch("ab -bc", ".abc.");
		assertMatch("ab OR -bc", ".ab.");
		assertMatch("ab OR -bc", "");
		assertMatch("ab OR -bc", ".");
		assertNoMatch("ab OR -bc", ".bc.");
	}
	
	@Test
	public void shouldMatchWithConcatenation() {
		assertMatch("a\"b c\"d", ".ab cd.");
		assertMatch("ab*bc", ".ab.bc.");
		assertNoMatch("ab*bc", ".ab.");
		assertNoMatch("ab*bc", ".bc.ab.");
	}
	
	private void assertMatch(String expression, String str) {
		Expression exp = parser.parse(expression);
		assertTrue(exp.matches(str));
	}
	
	private void assertNoMatch(String expression, String str) {
		Expression exp = parser.parse(expression);
		assertFalse(exp.matches(str));
	}
	
}
