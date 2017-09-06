package ceri.misc.parser.token;

import static ceri.misc.parser.token.Type.And;
import static ceri.misc.parser.token.Type.Exclude;
import static ceri.misc.parser.token.Type.Keyword;
import static ceri.misc.parser.token.Type.LParen;
import static ceri.misc.parser.token.Type.Or;
import static ceri.misc.parser.token.Type.Quote;
import static ceri.misc.parser.token.Type.RParen;
import static ceri.misc.parser.token.Type.Wildcard;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import ceri.common.collection.ImmutableUtil;

public class Tokenizer {
	private static final Collection<Token.Factory> FACTORIES = ImmutableUtil.asList(
		QuoteToken.FACTORY, OrToken.FACTORY, LParenToken.FACTORY, RParenToken.FACTORY,
		AndToken.FACTORY, ExcludeToken.FACTORY, WildcardToken.FACTORY, KeywordToken.FACTORY,
		UnknownToken.FACTORY);
	private static final Collection<Type> BINARY_OP = Collections.unmodifiableSet(EnumSet.of( //
		And, Or));
	private static final Collection<Type> VALID_BINARY_LHS = Collections.unmodifiableSet(EnumSet
		.of(RParen, Quote, Keyword, Wildcard));
	private static final Collection<Type> VALID_BINARY_RHS = Collections.unmodifiableSet(EnumSet
		.of(LParen, Quote, Keyword, Wildcard, Exclude));

	public List<Token> tokenize(String str) {
		Index i = new Index();
		int len = str.length();
		List<Token> tokens = new ArrayList<>();
		while (i.value() < len) {
			Token token = createToken(str, i);
			if (token != null) tokens.add(token);
			else i.inc();
		}
		return tokens;
	}

	public List<Token> clean(List<Token> tokens) {
		tokens = new ArrayList<>(tokens);
		removeUnknownTokens(tokens);
		addUnmatchedParentheses(tokens);
		removeInvalidBinaryOperators(tokens);
		return tokens;
	}

	private void removeUnknownTokens(List<Token> tokens) {
		for (Iterator<Token> i = tokens.iterator(); i.hasNext();)
			if (i.next() instanceof UnknownToken) i.remove();
	}

	private void removeInvalidBinaryOperators(List<Token> tokens) {
		int i = -1;
		for (Iterator<Token> iter = tokens.iterator(); iter.hasNext();) {
			i++;
			Token token = iter.next();
			if (!isType(token, BINARY_OP)) continue;
			if (i > 0 && i + 1 < tokens.size()) {
				Token previous = tokens.get(i - 1);
				Token next = tokens.get(i + 1);
				if (isType(previous, VALID_BINARY_LHS) && isType(next, VALID_BINARY_RHS)) continue;
			}
			i--;
			iter.remove();
		}
	}

	private boolean isType(Token token, Collection<Type> types) {
		if (token == null) return false;
		return types.contains(token.type());
	}

	private void addUnmatchedParentheses(List<Token> tokens) {
		int neg = 0;
		int pos = 0;
		for (Token token : tokens) {
			if (token instanceof RParenToken) {
				if (pos == 0) neg--;
				else pos--;
			} else if (token instanceof LParenToken) pos++;
		}
		for (int i = neg; i < 0; i++)
			tokens.add(0, new LParenToken());
		for (int i = pos; i > 0; i--)
			tokens.add(new RParenToken());
	}

	private Token createToken(String str, Index i) {
		for (Token.Factory factory : FACTORIES) {
			Token token = factory.create(str, i);
			if (token != null) return token;
		}
		// Unknown token is the catch-all
		throw new IllegalStateException("Should not happen");
	}

}
