package ceri.parser.token;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TokenizerBehavior {

	@Test
	public void shouldTokenizeAndOperator() {
		List<Token> tokens = new Tokenizer().tokenize("a *");
		List<Token> list =
			Arrays.<Token>asList(new KeywordToken("a"), new AndToken(), new WildcardToken());
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeOrOperator() {
		List<Token> tokens = new Tokenizer().tokenize("* OR (");
		List<Token> list =
			Arrays.<Token>asList(new WildcardToken(), new OrToken(), new LParenToken());
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeExcludeOperator() {
		List<Token> tokens = new Tokenizer().tokenize("-a");
		List<Token> list = Arrays.<Token>asList(new ExcludeToken(), new KeywordToken("a"));
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeParentheses() {
		List<Token> tokens = new Tokenizer().tokenize("(*(a))");
		List<Token> list =
			Arrays.<Token>asList(new LParenToken(), new WildcardToken(), new LParenToken(),
				new KeywordToken("a"), new RParenToken(), new RParenToken());
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeQuotes() {
		List<Token> tokens = new Tokenizer().tokenize("\"a\"");
		List<Token> list = Arrays.<Token>asList(new QuoteToken("a"));
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeWildcards() {
		List<Token> tokens = new Tokenizer().tokenize("*a*");
		List<Token> list =
			Arrays.<Token>asList(new WildcardToken(), new KeywordToken("a"), new WildcardToken());
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldTokenizeCombinations() {
		List<Token> tokens = new Tokenizer().tokenize("a -(b OR c)d");
		List<Token> list =
			Arrays.<Token>asList(new KeywordToken("a"), new AndToken(), new ExcludeToken(),
				new LParenToken(), new KeywordToken("b"), new OrToken(), new KeywordToken("c"),
				new RParenToken(), new KeywordToken("d"));
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldCleanParentheses() {
		List<Token> tokens = new Tokenizer().tokenize(")((");
		tokens = new Tokenizer().clean(tokens);
		List<Token> list = new Tokenizer().tokenize("()(())");
		assertThat(tokens, is(list));
	}

	@Test
	public void shouldRemoveInvalidBinaryOperators() {
		List<Token> tokens = new Tokenizer().tokenize(" a OR b OR ");
		tokens = new Tokenizer().clean(tokens);
		List<Token> list = new Tokenizer().tokenize("a OR b");
		assertThat(tokens, is(list));
	}

}
