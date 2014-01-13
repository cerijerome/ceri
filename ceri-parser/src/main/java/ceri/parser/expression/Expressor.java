package ceri.parser.expression;

import static ceri.parser.token.Type.And;
import static ceri.parser.token.Type.Exclude;
import static ceri.parser.token.Type.Keyword;
import static ceri.parser.token.Type.LParen;
import static ceri.parser.token.Type.Or;
import static ceri.parser.token.Type.Quote;
import static ceri.parser.token.Type.RParen;
import static ceri.parser.token.Type.Wildcard;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import ceri.parser.token.KeywordToken;
import ceri.parser.token.QuoteToken;
import ceri.parser.token.Token;

public class Expressor {
	private final PrintStream err;
	
	public Expressor() {
		this(null);
	}
	
	public Expressor(PrintStream err) {
		this.err = err;
	}
	
	public Expression express(Token...tokens) {
		return express(Arrays.asList(tokens));
	}
	
	public Expression express(List<Token> tokens) {
		return process(tokens.listIterator());
	}

	private Expression process(ListIterator<Token> iterator) {
		Expression exp = null;
		while (exp == null && iterator.hasNext()) exp = processSingle(iterator);
		return processBinaryOp(exp, iterator);
	}

	private Expression processBinaryOp(Expression previous, ListIterator<Token> iterator) {
		if (!iterator.hasNext()) return previous;
		Token token = iterator.next();
		Expression exp;
		if ((exp = processAnd(previous, token, iterator)) != null) return exp;
		if ((exp = processOr(previous, token, iterator)) != null) return exp;
		if (token.type() == RParen) return previous;
		// Invalid tokens: Quote, Wildcard, Keyword, LParen, Exclude
		log("Invalid token: " + token);
		return previous;
	}

	private Expression processSingle(ListIterator<Token> iterator) {
		if (!iterator.hasNext()) return null;
		Token token = iterator.next();
		Expression exp;
		if ((exp = processConcatable(token, iterator)) != null) return exp;
		if ((exp = processExclude(token, iterator)) != null) return exp;
		// Invalid tokens: And, Or, RParen, Unknown
		log("Invalid token: " + token);
		return null;
	}

	private Expression processConcatable(Token token, ListIterator<Token> iterator) {
		Expression exp = concatable(token, iterator);
		if (exp == null || !iterator.hasNext()) return exp;
		Expression next = processConcatable(iterator.next(), iterator);
		if (exp instanceof Wildcard && next instanceof Wildcard) return exp;
		if (exp instanceof Quote && next instanceof Quote)
			return new Quote(((Quote)exp).value + ((Quote)next).value);
		if (next != null) return new Concat(exp, next);
		iterator.previous();
		return exp;
	}

	private Expression concatable(Token token, ListIterator<Token> iterator) {
		if (token.type() == Wildcard) return new Wildcard();
		if (token.type() == Keyword) return new Keyword(((KeywordToken) token).value);
		if (token.type() == Quote) return new Quote(((QuoteToken) token).value);
		return processLParen(token, iterator);
	}

	private Expression processAnd(Expression previous, Token token, ListIterator<Token> iterator) {
		if (token.type() != And) return null;
		Expression next = process(iterator);
		if (next == null) return previous;
		return new And(previous, next);
	}

	private Expression processOr(Expression previous, Token token, ListIterator<Token> iterator) {
		if (token.type() != Or) return null;
		Expression next = process(iterator);
		if (next == null) return previous;
		return new Or(previous, next);
	}

	private Expression processExclude(Token token, ListIterator<Token> iterator) {
		if (token.type() != Exclude) return null;
		Expression exp = processSingle(iterator);
		if (exp == null) return null;
		return new Exclude(exp);
	}

	private Expression processLParen(Token token, ListIterator<Token> iterator) {
		if (token.type() != LParen) return null;
		Expression exp = process(iterator);
		if (exp == null) return null;
		if (exp instanceof Parentheses) return exp; // no need to repeat parentheses
		return new Parentheses(exp);
	}

	private void log(String s) {
		if (err == null) return;
		err.println(s);
	}
	
}
