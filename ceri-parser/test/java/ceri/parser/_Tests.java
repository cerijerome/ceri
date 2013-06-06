package ceri.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.parser.expression.ExpressionTest;
import ceri.parser.expression.ExpressorBehavior;
import ceri.parser.token.IndexBehavior;
import ceri.parser.token.TokenUtilTest;
import ceri.parser.token.TokenizerBehavior;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// parser
	ParserBehavior.class,
	// expression
	ExpressionTest.class,
	ExpressorBehavior.class,
	// token
	IndexBehavior.class,
	TokenizerBehavior.class,
	TokenUtilTest.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
