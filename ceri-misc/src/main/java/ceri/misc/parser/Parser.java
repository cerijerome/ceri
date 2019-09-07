package ceri.misc.parser;

import java.util.List;
import java.util.regex.Pattern;
import ceri.common.filter.Filter;
import ceri.common.filter.Filters;
import ceri.common.util.BasicUtil;
import ceri.misc.parser.expression.Expression;
import ceri.misc.parser.expression.Expressor;
import ceri.misc.parser.token.Token;
import ceri.misc.parser.token.Tokenizer;

public class Parser {
	private final Tokenizer tokenizer = new Tokenizer();
	private final Expressor expressor = new Expressor();

	public Expression parse(String str) {
		List<Token> tokens = tokenizer.tokenize(str);
		tokens = tokenizer.clean(tokens);
		return expressor.express(tokens);
	}

	public Pattern parseAsRegex(String str) {
		Expression expression = parse(str);
		return Pattern.compile(expression.asRegex());
	}

	/**
	 * Filter that returns true for strings that match the given pattern.
	 */
	public Filter<String> filter(String expression) {
		if (BasicUtil.isEmpty(expression)) return Filters._true();
		final Expression exp = parse(expression);
		if (exp == null) return Filters._true();
		return (exp::matches);
	}

}
