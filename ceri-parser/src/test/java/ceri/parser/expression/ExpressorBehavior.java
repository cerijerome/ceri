package ceri.parser.expression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.StringPrintStream;
import ceri.parser.token.Token;
import ceri.parser.token.Tokenizer;

public class ExpressorBehavior {
	private Expressor expressor;
	private StringPrintStream out;

	@Before
	public void before() {
		out = new StringPrintStream();
		expressor = new Expressor(out);
	}

	@Test
	public void shouldConcatExpressions() {
		assertExpression("a\"b\"", new Concat(new Keyword("a"), new Quote("b")));
		assertExpression("\"b\"*", new Concat(new Quote("b"), new Wildcard()));
		assertExpression("a(*)c", new Concat(new Keyword("a"), new Concat(new Parentheses(
			new Wildcard()), new Keyword("c"))));
		assertExpression("*a\"b c\"d*", new Concat(new Wildcard(), new Concat(new Keyword("a"),
			new Concat(new Quote("b c"), new Concat(new Keyword("d"), new Wildcard())))));
		assertExpression("(a)(b)", new Concat(new Parentheses(new Keyword("a")), new Parentheses(
			new Keyword("b"))));
	}

	@Test
	public void shouldCombineRepeatExpressions() {
		assertExpression("**", new Wildcard());
		assertExpression("*****", new Wildcard());
		assertExpression("\"a\"\"b\"", new Quote("ab"));
		assertExpression("\"a\"\"b\"\"c\"\"d\"\"e\"", new Quote("abcde"));
		assertExpression("((*))", new Parentheses(new Wildcard()));
		assertExpression("(((((*)))))", new Parentheses(new Wildcard()));
	}

	@Test
	public void shouldExcludeSingleExpressions() {
		assertExpression("-\"a\"", new Exclude(new Quote("a")));
		assertExpression("-a*", new Exclude(new Concat(new Keyword("a"), new Wildcard())));
		assertExpression("-(*)", new Exclude(new Parentheses(new Wildcard())));
		assertErrorExpression("- OR ", null);
	}

	@Test
	public void shouldIgnoreInvalidExpressions() {
		assertErrorExpression("-", null);
		assertErrorExpression("--", null);
		assertErrorExpression(" ", null);
		assertErrorExpression(" b", new Keyword("b"));
		assertErrorExpression(" OR ", null);
		assertErrorExpression("a OR ", new Keyword("a"));
		assertErrorExpression("()", null);
		assertErrorExpression("(-)", null);
		assertErrorExpression("(a", new Parentheses(new Keyword("a")));
		assertErrorExpression("a)", new Keyword("a"));
	}

	@Test
	public void shouldApplyOperatorExpressions() {
		assertExpression("a \"b c\"", new And(new Keyword("a"), new Quote("b c")));
		assertExpression("* OR *", new Or(new Wildcard(), new Wildcard()));
		assertExpression("* * OR *",
			new And(new Wildcard(), new Or(new Wildcard(), new Wildcard())));
		assertExpression("(* *) OR *",
			new Or(new Parentheses(new And(new Wildcard(), new Wildcard())), new Wildcard()));
	}

	private String assertExpressionWithError(String str, Expression expression) {
		List<Token> tokens = new Tokenizer().tokenize(str);
		Expression exp = expressor.express(tokens);
		assertThat(exp, CoreMatchers.is(expression));
		return out.toString();
	}

	private void assertExpression(String str, Expression expression) {
		String error = assertExpressionWithError(str, expression);
		assertTrue(error.isEmpty());
	}

	private void assertErrorExpression(String str, Expression expression) {
		String error = assertExpressionWithError(str, expression);
		assertFalse(error.isEmpty());
	}

}
