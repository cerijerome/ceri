package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TextUtilTest {
	
	@Test
	public void testToWords() {
		assertThat(TextUtil.toWords(""), is(new String[0]));
		assertThat(TextUtil.toWords("helloThereABC"), is(new String[] { "hello", "There", "ABC" }));
		assertThat(TextUtil.toWords("_hello1There2ABC3_"),
			is(new String[] { "_hello1", "There2", "ABC3_"}));
		assertThat(TextUtil.toWords("_"), is(new String[] { "_" }));
	}
	
	@Test
	public void testToPhrase() {
		assertThat(TextUtil.toPhrase(""), is(""));
		assertThat(TextUtil.toPhrase("helloThereABC"), is("hello there ABC"));
		assertThat(TextUtil.toPhrase("_hello1There2ABC3_"),	is("_hello1 there2 ABC3_"));
		assertThat(TextUtil.toPhrase("_"), is("_"));
	}

	@Test
	public void testCamelToPascal() {
		assertThat(TextUtil.camelToPascal(""), is(""));
		assertThat(TextUtil.camelToPascal("_helloThereABC_"), is("_HelloThereABC_"));
		assertThat(TextUtil.camelToPascal("hello1there2ABC3_"), is("Hello1There2ABC3_"));
	}
	
	@Test
	public void testFirstToUpper() {
		assertThat(TextUtil.firstToUpper(""), is(""));
		assertThat(TextUtil.firstToUpper("hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("Hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("_"), is("_"));
	}

	@Test
	public void testFirstToLower() {
		assertThat(TextUtil.firstToLower(""), is(""));
		assertThat(TextUtil.firstToLower("hello"), is("hello"));
		assertThat(TextUtil.firstToLower("Hello"), is("hello"));
		assertThat(TextUtil.firstToLower("_"), is("_"));
	}

	@Test
	public void testPascalToUpper() {
		assertThat(TextUtil.pascalToUpper(""), is(""));
		assertThat(TextUtil.pascalToUpper("_HelloThereABC_"), is("_HELLO_THERE_ABC_"));
		assertThat(TextUtil.pascalToUpper("_Hello1There2ABC3_"), is("_HELLO1_THERE2_ABC3_"));
	}
	
	@Test
	public void testPascalToCapitalizedPhrase() {
		assertThat(TextUtil.pascalToCapitalizedPhrase(""), is(""));
		assertThat(TextUtil.pascalToCapitalizedPhrase("_HelloThereABC_"), is("_Hello There ABC_"));
		assertThat(TextUtil.pascalToCapitalizedPhrase("Hello1There2ABC3_"), is("Hello1 There2 ABC3_"));
	}
	
	@Test
	public void testUpperToCapitalized() {
		assertThat(TextUtil.upperToCapitalized(""), is(""));
		assertThat(TextUtil.upperToCapitalized("_HELLO_THERE_ABC_"), is("_Hello_There_Abc_"));
		assertThat(TextUtil.upperToCapitalized("_HELLO1_THERE2_ABC3_"), is("_Hello1_There2_Abc3_"));
	}
	
	@Test
	public void testUpperToPascal() {
		assertThat(TextUtil.upperToPascal(""), is(""));
		assertThat(TextUtil.upperToPascal("_HELLO_THERE_ABC_"), is("_HelloThereAbc_"));
		assertThat(TextUtil.upperToPascal("_HELLO1_THERE2_ABC3_"), is("_Hello1There2Abc3_"));
	}
	
	@Test
	public void testUpperToProperty() {
		assertThat(TextUtil.upperToProperty(""), is(""));
		assertThat(TextUtil.upperToProperty("HELLO_THERE_ABC"), is("hello.there.abc"));
		assertThat(TextUtil.upperToProperty("HELLO1_THERE2_ABC3"), is("hello1.there2.abc3"));
	}
	
	@Test
	public void testPropertyToUpper() {
		assertThat(TextUtil.propertyToUpper("hello.there.abc"), is("HELLO_THERE_ABC"));
		assertThat(TextUtil.propertyToUpper("hello1.there2.abc3"), is("HELLO1_THERE2_ABC3"));
	}
	
}
