package ceri.misc.parser;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import ceri.common.function.Predicates;
import ceri.common.text.StringUtil;
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
	public Predicate<String> filter(String expression) {
		if (StringUtil.isBlank(expression)) return Predicates.yes();
		final Expression exp = parse(expression);
		if (exp == null) return Predicates.yes();
		return (exp::matches);
	}

}
