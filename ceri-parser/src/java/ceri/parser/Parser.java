package ceri.parser;

import java.util.List;
import java.util.regex.Pattern;
import ceri.parser.expression.Expression;
import ceri.parser.expression.Expressor;
import ceri.parser.token.Token;
import ceri.parser.token.Tokenizer;

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
	
}
